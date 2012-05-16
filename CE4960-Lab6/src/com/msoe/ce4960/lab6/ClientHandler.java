package com.msoe.ce4960.lab6;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.msoe.ce4960.lab6.HTTPRequest.HTTPRequestType;

/**
 * Handles the client requests that are sent to the server
 * @author Erik Sommer
 *
 */
public class ClientHandler implements Runnable {

	/**
	 * Size of the buffer to use when writing out the file
	 */
	private static final int BUFFER_SIZE = 1024;

	/**
	 * Socket that the client is connected from
	 */
	Socket mSocket;

	/**
	 * Constructor
	 * @param socket	the socket that the client is connected form
	 * @throws IllegalArgumentException	if {@code socket} is {@code null}
	 */
	public ClientHandler(Socket socket){

		// Parameter validation
		if(socket == null){
			throw new IllegalArgumentException("socket is null");
		}

		mSocket = socket;
	}

	/**
	 * Displays the POST output in a human-readable form
	 * @param map	the map that contains the post data
	 * @param os	the output stream to write the data to
	 * @throws IOException	if there was an error getting the output stream
	 */
	private void displayOutput(Map<String, String> map, DataOutputStream os) 
			throws IOException {

		// Build the response header
		StringBuilder responseBuilder = new StringBuilder();
		responseBuilder.append("HTTP/1.1 200 OK\n");
		responseBuilder.append("Content-Type: text/html; charset=utf-8\n");

		// Build the response body
		StringBuilder bodyBuilder = new StringBuilder();
		bodyBuilder.append("<p><strong>Text Input</strong>: " 
				+ map.get("textarea"));
		bodyBuilder.append("<p><strong>Radio Button Choice</strong>: " 
				+ map.get("radio-choice-1"));
		bodyBuilder.append("<p><strong>Dropdown Choice</strong>: " 
				+ map.get("select-choic"));
		bodyBuilder.append("<p><strong>Text Area</strong>: "
				+ map.get("textarea"));
		bodyBuilder.append("<p><strong>Value is checked</strong>: " 
				+ (map.containsKey("checkbox") ? (map.get("checkbox").equals("on") ? "Yes" : "No") : "No"));

		// Set the response header content-length
		responseBuilder.append("Content-Length: " + bodyBuilder.toString().getBytes().length + "\n\n");

		// Write the header followed by the content
		os.write(responseBuilder.toString().getBytes());
		os.write(bodyBuilder.toString().getBytes());
	}

	/**
	 * Processes a GET request for a file
	 * @param request	the request
	 * @param os		output stream to write data to
	 * @throws IOException	if there was an error reading or witing to the streams
	 */
	private void processGET(HTTPRequest request, OutputStream os) throws IOException {

		// Log that the server is processing a GET request
		System.out.println("Sending get");

		StringBuilder responseBuilder = new StringBuilder();

		File requestFile = request.getFile();

		if(!requestFile.exists()){
			// Send a 404 header if the file is not found
			responseBuilder.append("HTTP/1.1 404 FILE NOT FOUND\n");
			responseBuilder.append("Content-Type: text/html; charset=utf-8\n");
			responseBuilder.append("Content-Length: 0\n\n");
		}else{
			// Send the 200 header if the file is found
			responseBuilder.append("HTTP/1.1 200 OK\n");
			responseBuilder.append("Content-Type: text/html; charset=utf-8\n");
			responseBuilder.append("Content-Length: " + requestFile.length() + "\n\n");
		}

		// Write the header
		byte respBuff[] = responseBuilder.toString().getBytes();
		os.write(respBuff);

		if(!requestFile.exists()){
			// Don't return any more data if the file doesn't exist
			return;
		}

		// Open the file for reading and write out the data
		FileInputStream reader = new FileInputStream(requestFile);

		byte buff[] = new byte[BUFFER_SIZE];
		int numRead = reader.read(buff);;

		while(numRead > 0){

			os.write(buff, 0, numRead);
			numRead = reader.read(buff);
		}
	}

	/**
	 * Processes a POST request
	 * @param request	the request that was received
	 * @param reader	reader to read data from
	 * @param os		output to write results to
	 * @throws IOException	if there was an error reading or writing to the streams
	 */
	private void processPOST(HTTPRequest request, BufferedReader reader, 
			DataOutputStream os) throws IOException {

		// Log that the server is processing a POST request
		System.out.println("Processing post");

		// Read in the amount of data specified in the request
		char buff[] = new char[request.getLength()];
		reader.read(buff);

		// Split the POST data into key-value pairs
		String params[] = (new String(buff)).split("&");
		Map<String, String> map = new HashMap<String, String>();

		for(String param : params){
			String keyvalue[] = param.split("=");

			if(keyvalue.length > 1){
				map.put(keyvalue[0], keyvalue[1]);
			}else{
				map.put(keyvalue[0], null);
			}
		}

		// Display the values
		displayOutput(map, os);
	}

	/**
	 * Executed by the thread to handle the request
	 */
	@Override
	public void run() {

		try {
			// Read and parse the request
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					mSocket.getInputStream()));
			HTTPRequest request = HTTPRequest.fromStream(reader);

			// Create an output stream and process the request
			DataOutputStream os = new DataOutputStream(mSocket.getOutputStream());
			HTTPRequestType requestType = request.getRequestType();

			if(requestType == HTTPRequestType.GET){
				processGET(request, os);
			}else if(requestType == HTTPRequestType.POST){
				processPOST(request, reader, os);
			}else{
				System.err.println("Unsupported request type received");
			}

			// Flush and close the output stream.
			os.flush();
			os.close();

			// Close the connection once transmission is complete
			mSocket.close();
		} catch (IOException e) {
			System.err.println("I/O exception occured reading data");
			e.printStackTrace();
		}

	}

}
