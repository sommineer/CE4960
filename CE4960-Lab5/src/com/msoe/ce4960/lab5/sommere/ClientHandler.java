package com.msoe.ce4960.lab5.sommere;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;

import javax.swing.text.NumberFormatter;

import com.msoe.ce4960.lab5.sommere.shared.SSFTP;

/**
 * Handles the processing of a client when they connect to the server
 * @author Erik Sommer
 *
 */
public class ClientHandler implements Runnable {

	/**
	 * Socket to send the response back to
	 */
	private Socket mClientSocket;
	
	/**
	 * Input stream that data is read from
	 */
	private DataInputStream mInputStream;
	
	/**
	 * Output stream that data is written to
	 */
	private DataOutputStream mOutputStream;

	/**
	 * Constructor
	 * @param clientSocket	Socket the client is connected to
	 * @throws IOException	If there was an error getting the I/O streams
	 * @throws IllegalArgumentException	if {@code clientSocket} is {@code null}
	 */
	public ClientHandler(Socket clientSocket) throws IOException{

		// Argument Validation
		if(clientSocket == null){
			throw new IllegalArgumentException("clientSocket is null");
		}

		this.mClientSocket = clientSocket;
		this.mInputStream = new DataInputStream(clientSocket.getInputStream());
		this.mOutputStream = 
				new DataOutputStream(clientSocket.getOutputStream());
	}

	/**
	 * Gets the packet of data from the client
	 * @return	{@code true} if packet was successfully read and parsed,
	 * 			{@code false} otherwise
	 */
	private boolean getPacket(){

		boolean returnValue = false;
		DataInputStream dataInput = null;
		
		try{
			// Get the input stream
			dataInput = mInputStream;

			byte clientData[] = new byte[SSFTP.NUM_HEADER_BYTES];

			// Read the data and parse it
			if(dataInput.read(clientData) == SSFTP.NUM_HEADER_BYTES){
				mSSFTP = SSFTP.fromBytes(clientData);

				if(mSSFTP != null){
					returnValue = true;
				}
			}
		}catch(IOException e){
			returnValue = false;
			System.err.println("Error occured reading the data");
		}

		return returnValue;
	}

	/**
	 * Holds the client's request data
	 */
	private SSFTP mSSFTP;

	/**
	 * Executes the client request
	 */
	@Override
	public void run() {

		// Get and parse the request packet, returning invalid request if
		// it can't be parsed
		if(!getPacket() || mSSFTP.isInvaldRequest()){
			mSSFTP.setIsInvalidRequest(true);
			mSSFTP.setIsResponse(true);
			sendData(mSSFTP.toBytes());
			return;
		}

		// Create a local reference
		SSFTP ssftp = mSSFTP;
		
		// Indicate it is an response
		ssftp.setIsResponse(true);

		// Get the file name
		String fileName = ssftp.getFileName();

		// Init the variables needed to hold the data info
		byte[] data = null;
		int numChars = -1;
		
		
		// If the file name is the directory listing, get the directory listing
		if(fileName.equalsIgnoreCase(SSFTP.DIR_LISTING_FILE)){
			data = getDirListing().getBytes();
			numChars = data.length;
			ssftp.setIsEOF(true);
			
			// Indicate the packet is a response
			ssftp.setIsResponse(true);

			// Send the response and data back
			sendData(ssftp.toBytes());
			
			if(data != null){
				sendData(data);
			}
			
		}else{
			
			long startTime = System.nanoTime();
			
			// Otherwise, get the file
			File requestedFile = new File(DEFAULT_DIRECTORY + fileName);

			// Check if the file exists
			if(requestedFile.exists()){
				try {
					// Indicate the packet is a response
					ssftp.setIsResponse(true);

					// Send the response and data back
					sendData(ssftp.toBytes());
					
					// If it does, read the file
					FileInputStream reader = new FileInputStream(requestedFile);

					data = new byte[ssftp.getLength()];
					
					int numRead = 0;
					int total = 0;
					
					while((numRead = reader.read(data)) != -1){
						sendData(data, numRead);
						total += numRead;
					}
					
					System.out.println("Num Bytes Read: " + total);

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
				
				// Indicate the packet is a response
				ssftp.setIsResponse(true);

				// Send the response and data back
				sendData(ssftp.toBytes());
				
			}
			
			long delta = System.nanoTime() - startTime;
			
			System.out.println("Time to transfer: " + delta + "ns");
		}
		
		System.out.println("----[Sent Response]----");
		System.out.println(ssftp.toString());
		System.out.println("--[End Sent Response]--");
		
		// Close the streams
		try {
			mInputStream.close();
		} catch (IOException e) {
			System.err.println(
					"An error was encountered closing the input stream");
		}
		
		try {
			mOutputStream.close();
		} catch (IOException e) {
			System.err.println(
					"An error was encountered closing the output stream");
		}
		
		try {
			mClientSocket.close();
		} catch (IOException e) {
			System.err.println(
					"An error was encountered closing the client socket");
		}
	}

	private void sendData(byte[] data){
		sendData(data, data.length);
	}
	
	/**
	 * Sends the data to the client
	 * @param data	the array of data to send to the client
	 */
	private void sendData(byte[] data, int numBytes){
		
		try {
			DataOutputStream output = mOutputStream;
			
			int currPos = 0;
			int packetSize = mSSFTP.getLength();
			int numLeft = data.length;
			
			// Loop through and send the packets
			while(numLeft != 0){
				
				int writeNum = (numLeft > packetSize) ? packetSize : numLeft;
				
				output.write(data, currPos, writeNum);
				numLeft -= writeNum;
			}
			
		}catch (IOException e){
			System.err.println("I/O Exception occured sending the data");
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
