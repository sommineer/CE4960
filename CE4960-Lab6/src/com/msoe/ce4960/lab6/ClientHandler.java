package com.msoe.ce4960.lab6;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.msoe.ce4960.lab6.HTTPRequest.HTTPRequestType;

public class ClientHandler implements Runnable {

	@Override
	public void run() {


		try {
			HTTPRequest request = HTTPRequest.fromStream(mSocket.getInputStream());
	
			BufferedWriter os = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));

			if(request.mRequestType == HTTPRequestType.GET){
				processGET(request, os);
			}

			os.flush();
			os.close();
			
			mSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void processGET(HTTPRequest request, BufferedWriter os) throws IOException {
		System.out.println("Sending get");
		os.write("HTTP/1.1 200 OK\n");
		os.write("Content-Type: text/html; charset=utf-8\n");
		
		
		StringBuilder builder = new StringBuilder();
		builder.append("You requested the file: \"" + request.file.getName() + "\"<br />");
		builder.append("That file " + (request.file.exists() ? "does" : "does not") + " exist<br />");
		builder.append("Your browser agent is: " + request.mUserAgent + "<br />");
		
		String response = builder.toString();
		
		os.write("Content-Length: " + response.length() + "\n\n");
		os.write(response);
	}

	Socket mSocket;

	public ClientHandler(Socket socket){
		mSocket = socket;
	}

}
