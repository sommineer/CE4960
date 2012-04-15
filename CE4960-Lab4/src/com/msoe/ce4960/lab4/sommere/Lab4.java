package com.msoe.ce4960.lab4.sommere;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Lab4 {

	public static void main(String[] args){

		if(args.length < 2){
			System.out.println("Usage: Lab4 ServerIP Port");
			return;
		}

		int port;
		try{
			port = Integer.parseInt(args[1]);
		}catch(NumberFormatException e){
			System.out.println("Usage: Lab4 ServerIP Port");
			return;
		}

		try {
			DatagramSocket serverSocket = new DatagramSocket(port);

			byte[] receiveData = new byte[1024];

			while(true){

				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				
				ClientHandler handler = new ClientHandler(receivePacket, serverSocket);
				
				(new Thread(handler)).start();
			}

		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}
}