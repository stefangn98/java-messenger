# A simple CLI Messenger App in Java

## Instructions

* Once extracted, use the _**make**_ command in the terminal in the _src_ folder
* To start the server execute: _java MessageServer **portnumber**_ 
* To start a client execute: _java MessageServerClient localhost **portnumber**_ 

**NOTE:** The app defaults to localhost so you should not change that and also remember that each client must use the same port number as the server.

## Functionality
The app provides some basic features for a messenger app.
As a user you can:
- Register with a unique name
- View all available commands
- Message all currently connected clients
- Private message other specific clients
- Create, remove, join and leave groups of clients
- Subscribe to a certain topic and view all messages regarding it (like the _Twitter_ hashtag)