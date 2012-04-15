package com.msoe.ce4960.lab4.sommere;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Runs a state-less file server that responds to UDP requests
 * @author Erik Sommer
 *
 */
public class ServerMain {

	/**
	 * String to return when the program is started incorrectly
	 */
	static final String USAGE_STRING = "Usage: ServerMain Port";
	
	/**
	 * Main method
	 * @param args	first argument is the port number, all others are ignored
	 */
	public static void main(String[] args){

		// Ensure there is an argument
		if(args.length < 1){
			System.out.println(USAGE_STRING);
			return;
		}

		// Attempt to parse the port
		int port;
		try{
			port = Integer.parseInt(args[0]);
		}catch(NumberFormatException e){
			System.out.println(USAGE_STRING);
			return;
		}

		try {
			// Setup the socket
			System.out.println("Starting to listen on port " + port);
			
			DatagramSocket serverSocket = new DatagramSocket(port);

			byte[] receiveData = new byte[1024];

			// Start listening for packets
			while(true){
				
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				
				// Receive a packet and spin off the handler
				serverSocket.receive(receivePacket);
				System.out.println("Received packet, starting client handler...");
				ClientHandler handler = new ClientHandler(receivePacket, serverSocket);
				
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