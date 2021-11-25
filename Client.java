package a21260210.Sockets.Exercises;

import java.io.*;
import java.net.Socket;

public class Exercise8ClientConcurrentV2 {
    private static final String MSG_FILE_NOT_FOUND = "File not found";
    private static final String MSG_TRANSFER_COMPLETE = "Transfer complete";
    private static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) {

        // server data
        Socket client;      // client socket
        String hostname;    // server hostname
        int port;           // sever port

        String filename;    // filename of requested file
        String path;        // path to store requested file
        StringBuilder file;
        FileOutputStream fileOutputStream;

        // server reply
        byte[] buffer;      // buffer to get server reply
        String reply;       // server reply



        // verify arguments
        if (args.length != 3){
            System.err.println("\nSyntax error.");
            System.err.println("Try: java Client.java [ip:port] [file] [path]");
            return;
        }

        // get server hostname and port
        String[] hostnameAndPort = args[0].split(":");
        if (hostnameAndPort.length != 2){
            System.err.println("\nInvalid server IP and Port.");
            return;
        }

        // set server hostname and port
        hostname = hostnameAndPort[0];
        port = Integer.parseInt(hostnameAndPort[1]);

        // set filename and path of requested file
        filename = args[1];
        path = args[2];

        // display info
        System.out.printf("\nServer -> [%s:%d]\nRequested file -> [%s]\nStore file in path -> [%s]\n",
                hostname, port, filename, path);

        // verify if path exists
        if (!isPathOrFileValid(path)){
            System.err.println("The path is invalid.");
            return;
        }


        try {
            // create the client socket
            client = new Socket(hostname, port);
            System.out.println("Connection with server established...");

            // send request to server
            sendRequest(client.getOutputStream(), filename.getBytes());

            // verify if the file exists
            // get the reply from the server
            buffer = getReply(client.getInputStream());
            reply = new String(buffer, 0, buffer.length);

            if (reply.equals(MSG_FILE_NOT_FOUND)){
                System.out.println("The server couldn't find the requested file.");
                client.close();
                return;
            }

            // create path + filename
            file = new StringBuilder(path).append(filename);
            fileOutputStream = new FileOutputStream(file.toString());

            // loop indefinitely to get server reply
            while (true){
                if (reply.equals(MSG_TRANSFER_COMPLETE)){
                    System.out.println("The file transfer is complete.");
                    break;
                }

                // write data to the new file
                fileOutputStream.write(buffer);

                // get the reply from the server
                buffer = getReply(client.getInputStream());
                reply = new String(buffer, 0, buffer.length);
            }

            // close client
            client.close();
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

    /**
     * Send a request to the server
     * @param outputStream server output stream
     * @param buffer data to be sent
     */
    private static void sendRequest(OutputStream outputStream, byte[] buffer){
        PrintStream printStream = new PrintStream(outputStream, true);
        String request = new String(buffer, 0, buffer.length);
        printStream.println(request);
    }

    /**
     * get the reply from the serve
     * @param inputStream server input stream
     * @return reply from server as byte array
     */
    private static byte[] getReply(InputStream inputStream){
        byte[] buffer = new byte[BUFFER_SIZE];

        try {
            buffer = inputStream.readNBytes(BUFFER_SIZE);
            //buffer = inputStream.readAllBytes();
        }
        catch (IOException e) {
            System.err.println("I/O exception: " + e);
        }
        return buffer;
    }

}
