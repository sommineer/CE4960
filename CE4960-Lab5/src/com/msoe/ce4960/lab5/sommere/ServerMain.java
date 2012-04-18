package com.msoe.ce4960.lab5.sommere;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Runs a state-less file server that responds to UDP requests.  Main Program
 * 
 * Experiences:
 * Overall this lab wasn't all that difficult.  Working in java with the lack
 * of unsigned integers made things a bit more complicated.  You essentially
 * have to store them in double the storage space, so the 16-bit integer was
 * actually stored as a 32-bit integer.  Other than that though, no other
 * major difficulties were encountered
 * 
 * Testing:
 * This program was tested with a rough client that was written at the same
 * time.  It was also tested in Lab using a client written by another student.
 * We were able to transfer text files, .pdf files, and .jpg files successfully.
 * At first the .pdf files did not appear to work, but it must've been an issue
 * on the client's machine since a binary diff of the source and the received
 * files says they are identical.  The protocol was also verified by analyzing
 * WireShark traffic.  
 * 
 * Changes:
 * Very little of the Server code was changed in-lab.  Most
 * of the changes involved adding debugging info or enhancing error handling.
 * The only major changes were altering the file reader to read bytes instead
 * of characters, and adding a bit of a UI to the server control.
 * 
 * Usage:
 * Run this file with the port as a command line argument.  Otherwise run it
 * and it will prompt you for the required info.
 * 
 * @author Erik Sommer
 *
 */
public class ServerMain {

	/**
	 * Maximum port number
	 */
	private static final int MAX_PORT_NUM = 65535;
	
	/**
	 * String to return when the program is started incorrectly
	 */
	static final String USAGE_STRING = "Usage: ServerMain Port";
	
	/**
	 * Gets the port to bind to from the user
	 * @return	the port to bind to
	 */
	private static int getPort(){
		
		int port = -1;
		boolean valid = false;
		
		Scanner scanner = new Scanner(System.in);
		
		while(!valid){
			System.out.print("Enter a port to listen on: ");
			try{
				port = scanner.nextInt();
			}catch(InputMismatchException e){
				scanner.next();
				valid = false;
			}
			
			if((port < 0) || (port > MAX_PORT_NUM)){
				System.err.println("Invalid port specified (must be between " +
						"0 and " + MAX_PORT_NUM + ")");
			}else{
				valid = true;
			}
		}
		
		return port;
	}
	
	/**
	 * Main method
	 * @param args	first argument is the port number, all others are ignored
	 */
	public static void main(String[] args){

		int port;
		
		// If there isn't a command line argument, prompt the user for a port
		if(args.length < 1){
			port = getPort();
		}else{
			try{
				port = Integer.parseInt(args[0]);
			}catch(NumberFormatException e){
				System.out.println(USAGE_STRING);
				return;
			}
		}

		try {
			ServerSocket serverSocket = new ServerSocket(port);

			// Setup the socket
			System.out.println("Connected to socket");
			System.out.println("Port: " + port);

			// Start listening for connections
			while(true){
				
				// Receive a connection and spin off the handler
				Socket clientSocket = serverSocket.accept();
				System.out.println("Received connection, starting client handler...");
				ClientHandler handler = new ClientHandler(clientSocket);
				
				(new Thread(handler)).start();
			}
		} catch (SocketException e) {
			System.err.println("An error occured connecting to the socket");
			e.printStackTrace();
		}catch (IOException e) {
			System.err.println("An I/O error occured while trying to receive data");
			e.printStackTrace();
		}
	}
}