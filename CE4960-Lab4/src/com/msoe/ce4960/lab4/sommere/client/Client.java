package com.msoe.ce4960.lab4.sommere.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.msoe.ce4960.lab4.sommere.shared.SSFTP;

public class Client {

	public static void main(String[] args){

		SSFTP ssftp = new SSFTP("file (2).txt", 985, 100);

		DatagramSocket clientSocket;
		try {
			clientSocket = new DatagramSocket();
			InetAddress ipAddress = InetAddress.getByName("192.168.1.8");

			DatagramPacket sendPacket = new DatagramPacket(ssftp.toBytes(), ssftp.toBytes().length, ipAddress, 5000);
			clientSocket.send(sendPacket);
			
			DatagramPacket receivePacket = new DatagramPacket(new byte[1024], 1024);
			clientSocket.receive(receivePacket);
			clientSocket.close();
			
			SSFTP readBack = SSFTP.fromBytes(receivePacket.getData());

			System.out.println("Read Back\nIsRequest:\t\t" + String.valueOf(readBack.isRequest()));
			System.out.println("IsEOF:\t\t\t" + String.valueOf(readBack.isEOF()));
			System.out.println("IsFileNotFound:\t\t" + String.valueOf(readBack.isFileNotFound()));
			System.out.println("IsInvalidRequest:\t" + String.valueOf(readBack.isInvaldRequest()));
			System.out.println("Length:\t\t\t" + String.valueOf(readBack.getLength()));
			System.out.println("Offset:\t\t\t" + String.valueOf(readBack.getOffset()));
			System.out.println("FileName:\t\t" + String.valueOf(readBack.getFileName()));

			if(readBack.getData() != null){
				System.out.println("----------[Begin Data]----------");
				System.out.println(new String(readBack.getData()));
				System.out.println("-----------[End Data]-----------");
			}else{
				System.out.println("No Data");
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

}
