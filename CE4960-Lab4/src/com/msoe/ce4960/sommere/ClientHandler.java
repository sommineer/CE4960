package com.msoe.ce4960.sommere;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ClientHandler implements Runnable {

	private DatagramPacket mReceivePacket;
	private DatagramSocket mServerSocket;

	public ClientHandler(DatagramPacket mReceivePacket, DatagramSocket mServerSocket){

		this.mReceivePacket = mReceivePacket;
		this.mServerSocket = mServerSocket;
	}

	@Override
	public void run() {

		SSFTP ssftp = SSFTP.fromBytes(mReceivePacket.getData());

		if(ssftp == null){
			System.err.println("Error parsing");
		}

		int numChars = 0;
		String data = null;

		if(ssftp.getFileName().equalsIgnoreCase(DIR_LISTING_FILE)){
			data = getDirListing();
			numChars = data.length();
			ssftp.setIsEOF(true);
		}else{
			File requestedFile = new File(ssftp.getFileName());

			if(requestedFile.exists()){
				try {
					BufferedReader reader = new BufferedReader(new FileReader(requestedFile));
					reader.skip(ssftp.getOffset());

					char returnChars[] = new char[ssftp.getLength()];
					numChars = reader.read((char[])returnChars, (int)ssftp.getOffset(), ssftp.getLength());
					data = new String(returnChars);
					ssftp.setIsEOF((numChars == -1) || numChars != ssftp.getLength() || (reader.read() == -1));
				} catch (FileNotFoundException e) {
					ssftp.setIsFileNotFound(true);
				} catch (IOException e) {
					ssftp.setIsInvalidRequest(true);
				}
			}else{
				ssftp.setIsFileNotFound(true);
			}
		}

		ssftp.setLength((numChars != -1) ? numChars : 0);

		if(data != null){
			try {
				ssftp.setData(data.getBytes("UTF-8"));

			} catch (UnsupportedEncodingException e) {
				ssftp.setIsInvalidRequest(true);
			}
		}

		sendResult(ssftp);
	}

	private void sendResult(SSFTP ssftp){
		DatagramPacket returnPacket = new DatagramPacket(ssftp.getData(), 0, ssftp.getNumBytes(), mReceivePacket.getAddress(), mReceivePacket.getPort());
		returnPacket.setData(ssftp.toBytes());

		try {
			mServerSocket.send(returnPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
