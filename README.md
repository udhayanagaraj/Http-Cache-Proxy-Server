# Caching Proxy Server   

This project implements a simple caching proxy server in Java. The proxy server intercepts HTTP requests, forwards them to an origin server, caches the response, and serves subsequent requests from the cache. It also logs the source of the response (whether it is served from the cache or the origin server).

### Prerequisites

Before running the server, make sure you have the following:

- Java 8 or higher installed on your machine.

### Setup and Running the Project

1. Download and Extract the Project
Download the project ZIP file and extract it to a folder on your machine.

2. Navigate to the Source Code Directory
After extracting the project, navigate to the source code directory:    
   cd <path-to-extracted-folder>/src/main/java

3. Compile the Java Files
To compile the Java files, use the following javac command:    
   javac com/example/*.java

4. Run the Proxy Server
To run the caching proxy server, use the java command with the appropriate arguments:    
   java com.example.Main --port portnum --origin actualOrigin
   - portnum: The port number on which the proxy server will listen (e.g., 3000).
   - actualOrigin: The origin URL that the proxy server will forward requests to (e.g., https://dummyjson.com).
  
5. Testing the Proxy Server
Open your browser or use a tool like curl to make a request to the proxy server.
   - curl http://localhost:3000/products 
