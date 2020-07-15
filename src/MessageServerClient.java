// Created by: Stefan Nikolov
// User ID: 51768275

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;

// Because there will be multiple clients we have to implement threading
// we do that by using "Runnable"
public class MessageServerClient implements Runnable {
    // Declare required fields for each client
    // client socket number
    private static Socket socket = null;
    // client output stream
    private static PrintStream out = null;
    // client input stream
    private static DataInputStream in = null;

    private static BufferedReader iLine = null;
    private static boolean closed = false;

    public static void main(String[] args) {
        // Set default host and port number in case the user doesn't specify any
        String defaultHost = "localhost";
        int defaultPort = 50015;
        // If the user hasn't specified a host and a port number we use the default ones
        if(args.length < 2) {
            System.out.println("MessageServerClient <host> <port> \n" +
                                "Now using host=" + defaultHost +", port=" + defaultPort);
        } else {    // Otherwise set them to the commandline arguments passed
            defaultHost = args[0];
            defaultPort = Integer.parseInt(args[1]);
        }

        // Open the socket on the host and the user input and output streams
        try {
            // Initialize all class fields
            // Initialize the clients' socket to the provided ones in cmd
            socket = new Socket(defaultHost, defaultPort);
            // Output and input streams are used to write/read to and from the socket
            out = new PrintStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            // This is used to read input from the user using System.in
            iLine = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(socket != null && out != null && in != null) {
            try {
                new Thread(new MessageServerClient()).start();
                while(!closed) {
                    out.println(iLine.readLine());
                }
                out.close();
                in.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        String response;
        try {
            while((response = in.readLine()) != null) {
                System.out.println(response);
            }
            closed = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}