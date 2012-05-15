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

			if(keyvalue.length > 1){
				map.put(keyvalue[0], keyvalue[1]);
			}else{
				map.put(keyvalue[0], null);
			}
		}

		displayOutput(map, os);
	}

	private void displayOutput(Map<String, String> map, DataOutputStream os) throws IOException {
		
		StringBuilder responseBuilder = new StringBuilder();
		responseBuilder.append("HTTP/1.1 200 OK\n");
		responseBuilder.append("Content-Type: text/html; charset=utf-8\n");
		
		
		StringBuilder bodyBuilder = new StringBuilder();
		bodyBuilder.append("<p><strong>Text Input</strong>: " + map.get("textarea"));
		bodyBuilder.append("<p><strong>Radio Button Choice</strong>: " + map.get("radio-choice-1"));
		bodyBuilder.append("<p><strong>Dropdown Choice</strong>: " + map.get("select-choic"));
		bodyBuilder.append("<p><strong>Text Area</strong>: " + map.get("textarea"));
		bodyBuilder.append("<p><strong>Value is checked</strong>: " + (map.containsKey("checkbox") ? (map.get("checkbox").equals("on") ? "Yes" : "No") : "No"));
		
		responseBuilder.append("Content-Length: " + bodyBuilder.toString().getBytes().length + "\n\n");
		
		os.write(responseBuilder.toString().getBytes());
		os.write(bodyBuilder.toString().getBytes());
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
