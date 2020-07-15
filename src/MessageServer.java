// Created by: Stefan Nikolov
// User ID: 51768275



import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class MessageServer implements Runnable {
    Socket clientSocket;
    int userID;
    static HashMap<Integer, Socket> socketsMap = new HashMap<Integer, Socket>();    // This map holds user IDs and sockets
    static HashMap<Socket, String> usernameMap = new HashMap<Socket, String>();     // This map holds usernames and sockets
    static int userNumber = 1;  // Used as user ID; Each time a new user is introduced, it's incremented by 1
    String username;

    // Create lists of groups/topics
    static ArrayList<UserGroup> groups = new ArrayList<UserGroup>();
    static ArrayList<UserGroup> topics = new ArrayList<UserGroup>();

    MessageServer(Socket socket, int id) {
        this.clientSocket = socket;
        this.userID = id;
    }

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0])); // Listening on the socket specified as argument
        System.out.println("Listening on socket -> " + args[0]);

        while(true) {
            Socket socket = serverSocket.accept(); // Accepting the client socket
            MessageServer myServer = new MessageServer(socket, userNumber); // Create an instance of the server with the appropriate socket number and user ID
            new Thread(myServer).start();   // Start the server thread
            socketsMap.put(userNumber, socket); // Add the user ID and the sockets it's using to the hash map
            userNumber++;   // Increment the user ID so that no two users have the same ID
        }
    }

    public void run() {
        try {
            // Initialize output and input streams (reading from the socket)
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;   // The message that's received from the user

            // Some commands that the user can type and receive different functionality
            String help = "/help";  // Print all commands and their use
            String all = "/all";    // Message all clients
            String register = "/register";  // Register user
            String pm = "/pm";  // Private message a client
            // Group related commands
            String messageGroup = "/group"; // Send a message to all group members of a group
            String createGroup = "/create"; // Create a group
            String joinGroup = "/join"; // Join a group
            String leaveGroup = "/leave";   // Leave a group
            String removeGroup = "/remove"; // Remove a group
            String listGroups = "/groups";   // List all groups
            // Topic related commands
            String createTopic = "/topic";  // Create a topic with a keyword
            String subscribeToTopic = "/subscribe"; // Subscribe to a topic
            String unsubscribeFromTopic = "/unsubscribe";   // Unsubscribe from a topic
            String listTopics = "/topics";  // List all topics

            

            // Display welcome message
            output.println("Welcome to the chatroom.\n You must register if you want to send messages.\n" +
                            "To do so type /register <username> \n" + "If you wish to view all commands, type '/help");
            // Extract first line (should be /register <username>)
            String[] line = input.readLine().split(" ");
            // Set appropriate variables for registration
            String command = line[0];
            username = line[1];
            
            // Handle user registration
            if(command.equals(register)) {
                while(usernameMap.containsValue(username)) {
                    output.println("Username taken. Try again. There's no need to use /register. Just type a username.");
                    username = input.readLine().split(" ")[0];
                }
                usernameMap.put(socketsMap.get(userID), username);
                System.out.println(username + " has joined the chat.");
            }

            // Keep checking the user input
            while((inputLine = input.readLine()) != null) {
                // Take the user input and split it
                // The way that I've built it, we must know what the first word
                // of the user input is because it must be a command
                // Based on that command, different parts of the app will activate
                String usrMessage[] = inputLine.split(" "); 
            
                // Check if the user has typed '/help'
                // Print all available commands with explanations
                if(usrMessage[0].equals(help)) {
                    output = new PrintWriter(clientSocket.getOutputStream(), true);
                    output.println("(!)Note(!) Make sure you follow the specified syntax or else the service will not work.\n" + 
                                    "(1). /all <message> -> send a message to all clients.\n" +
                                    "(2). /pm <username> <message> -> send a private message to a client.\n" + 
                                    "(3). /create <groupname> -> create a group and also join it.\n" + 
                                    "(4). /join <groupname> -> join a group.\n" + 
                                    "(5). /group <groupname> -> send a message to all members of a group.\n" + 
                                    "(6). /leave <groupname> -> leave a group.\n" + 
                                    "(7). /remove <groupname> -> remove a group if you are the creator.\n" +
                                    "(8). /groups -> list all currently active groups.\n" +
                                    "(9). /topic <keyword> -> create a topic for the given keyword.\n" +
                                    "(10). /subscribe <keyword> -> subscribe to see messages that include the given keyword.\n" +
                                    "(11). /unsubscribe <keyword> -> unsubscribe from the topic and no longer see messages with the keyword.\n" +
                                    "(12). /topics -> list all currently active topics.\n");
                } else if(usrMessage[0].equals(all)) {  // Send a message to all clients currently connected
                    // An array to keep track of who has received the message
                    ArrayList<Socket> seen = new ArrayList<Socket>();
                    // This first for-loop handles the case where a topic word is included in the message
                    for(int j = 1; j < usrMessage.length; j++) {    // Loop over each word in the message
                        for(int k = 0; k < topics.size(); k++) {    // Loop over each registered keyword topic
                            // The first part of the if statement checks whether there is a topic word
                            // A topic word is a keyword with "#" in the beginning hence the use of startswith()
                            // The second part checks is the keyword matches any created topics
                            if(usrMessage[j].startsWith("#") && usrMessage[j].equals(topics.get(k).name)) {
                                // Now we have to loop over all subscribers and send them the message
                                for(int counter = 0; counter < topics.get(k).groupSize(); counter++) {
                                    Socket listener = topics.get(k).getMember(counter);
                                    if(!seen.contains(listener)) {
                                        output = new PrintWriter(listener.getOutputStream(), true);
                                        output.printf("[%s] %s:", topics.get(k).name, usernameMap.get(clientSocket));
                                        for(int l = 1; l < usrMessage.length; l++) {
                                            output.printf("%s ", usrMessage[l]);
                                        }
                                        seen.add(listener);
                                        output.println("");
                                        output.flush();
                                    }
                                }
                            }
                        }
                    }
                    // Loop through all sockets that are in the map at this time
                    for(int i = 1; i <= socketsMap.size(); i++) {
                        Socket listener = socketsMap.get(i);
                        // If the listener hasn't received the message
                        if(!seen.contains(listener)) {
                            output = new PrintWriter(listener.getOutputStream(), true);
                            output.printf("[All] %s: ", usernameMap.get(clientSocket));
                            for(int index = 1; index < usrMessage.length; index++) {
                                output.printf("%s ", usrMessage[index]);
                            }
                            output.println("");
                            output.flush();
                        }   
                    }
                } else if(usrMessage[0].equals(pm)) {   // Handle personal messaging between two users
                    String listener = usrMessage[1];    // Get the username of the recepient (command should be /pm <username> <message>)
                    if(usernameMap.containsValue(listener)) {   // Check if the username exists in the hash map

                        // System.out.println(username + " tried to pm " + listener);
                        // Using a helper function to extract the recipient socket (port) number from the hash map using their username
                        Socket recepient = getKeyByValue(usernameMap, listener);
                        // Handle messaging the same way as when messaging everybody
                        output = new PrintWriter(recepient.getOutputStream(), true);
                        output.printf("[PM] %s: ", username);
                        for(int index = 2; index < usrMessage.length; index++) {
                            output.printf("%s ", usrMessage[index]);
                        }
                        output.println("");
                        output.flush();
                    } else {
                        output = new PrintWriter(clientSocket.getOutputStream(), true);
                        output.println("No such user exists.");
                        output.flush();
                    }
                } else if(usrMessage[0].equals(createGroup)) {
                    // Just in case the output stream has been changed
                    output = new PrintWriter(clientSocket.getOutputStream(), true);
                    boolean exists = false;
                    if(groups.size() > 0) {
                        for(int i = 0; i < groups.size(); i++) {
                            if(groups.get(i).name.equals(usrMessage[1])) {
                                output.println("Group already exists!\n" + 
                                                "Please try again.");       
                                output.flush();
                                exists = true;
                                break;
                            }
                        }
                    }
                    if(!exists) {
                        UserGroup newGroup = new UserGroup(usrMessage[1]);
                        newGroup.addMember(clientSocket);
                        groups.add(newGroup);
                        output.println("You have successfully created a new group.\n" + 
                                        "And you have joined it.");
                        output.flush();        
                    }
                } else if(usrMessage[0].equals(joinGroup)) {
                    boolean groupExists = false;    // Flag if the group exists
                    int index = 0;  // The index in the array of the group (if it exists)
                    for(int i = 0; i < groups.size(); i++) {    // Loop over all groups 
                        if(groups.get(i).name.equals(usrMessage[1])) {  // If the group exists, switch the flag and records its index
                            groupExists = true;
                            index = i;               
                        }
                    }
                    output = new PrintWriter(clientSocket.getOutputStream(), true);
                    if(groupExists && !groups.get(index).memberInGroup(clientSocket)) { // If the group exists and the user is currently NOT a member then we add him
                        
                        groups.get(index).addMember(clientSocket);
                        System.out.println(usernameMap.get(socketsMap.get(userID)) + " has joined the group [" + usrMessage[1] + "].");
                        output.println("You have joined the group [" + usrMessage[1] + "].");
                    } else if(!groupExists) {
                        output.println("No such group exists. Try again.");
                    } else if(groups.get(index).memberInGroup(clientSocket)) {
                        output.println("You are already a member of this group.");
                    }
                    output.flush();

                } else if(usrMessage[0].equals(leaveGroup)) {   // Again we have to check first if the group the user is trying to leave exists
                    boolean groupExists = false;
                    int index = 0;
                    for(int i = 0; i < groups.size(); i++) {
                        if(groups.get(i).name.equals(usrMessage[1])) {
                            groupExists = true;
                            index = i;
                        }
                    }
                    output = new PrintWriter(clientSocket.getOutputStream(), true);
                    if(groupExists) {
                        groups.get(index).removeMember(clientSocket);
                        output.println("You have successfully left the group " + groups.get(index).name);
                    } else {
                        output.println("You cannot leave this group. You are either not a member\n" + 
                                        " or it doesn't exist.");
                    }
                    output.flush();

                } else if(usrMessage[0].equals(messageGroup)) {
                    ArrayList<Socket> seen = new ArrayList<Socket>();   // Using the same method as in "message all" to keep track of people we've already sent the message to
                    boolean userInGroup = false;    // Flag if the user belongs in the group
                    int index = 0;    // Used to save the index of the group in the "groups" array

                    for(int i = 0; i < groups.size(); i++) {
                        if(groups.get(i).name.equals(usrMessage[1]) && groups.get(i).memberInGroup(clientSocket)) {
                            userInGroup = true;
                            index = i;
                        }
                    }
                    // If the user is in the group and the group exists, we loop over all members and send them the message
                    if(userInGroup) {
                        for(int j = 2; j < usrMessage.length; j++) {    // Loop over each word in the message
                            for(int k = 0; k < topics.size(); k++) {    // Loop over each registered keyword topic
                                // The first part of the if statement checks whether there is a topic word
                                // A topic word is a keyword with "#" in the beginning hence the use of startswith()
                                // The second part checks is the keyword matches any created topics
                                if(usrMessage[j].startsWith("#") && usrMessage[j].equals(topics.get(k).name)) {
                                    // Now we have to loop over all subscribers and send them the message
                                    for(int counter = 0; counter < topics.get(k).groupSize(); counter++) {
                                        Socket listener = topics.get(k).getMember(counter);
                                        if(!seen.contains(listener)) {
                                            output = new PrintWriter(listener.getOutputStream(), true);
                                            output.printf("[%s] %s:", topics.get(k).name, usernameMap.get(clientSocket));
                                            for(int l = 2; l < usrMessage.length; l++) {
                                                output.printf("%s ", usrMessage[l]);
                                            }
                                            seen.add(listener);
                                            output.println("");
                                            output.flush();
                                        }
                                    }
                                }
                            }
                        }
                        for(int i = 0; i < groups.get(index).groupSize(); i++) {
                            Socket listener = groups.get(index).getMember(i);
                            if(!seen.contains(listener)) {  // Make sure we don't send the same message twice
                                output = new PrintWriter(listener.getOutputStream(), true);
                                output.printf("[%s] %s:", groups.get(index).name, usernameMap.get(clientSocket));
                                for(int j = 2; j < usrMessage.length; j++) {    // Here we start from 2 because the first two symbols are the keyword and group name
                                    output.printf("%s ", usrMessage[j]);
                                }
                                output.println("");
                                output.flush();
                            }
                        }
                    }
                } else if(usrMessage[0].equals(removeGroup)) {
                    // The only person that can remove the group is the creator
                    // The creator is always the first entry in the group members list (the 0th index)
                    boolean exists = false;
                    int index = 0;
                    // As usual first we loop over all groups to see if the group exists 
                    for(int i = 0; i < groups.size(); i++) {
                        if(groups.get(i).name.equals(usrMessage[1])) {  // Here we do not care if the user is in the group because we will make that check further down
                            exists = true;
                            index = i;
                        }
                    }
                    
                    // If the group exists we proceed to do some further checks
                    if(exists) {
                        output = new PrintWriter(clientSocket.getOutputStream(), true);
                        // The first part of the if gets the group based on the index and check if the user is a member of it
                        // The second part gets the group based on the index and uses a method that returns the index of the member in the group array
                        // If the index equals 0 this means that the user is the original creator of the group so we can proceed with removing the group
                        if(groups.get(index).memberInGroup(clientSocket) && (groups.get(index).getMemberIndex(clientSocket) == 0)) {
                            groups.get(index).clearMembers();
                            System.out.println("User [" + username + "] has removed the group [" + groups.get(index).name + "].");
                            output.println("You have successfully removed the group [" + groups.get(index).name + "].");
                        } else {
                            output.println("You do not have the right to remove this group.");
                        }
                    } else {
                        output.println("No such group exists.");
                    }
                    output.flush();
                } else if(usrMessage[0].equals(listGroups)) {
                    // Get the output stream of the current user
                    output = new PrintWriter(clientSocket.getOutputStream(), true);
                    // Loop over all
                    if(groups.size() != 0) {
                        for(int i = 0; i < groups.size(); i++) {
                            // Print their names on new lines
                            output.println("(" + i + ") -> " + groups.get(i).name + "\n");
                        }
                    } else {
                        output.println("No groups exist.");
                    }
                    output.println("");
                    output.flush();
                } else if(usrMessage[0].equals(createTopic)) {
                    // The way we create topics is the same as with groups (the code is absolutely the same)
                    // TODO: Extract it to a method
                    output = new PrintWriter(clientSocket.getOutputStream(), true);
                    boolean exists = false;
                    if(topics.size() > 0) {
                        for(int i = 0; i < topics.size(); i++) {
                            if(topics.get(i).name.equals(usrMessage[1])) {
                                output.println("Group already exists!\n" + 
                                                "Please try again.");       
                                output.flush();
                                exists = true;
                                break;
                            }
                        }
                    }
                    if(!exists) {
                        UserGroup newGroup = new UserGroup("#" + usrMessage[1]);
                        newGroup.addMember(clientSocket);
                        topics.add(newGroup);
                        output.println("You have successfully created a new topic.\n" + 
                                        "To reference it in the future, use #<keyword>.");
                        output.flush();        
                    }
                } else if(usrMessage[0].equals(subscribeToTopic)) {
                    boolean exists = false;
                    int index = 0;
                    for(int i = 0; i < topics.size(); i++) {
                        if(topics.get(i).name.equals("#" + usrMessage[1])) {
                            exists = true;
                            index = i;
                        }
                    }
                    output = new PrintWriter(clientSocket.getOutputStream(), true);
                    if(exists && !topics.get(index).memberInGroup(clientSocket)) {
                        
                        topics.get(index).addMember(clientSocket);
                        System.out.println(usernameMap.get(socketsMap.get(userID)) + " has subscribed to the topic #" + usrMessage[1] + ".");
                        output.println("You have subscribed to the topic #" + usrMessage[1] + ".");
                    } else if(!exists) {
                        output.println("No such topic exists. Try again.");
                    } else if(topics.get(index).memberInGroup(clientSocket)) {
                        output.println("You are already subscribed to this topic.");
                    }
                    output.flush();
                } else if(usrMessage[0].equals(unsubscribeFromTopic)) {
                    boolean exists = false;
                    int index = 0;
                    for(int i = 0; i < topics.size(); i++) {
                        if(topics.get(i).name.equals("#" + usrMessage[1])) {
                            exists = true;
                            index = i;
                        }
                    }

                    output = new PrintWriter(clientSocket.getOutputStream(), true);
                    if(exists) {
                        topics.get(index).removeMember(clientSocket);
                        output.println("You are no longer subscribed to #" + topics.get(index).name);
                    } else {
                        output.println("You cannot unsubscribe. You are either not a subscriber\n" + 
                                        " or no such topic exists.");
                    }
                    output.flush();
                } else if(usrMessage[0].equals(listTopics)) {
                    // Get the output stream of the current user
                    output = new PrintWriter(clientSocket.getOutputStream(), true);
                    // Loop over all
                    if(topics.size() != 0) {
                        for(int i = 0; i < topics.size(); i++) {
                            // Print their names on new lines
                            output.println("(" + i + ") -> " + topics.get(i).name + "\n");
                        }
                    } else {
                        output.println("There are no topics.");
                    }
                    output.println("");
                    output.flush();
                } else {
                    output = new PrintWriter(clientSocket.getOutputStream(), true);
                    output.println("Command not recognised! Type '/help' to view all available commands.");
                }
            
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Function taken from https://stackoverflow.com/questions/1383797/java-hashmap-how-to-get-key-from-value
    public static <Socket, String> Socket getKeyByValue(HashMap<Socket, String> map, String value) {
        for(Socket entry : map.keySet()) {
            if(map.get(entry).equals(value)) {
                return entry;
            }
        }
        return null;
    }

}



