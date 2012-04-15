package com.msoe.ce4960.lab4.sommere;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.msoe.ce4960.lab4.sommere.shared.SSFTP;

/**
 * Handles the processing of a client when they connect to the server
 * @author Erik Sommer
 *
 */
public class ClientHandler implements Runnable {

	/**
	 * Packet that was received
	 */
	private DatagramPacket mReceivePacket;

	/**
	 * Socket to send the response back to
	 */
	private DatagramSocket mServerSocket;

	/**
	 * Constructor
	 * @param receivePacket	packet to process
	 * @param serverSocket		socket to send the response back to
	 * @throws IllegalArgumentException	if {@code receivePacket} or 
	 * 									{@code serverSocket} are {@code null}
	 */
	public ClientHandler(DatagramPacket receivePacket, DatagramSocket serverSocket){

		// Argument Validation
		if(receivePacket == null){
			throw new IllegalArgumentException("receivePacket is null");
		}else if(serverSocket == null){
			throw new IllegalArgumentException("serverSocket is null");
		}

		this.mReceivePacket = receivePacket;
		this.mServerSocket = serverSocket;
	}

	/**
	 * Executes the client request
	 */
	@Override
	public void run() {

		// Create the {@link SSFTP} object
		SSFTP ssftp = SSFTP.fromBytes(mReceivePacket.getData());

		// If there was an error receiving the data, stop processing
		if(ssftp == null){
			System.err.println("Error parsing client packet");
			return;
		}

		// The number of characters that were read
		int numChars = 0;
		
		// Data that was read
		String data = null;

		// Get the file name
		String fileName = ssftp.getFileName();

		// If the file name is the directory listing, get the directory listing
		if(fileName.equalsIgnoreCase(DIR_LISTING_FILE)){
			data = getDirListing();
			numChars = data.length();
			ssftp.setIsEOF(true);
		}else{
			// Otherwise, get the file
			File requestedFile = new File(fileName);

			// Check if the file exists
			if(requestedFile.exists()){
				try {
					// If it does, skip to the offset and read the file
					BufferedReader reader = new BufferedReader(new FileReader(requestedFile));
					reader.skip(ssftp.getOffset());

					char returnChars[] = new char[ssftp.getLength()];
					numChars = reader.read(returnChars, (int)ssftp.getOffset(), ssftp.getLength());
					data = new String(returnChars);

					// Set the EOF flag if no characters were read, not the
					// expected amount of characters were read, or trying to
					// read the next character returns -1 (exact length)
					ssftp.setIsEOF(
							(numChars == -1) || numChars != ssftp.getLength() 
							|| (reader.read() == -1));
				} catch (FileNotFoundException e) {
					ssftp.setIsFileNotFound(true);
				} catch (IOException e) {
					System.err.println("An error occured reading the file");
					ssftp.setIsInvalidRequest(true);
				}
			}else{
				ssftp.setIsFileNotFound(true);
			}
		}

		// Set the length
		ssftp.setLength((numChars != -1) ? numChars : 0);

		// Set the data, if there is any
		if(data != null){
			try {
				ssftp.setData(data.getBytes("UTF-8"));

			} catch (UnsupportedEncodingException e) {
				System.err.println("An error occured encoding the data");
				ssftp.setIsInvalidRequest(true);
			}
		}

		// Indicate the packet is a response
		ssftp.setIsRequest(false);
		
		// Send the response back
		sendResult(ssftp);
	}

	/**
	 * Send the response back to the client
	 * @param ssftp	the response to send back to the client
	 */
	private void sendResult(SSFTP ssftp){
		
		// Assemble the packet
		DatagramPacket returnPacket = new DatagramPacket(ssftp.toBytes(), 0, ssftp.getNumBytes(), mReceivePacket.getAddress(), mReceivePacket.getPort());
		returnPacket.setData(ssftp.toBytes());

		try {
			mServerSocket.send(returnPacket);
		} catch (IOException e) {
			System.err.println("An error occured ending the response packet");
			e.printStackTrace();
		}
	}

	private String getDirListing(){

		File rootDir = new File(DEFAULT_DIRECTORY);

		File[] files = rootDir.listFiles(new FileFilter(){

			@Override
			public boolean accept(File arg0) {
				return !arg0.isDirectory();
			}

		});

		StringBuilder builder = new StringBuilder();
		boolean first = true;

		for(File file : files){

			if(!first){
				builder.append("\n");
			}else{
				first = false;
			}

			builder.append(file.getName());
		}

		return builder.toString();
	}

	private static final String DIR_LISTING_FILE = ".";
	private static final String DEFAULT_DIRECTORY = "";

}
