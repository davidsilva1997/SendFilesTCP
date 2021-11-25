package a21260210.Sockets.Exercises;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Exercise8ServerConcurrentV2 {

    public static void main(String[] args) {

        // Server data
        ServerSocket server;        // server socket
        InetAddress serverAddress;  // server address
        int port;                   // server port

        String path;    // path of files that can be shared

        // verify arguments
        if (args.length != 2){
            System.err.println("\nSyntax error.");
            System.err.println("Try: java Server.java [port] [path]");
            return;
        }

        // set the port and the path
        port = Integer.parseInt(args[0]);
        path = args[1];

        // verify if the path exists
        if (!isPathOrFileValid(path)){
            System.err.println("The path is invalid.");
            return;
        }


        try {
            // get the server ip
            serverAddress = InetAddress.getLocalHost();

            // create the server socket
            server = new ServerSocket(port);

            // display info about server
            System.out.printf("\nConcurrent Server running [%s:%d]\n", serverAddress.getHostAddress(), server.getLocalPort());

            // indefinitely loop, accepting new clients
            while (true){
                new Thread(new ThreadServerV2(server.accept(), path)).start();
            }

        }
        catch (IOException e) {
            System.err.println("I/O exception: " + e);
        }

    }


    /**
     * Verify if a path or a file exists
     * @param value name of path or file
     * @return true if exists
     */
    private static boolean isPathOrFileValid(String value){
        return new File(value).exists();
    }
}

class ThreadServerV2 implements Runnable {
    private static final String MSG_FILE_NOT_FOUND = "File not found";
    private static final String MSG_TRANSFER_COMPLETE = "Transfer complete";
    private static final int BUFFER_SIZE = 4096;

    Socket client;      // client socket
    String path;        // path of files that can be shared

    // constructor
    public ThreadServerV2(Socket client, String path){
        this.client = client;
        this.path = path;
    }

    // work to do
    public void run() {
        StringBuilder requestedFile;    // path + filename
        String request;                 // client request (filename)
        InputStream fileInputStream;
        byte[] readBytes = new byte[BUFFER_SIZE];
        long startTime;
        long stopTime;
        long duration;

        try {
            // display client info
            System.out.printf("\nNew client connected [%s:%d]\n", client.getInetAddress(), client.getPort());

            // get client request
            request = getRequest(client.getInputStream());
            requestedFile = new StringBuilder(path).append(request);
            System.out.println("Client requested file: " + requestedFile);

            // verify if requested file exists
            if (!isPathOrFileValid(requestedFile.toString())){
                System.out.printf("Requested file from client [%s:%d] not found.\n", client.getInetAddress(), client.getPort());

                // reply to the client
                reply(client.getOutputStream(), MSG_FILE_NOT_FOUND.getBytes());

                // close the connection with the client
                client.close();

                return;
            }

            // set the file input stream
            fileInputStream = new FileInputStream(requestedFile.toString());

            System.out.printf("File transfer [%s] with client [%s:%d] started.\n",
                    requestedFile, client.getInetAddress(), client.getPort());


            // start the transfer
            startTime = System.nanoTime();

            while ((fileInputStream.read(readBytes)) != -1){
                reply(client.getOutputStream(), readBytes);
            }

            stopTime = System.nanoTime();
            duration = (stopTime - startTime) / 1000000;

            System.out.printf("File transfer [%s] with client [%s:%d] completed in [%d] milliseconds.\n",
                    requestedFile, client.getInetAddress(), client.getPort(), duration);

            // warn the client that the file transfer is complete
            reply(client.getOutputStream(), MSG_TRANSFER_COMPLETE.getBytes());

            // close client connection
            client.close();
        }
        catch (IOException e) {
            System.err.println("I/O exception: " + e);
        }


    }

    /**
     * get the request from a client
     * @param inputStream client input stream
     * @return client request
     */
    private String getRequest(InputStream inputStream){
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String request = null;

        try {
            // read the request from client
            request = in.readLine();
        }
        catch (IOException e) {
            System.err.println("I/O exception: " + e);
        }

        return request;
    }

    /**
     * reply to the client
     * @param outputStream client output stream
     * @param buffer reply to send to the client
     */
    private static void reply(OutputStream outputStream, byte[] buffer){
        PrintStream printStream = new PrintStream(outputStream, true);
        printStream.write(buffer, 0, buffer.length);

        //String reply = new String(buffer, 0, buffer.length);
        //printStream.println(reply);
    }

    /**
     * Verify if a path or a file exists
     * @param value name of path or file
     * @return true if exists
     */
    private static boolean isPathOrFileValid(String value){
        return new File(value).exists();
    }
}
