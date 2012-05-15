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

public class ClientHandler implements Runnable {

	@Override
	public void run() {


		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
			HTTPRequest request = HTTPRequest.fromStream(reader);

			DataOutputStream os = new DataOutputStream(mSocket.getOutputStream());

			if(request.mRequestType == HTTPRequestType.GET){
				processGET(request, os);
			}else if(request.mRequestType == HTTPRequestType.POST){
				processPOST(request, reader, os);
			}

			os.flush();
			os.close();

			mSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void processPOST(HTTPRequest request, BufferedReader reader, DataOutputStream os) throws IOException {
		System.out.println("Processing post");
		
		char buff[] = new char[request.mLength];
		
		reader.read(buff);
		
		System.out.println(buff);
		
		String params[] = (new String(buff)).split("&");
		Map<String, String> map = new HashMap<String, String>();
		
		for(String param : params){
			String keyvalue[] = param.split("=");
			map.put(keyvalue[0], keyvalue[1]);
		}
		
		System.out.println(map);
		System.out.println(map.keySet());
		System.out.println(map.entrySet());
	}

	private void processGET(HTTPRequest request, OutputStream os) throws IOException {
		System.out.println("Sending get");

		StringBuilder responseBuilder = new StringBuilder();

		if(!request.file.exists()){
			responseBuilder.append("HTTP/1.1 404 FILE NOT FOUND\n");
			responseBuilder.append("Content-Type: text/html; charset=utf-8\n");
			responseBuilder.append("Content-Length: 0\n\n");
		}else{
			responseBuilder.append("HTTP/1.1 200 OK\n");
			responseBuilder.append("Content-Type: text/html; charset=utf-8\n");
			responseBuilder.append("Content-Length: " + request.file.length() + "\n\n");
		}

		byte respBuff[] = responseBuilder.toString().getBytes();
		os.write(respBuff);

		if(!request.file.exists()){
			return;
		}

		if(request.file.getName().equals(INPUT_PROCESSOR)){
			os.write("PROCESSED!".getBytes());
		}else{
			FileInputStream reader = new FileInputStream(request.file);

			byte buff[] = new byte[1024];

			int numRead = reader.read(buff);;

			while(numRead > 0){

				os.write(buff, 0, numRead);
				numRead = reader.read(buff);
			}
		}
	}

	Socket mSocket;

	public ClientHandler(Socket socket){
		mSocket = socket;
	}

	private static final String INPUT_PROCESSOR = (new File("files/process")).getName();
}
