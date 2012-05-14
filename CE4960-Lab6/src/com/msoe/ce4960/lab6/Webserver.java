package com.msoe.ce4960.lab6;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Webserver {

	public static void main(String[] args){

		ServerSocket socket;
		try {
			socket = new ServerSocket(8888);

			while(true){
				Socket clientSocket = socket.accept();

				new Thread(new ClientHandler(clientSocket)).start();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

}
