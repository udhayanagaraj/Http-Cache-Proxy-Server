package com.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        if(args.length == 0){
            System.out.println("Invalid arguments provided");
            System.out.println("Proxy server");
            System.out.println("Usage: ");
            System.out.println("caching-proxy --port <number> --origin <url>");
            System.out.println("Example: ");
            System.out.println("caching-proxy --port 3000 --origin https://dummyjson.com");
            return;
        }

        if(args[0].equals("--help")) {
            System.out.println("Proxy server");
            System.out.println("Usage: ");
            System.out.println("caching-proxy --port <number> --origin <url>");
            System.out.println("Example: ");
            System.out.println("caching-proxy --port 3000 --origin https://dummyjson.com");
            return;
        }



        int port = 0;
        String origin = null;

        for (int i = 0; i < args.length; i++) {
            if(args[i].equals("--port")){
                port = Integer.parseInt(args[++i]);
            }
            if(args[i].equals("--origin")){
                origin = args[++i];
            }
        }

        if(port == 0 || origin == null){
            System.out.println("Invalid arguments provided");
            System.out.println("Usage:");
            System.out.println("caching-proxy --port <number> --origin <url>");
            return;
        }
        System.out.println("Staring caching proxy server...");
        System.out.println("Port: "+port);
        System.out.println("Origin: "+origin);


        Server server = new Server();
        server.server(port,"https://dummyjson.com");

    }
}