package com.msoe.ce4960.lab4.sommere.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.msoe.ce4960.lab4.sommere.shared.SSFTP;

public class Client {

	public static void main(String[] args){

		SSFTP ssftp = new SSFTP("test.txt", 1000, 0);

		Socket clientSocket;
		try {
			clientSocket = new Socket(InetAddress.getByName("192.168.1.8"), 22222);

			DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
			outputStream.write(ssftp.toBytes());

			byte[] responsePacket = new byte[SSFTP.NUM_HEADER_BYTES];

			DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
			inputStream.read(responsePacket);

			// Create the SSFTP object and validate it
			SSFTP responsePakcet = SSFTP.fromBytes(responsePacket);

			if(responsePakcet.isInvaldRequest()){
				System.err.println("The received packet is an invalid request");
				return;
			}else if(responsePakcet.isFileNotFound()){
				System.err.println("The file was not found");
				return;
			}

			// The response is as expected, start reading the file
			FileOutputStream fileOutput = new FileOutputStream(FILE_DIR + ssftp.getFileName());
			DataInputStream networkInput = new DataInputStream(clientSocket.getInputStream());

			byte data[] = new byte[ssftp.getLength()];


			int numBytes = networkInput.read(data);

			try{
				while(numBytes != -1){
					fileOutput.write(data, 0, numBytes);
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
			
			try{
				fileOutput.close();
			}catch (IOException e){
				System.err.println(
						"An error was encountered closing the file output stream");
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

	}

	/**
	 * Directory to store files into
	 */
	private static final String FILE_DIR = "rec/";

}
