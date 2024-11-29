package com.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


public class Server {

    private final ConcurrentHashMap<String ,String> cache = new ConcurrentHashMap<>();
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public void server(int port,String address) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port),0);

        server.createContext("/",new ProxyHandler(address,cache));
        server.setExecutor(null);
        server.start();
        logger.info("Server is running on port: "+port);
    }

    static class ProxyHandler implements HttpHandler{
        private final String origin;
        private final ConcurrentHashMap<String,String> cache;
        private static final Logger logger = Logger.getLogger(ProxyHandler.class.getName());

        public ProxyHandler(String origin,ConcurrentHashMap<String,String> cache) {
            this.origin = origin;
            this.cache = cache;
        };

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

            String requestPath = httpExchange.getRequestURI().getPath();
            String targetUrl = origin + requestPath;
            logger.info("Received request for: " + targetUrl);

            try {
                if(cache.containsKey(targetUrl)){
                    String cachedResponse = cache.get(targetUrl);
                    logger.info("Cache hit for: " + targetUrl);
                    httpExchange.getResponseHeaders().set("X-Source", "Cache");
                    httpExchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                    httpExchange.sendResponseHeaders(200, cachedResponse.getBytes().length);
                    OutputStream os = httpExchange.getResponseBody();
                    os.write(cachedResponse.getBytes());
                    os.close();
                    return;
                }

                HttpURLConnection connection = (HttpURLConnection) new URI(targetUrl).toURL().openConnection();
                connection.setRequestMethod(httpExchange.getRequestMethod());

                int responseCode = connection.getResponseCode();
                logger.info("Response code from origin server: " + responseCode);

                boolean redirect  = responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_SEE_OTHER;

                if(redirect) {
                    String newUrl = connection.getHeaderField("Location");
                    logger.info("Redirected to: " + newUrl);

                    connection = (HttpURLConnection) new URI(newUrl).toURL().openConnection();
                    responseCode = connection.getResponseCode();
                    logger.info("Response code after redirect: " + responseCode);
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream()
                ));

                StringBuilder responseBuilder = new StringBuilder();

                String line;

                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                reader.close();

                String response = responseBuilder.toString();
                cache.put(targetUrl,response);
                logger.info("Fetched and cached response for: " + targetUrl);

                httpExchange.getResponseHeaders().set("X-Source", "Server");
                httpExchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                httpExchange.sendResponseHeaders(responseCode, responseBytes.length);
                OutputStream os = httpExchange.getResponseBody();
                os.write(responseBytes);
                os.close();
            } catch (Exception e) {
                String errorMessage = "An error occurred: "+e.getMessage();
                logger.severe(errorMessage);
                httpExchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                httpExchange.sendResponseHeaders(500,errorMessage.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(errorMessage.getBytes(StandardCharsets.UTF_8));
                os.close();
            }
        }
    }
}
