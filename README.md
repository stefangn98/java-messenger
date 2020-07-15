# A simple CLI Messenger App in Java

## How to compile:
* Extract the _**src**_ folder
* Open a terminal and navigate to it
* Type _**make**_ in the terminal and wait

## How to run:
As this app was developed with the CLI in mind it has to be run from it.

As it is based on socket communication, when executing you should specify an open port number.

**NOTE:** The server defaults to _localhost_ as the host so you should keep that in mind too.

1. How to start the server
	1. Open a terminal and navigate to the folder (src)
	1. Execute the following command: _java MessageServer "portnumber"_
2. How to start the client(s)
	2. Open a terminal and navigate to the folder (src)
	2. Execute the following command: _java MessageServerClient localhost "portnumber"_
	
**Note:** The clients should use the same port number as the server or else they won't be able to communicate
	
