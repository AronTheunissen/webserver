package nl.sogyo.webserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class ConnectionHandler implements Runnable {
    private Socket socket;

    public ConnectionHandler(Socket toHandle) {
        this.socket = toHandle;
    }

    /// Handle the incoming connection. This method is called by the JVM when passing an
    /// instance of the connection handler class to a Thread.
    public void run() {
        try {

            // Set up a reader that can conveniently read our incoming bytes as lines of text.
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = null;
            StringBuilder requestString = new StringBuilder();
            do {
                // Read the incoming message line by line and echo it to the system out.
                line = reader.readLine();
                requestString.append(line).append(System.getProperty("line.separator"));
            } while (!line.isEmpty());


            ResponseWriter response = getResponseWriter(reader, new RequestParse(requestString.toString()));
            
            // Set up a writer that can write text to our binary output stream.
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            // Write a simple hello world textual response to the client.

            writer.write(getResponseString(response));
            writer.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // After handling the request, we can close our socket.
            try {
                socket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ResponseWriter getResponseWriter(BufferedReader reader, RequestParse request) throws IOException {
        System.out.println("The test sentence is" + request.getHeaderParameterNames().contains("Content-Length"));
        if (request.getHeaderParameterNames().contains("Content-Length")) {
            char bodyChar;
            StringBuilder bodyString = new StringBuilder();
            int bodyLength = 0;
            while (bodyLength < Integer.parseInt(request.getHeaderParameterValue("Content-Length"))) {
                bodyChar = (char) reader.read();
                bodyString.append(bodyChar);
                bodyLength++;
            }
            System.out.println();
            return new ResponseWriter(request, bodyString.toString());
        }
        return new ResponseWriter(request);
    }

    private String getResponseString(ResponseWriter response) {
        StringBuilder responseString = new StringBuilder();
        responseString.append("HTTP/1.1 ").append(response.getStatus().getCode()).append(" ")
                .append(response.getStatus().getDescription()).append("\r\n");
        if (response.getStatus().getCode() < 300) {
            response.getCustomHeaders().forEach((key, value) -> responseString.append(key)
                    .append(": ").append(value).append("\r\n"));
            responseString.append("\r\n").append(response.getContent());
        }
        return responseString.toString();
    }


    public static void main(String... args) {
        try {
            // A server socket opens a port on the local computer (in this program port 9090).
            // The computer now listens to connections that are made using the TCP/IP protocol.
            ServerSocket socket = new ServerSocket(9090);
            System.out.println("Application started. Listening at localhost:9090");
            // We are going to use threading. Plain threads (i.e. new Thread(...)) are very expensive -
            // it requires a low-level call to the operating system (kernel) for every thread. By using
            // a thread pool, we can reuse a single operating system thread for multiple requests. This
            // is also known als multiplexing.
            ExecutorService threadPool = Executors.newCachedThreadPool();

            // Start an infinite loop. This pattern is common for applications that run indefinitely
            // and react on system events (e.g. connection established). Inside the loop, we handle
            // the connection with our application logic. 
            while(true) {
                // Wait for someone to connect. This call is blocking; i.e. our program is halted
                // until someone connects to localhost:9090. A socker is a connection (a virtual
                // telephone line) between two endpoints - the client (browser) and the server (this).
                Socket newConnection = socket.accept();
                // We want to process our incoming call. Furthermore, we want to support multiple
                // connections. Therefore, we handle the processing on a background thread. Java
                // takes care of finding an available thread for us. We submit a new task (implementing
                // the Runnable interface) by passing it into the submit function.
                // When a Runnable is submitted, the thread is started by calling the run() method of
                // the runnable (which is the ConnectionHandler).
                // As our handling is in a background thread, we can accept new connections on the
                // main thread (in the next iteration of the loop).
                // Starting the thread is so-called fire and forget. The main thread starts a second
                // thread and forgets about its existence. We receive no feedback on whether the
                // connection was handled gracefully.
                threadPool.submit(new ConnectionHandler(newConnection));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
