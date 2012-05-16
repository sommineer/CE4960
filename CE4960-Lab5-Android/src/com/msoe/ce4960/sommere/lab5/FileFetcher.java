package com.msoe.ce4960.sommere.lab5;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;

/**
 * AsyncTask that fetches the file from the server
 * @author Erik Sommer
 *
 */
public class FileFetcher extends AsyncTask<String, Void, byte[]> {
	
	/**
	 * Name of the file that is downloaded
	 */
	private String mFileName;

	/**
	 * Interface used to handle when the file has been fetched
	 * @author Erik Sommer
	 *
	 */
	public interface FileFetcherListener{
		
		/**
		 * Handles when the file has been fetched
		 * @param fileName	the name of the file
		 * @param data		the data contained in the file
		 */
		public void onFileFetched(String fileName, byte[] data);
	
	}
	
	/**
	 * Address of the server
	 */
	private InetAddress serverAddress;
	
	/**
	 * Listener to call back to when the data has been downloaded
	 */
	private FileFetcherListener mListener;
	
	
	/**
	 * Constructor
	 * @param listener		listener to call back to when the data has been fetched
	 * @param serverAddress	server to get the data from
	 */
	public FileFetcher(FileFetcherListener listener, InetAddress serverAddress){
		mListener = listener;
		this.serverAddress = serverAddress;
	}
	
	/**
	 * Port to connect to on the server
	 */
	private static final int SERVER_PORT = 22222;
	
	/**
	 * Size of the request
	 */
	private static final int REQUEST_SIZE = 5000;
	
	/**
	 * Fetches the file
	 * @param params	first parameter should be the file name.  All others
	 * 					are ignored
	 * @return	a {@code byte[]} array containing the file contents
	 */
	@Override
	protected byte[] doInBackground(String... params) {
		
		// Grab the file name and create a new packet
		String fileName = params[0];
		SSFTP ssftp = new SSFTP(fileName, REQUEST_SIZE, 0);

		
		Socket clientSocket;
		byte data[] = null;
		
		try{
			// Write out the request
			clientSocket = new Socket(serverAddress, SERVER_PORT);
			DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
			outputStream.write(ssftp.toBytes());

			// Read in the response
			byte[] responsePacket = new byte[SSFTP.NUM_HEADER_BYTES];
			DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
			inputStream.read(responsePacket);

			// Create the SSFTP object and validate it
			SSFTP responsePakcet = SSFTP.fromBytes(responsePacket);
			
			// Validate the response
			if(responsePakcet.isInvalidRequest()){
				Log.w(getClass().getName(), "Request is invalid");
				return null;
			}else if(responsePakcet.isFileNotFound()){
				Log.w(getClass().getName(), "File was not found");
				return null;
			}

			// The response is as expected, start reading the file
			DataInputStream networkInput = new DataInputStream(clientSocket.getInputStream());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			
			byte[] buffer = new byte[REQUEST_SIZE];
			int numRead = 0;
			
			// Read in the file data
			while((numRead = networkInput.read(buffer)) != -1){
				out.write(buffer, 0, numRead);
			}
			
			// Convert the stream to an array
			data = out.toByteArray();

			// Close the streams
			try {
				inputStream.close();
			}catch (IOException e){
				Log.e(getClass().getName(), "Error closing the input stream", e);
			}

			try {
				networkInput.close();
			}catch (IOException e){
				Log.e(getClass().getName(), "Error closing the input stream", e);
			}

			try {
				clientSocket.close();
			}catch (IOException e){
				Log.e(getClass().getName(), "Error closing the input stream", e);
			}
		} catch (SocketException e) {
			Log.e(getClass().getName(), "A socket error occured", e);
		} catch (UnknownHostException e) {
			Log.e(getClass().getName(), "Could not find server", e);
		} catch (IOException e) {
			Log.e(getClass().getName(), "An I/O error occured", e);
		}

		mFileName = fileName;
		
		return data;
	}
	
	/**
	 * Handles when the file has finished downloading.  Notifies the
	 * listener
	 * @param result	the file that was read
	 */
	@Override
	protected void onPostExecute(byte[] result) {
		mListener.onFileFetched(mFileName, result);
	}
}
