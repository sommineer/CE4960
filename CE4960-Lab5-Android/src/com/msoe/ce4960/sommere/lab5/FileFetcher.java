package com.msoe.ce4960.sommere.lab5;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.os.AsyncTask;

public class FileFetcher extends AsyncTask<String, Void, byte[]> {

	/**
	 * Directory to store files into
	 */
	private static final String FILE_DIR = "rec/";
	
	/**
	 * Name of the file that is downloaded
	 */
	private String mFileName;

	public interface FileFetcherListener{
		
		public void onFileFetched(String fileName, byte[] data);
	
	}
	
	private FileFetcherListener mListener;
	
	public FileFetcher(FileFetcherListener listener){
		mListener = listener;
	}
	
	@Override
	protected byte[] doInBackground(String... params) {
		
		String fileName = params[0];
		
		SSFTP ssftp = new SSFTP(fileName, 1000, 0);

		Socket clientSocket;

		byte data[] = null;
		
		try{
			clientSocket = new Socket(InetAddress.getByName("192.168.1.8"), 22222);
			DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
			outputStream.write(ssftp.toBytes());

			byte[] responsePacket = new byte[SSFTP.NUM_HEADER_BYTES];

			DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
			inputStream.read(responsePacket);

			// Create the SSFTP object and validate it
			SSFTP responsePakcet = SSFTP.fromBytes(responsePacket);

			
			if(responsePakcet.isInvalidRequest()){
				System.err.println("The received packet is an invalid request");
				return null;
			}else if(responsePakcet.isFileNotFound()){
				System.err.println("The file was not found");
				return null;
			}

			// The response is as expected, start reading the file
			DataInputStream networkInput = new DataInputStream(clientSocket.getInputStream());

			data = new byte[ssftp.getLength()];

			int numBytes = networkInput.read(data);

			try{
				while(numBytes != -1){
					numBytes = networkInput.read(data);
				}
			}catch(IOException e){
				System.out.println("Server closed connection");
			}

			// Close the streams
			try {
				inputStream.close();
			}catch (IOException e){
				System.err.println(
						"An error was encountered closing the input stream");
			}

			try {
				networkInput.close();
			}catch (IOException e){
				System.err.println(
						"An error was encountered closing the output stream");
			}

			try {
				clientSocket.close();
			}catch (IOException e){
				System.err.println(
						"An error was encountered closing the client socket");
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mFileName = fileName;
		
		return data;
	}
	
	@Override
	protected void onPostExecute(byte[] result) {
		mListener.onFileFetched(mFileName, result);
	}



}
