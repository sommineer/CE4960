package com.msoe.ce4960.lab4.sommere;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
		
		System.out.println("---[Received Packet]----");
		System.out.println(ssftp.toString());
		System.out.println("--[End Received Packet]--");

		// The number of characters that were read
		int numChars = 0;

		// Data that was read
		byte[] data = null;

		// Get the file name
		String fileName = ssftp.getFileName();

		if(ssftp.isInvaldRequest()){
			// Indicate the packet is a response
			ssftp.setIsResponse(true);

			// Send the response back
			sendResult(ssftp);
			
			System.out.println("----[Sent Response]----");
			System.out.println(ssftp.toString());
			System.out.println("--[End Sent Response]--");
			
			return;
		}
		
		// If the file name is the directory listing, get the directory listing
		if(fileName.equalsIgnoreCase(SSFTP.DIR_LISTING_FILE)){
			data = getDirListing().getBytes();
			numChars = data.length;
			ssftp.setIsEOF(true);
		}else{
			// Otherwise, get the file
			File requestedFile = new File(DEFAULT_DIRECTORY + fileName);

			// Check if the file exists
			if(requestedFile.exists()){
				try {
					// If it does, read the file
					FileInputStream reader = new FileInputStream(requestedFile);

					data = new byte[ssftp.getLength()];

					int offset = (int)ssftp.getOffset();

					// Skip to the offset
					if(reader.skip(offset) != offset){
						ssftp.setIsInvalidRequest(true);
						System.err.println("Offset is past the end of the file");
					}else{

						numChars = reader.read(data);

						// Set the EOF flag if no characters were read, not the
						// expected amount of characters were read, or trying to
						// read the next character returns -1 (exact length)
						ssftp.setIsEOF(
								(numChars == -1) || numChars != ssftp.getLength() 
								|| (reader.read() == -1));
					}
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
				ssftp.setData(data, numChars);
		}

		// Indicate the packet is a response
		ssftp.setIsResponse(true);

		// Send the response back
		sendResult(ssftp);
		
		System.out.println("----[Sent Response]----");
		System.out.println(ssftp.toString());
		System.out.println("--[End Sent Response]--");
	}

	/**
	 * Send the response back to the client
	 * @param ssftp	the response to send back to the client
	 */
	private void sendResult(SSFTP ssftp){

		// Assemble the packet
		int numBytes = ssftp.getNumBytes();
		DatagramPacket returnPacket = new DatagramPacket(new byte[numBytes], 0, numBytes, mReceivePacket.getAddress(), mReceivePacket.getPort());
		returnPacket.setData(ssftp.toBytes());

		try {
			mServerSocket.send(returnPacket);
		} catch (IOException e) {
			System.err.println("An error occured ending the response packet");
			e.printStackTrace();
		}
	}

	/**
	 * Gets the directory listing of the files
	 * @return	the directory listing of the files
	 */
	private String getDirListing(){

		// Get the root directory and validate that it exists
		File rootDir = new File(DEFAULT_DIRECTORY);

		if(!rootDir.exists()){
			throw new IllegalStateException("DEFAULT_DIRECTORY does not exist");
		}else if(!rootDir.isDirectory()){
			throw new IllegalStateException(
					"DEFAULT_DIRECTORY is not a directory");
		}

		// Get the listing of the files
		File[] files = rootDir.listFiles(new FileFilter(){

			@Override
			public boolean accept(File arg0) {
				// Ignore directories
				return !arg0.isDirectory();
			}

		});

		// Build the file list
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

	/**
	 * Directory to pull the files from
	 */
	private static final String DEFAULT_DIRECTORY = "files/";

}
