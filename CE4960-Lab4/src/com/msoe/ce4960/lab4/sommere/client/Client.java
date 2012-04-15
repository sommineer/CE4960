package com.msoe.ce4960.lab4.sommere.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

import com.msoe.ce4960.lab4.sommere.shared.SSFTP;

public class Client {

	public static void main(String[] args){

		SSFTP ssftp = new SSFTP("123456789123123456789123456.txt", 10, 5000);
		ssftp.setIsEOF(false);
		ssftp.setIsFileNotFound(false);
		ssftp.setIsInvalidRequest(false);
		ssftp.setIsRequest(true);
		
		Random gen = new Random();
		
		int num = gen.nextInt()%10000;
		
		if(num < 0){
			num *= -1;
		}
		
		byte[] sampleData = new byte[num];
		gen.nextBytes(sampleData);
		
		ssftp.setData(sampleData);
		byte[] dataBytes = ssftp.toBytes();
		
		

		for(int i = 0; i < SSFTP.NUM_HEADER_BYTES; i++){
			System.out.println(String.valueOf(i) + ":\t0x" + Integer.toHexString(Integer.parseInt(String.valueOf(dataBytes[i]))));
		}


		/*SSFTP readBack = SSFTP.fromBytes(dataBytes);

		System.out.println("Read Back\nIsRequest:\t\t" + String.valueOf(readBack.isRequest()));
		System.out.println("IsEOF:\t\t\t" + String.valueOf(readBack.isEOF()));
		System.out.println("IsFileNotFound:\t\t" + String.valueOf(readBack.isFileNotFound()));
		System.out.println("IsInvalidRequest:\t" + String.valueOf(readBack.isInvaldRequest()));
		System.out.println("Length:\t\t\t" + String.valueOf(readBack.getLength()));
		System.out.println("Offset:\t\t\t" + String.valueOf(readBack.getOffset()));
		System.out.println("FileName:\t\t" + String.valueOf(readBack.getFileName()));

		if(readBack.getData() != null){
			System.out.println("SizeOfDataArray:\t\t" + String.valueOf(readBack.getData().length));
			
			byte[] verify = readBack.getData();
			
			boolean verified = true;
			
			for(int i = 0; i < verify.length; i++){
				if(sampleData[i] != verify[i]){
					System.out.println("verification failed at byte " + String.valueOf(i));
					verified = false;
					break;
				}
			}
			
			if(!verified){
				System.out.println("Verification failed");
			}else{
				System.out.println("Data OK!");
			}
		}else{
			System.out.println("No Data");
		}*/


		
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
				System.out.println("SizeOfDataArray:\t\t" + String.valueOf(readBack.getData().length));
				
				byte[] verify = readBack.getData();
				
				boolean verified = true;
				
				for(int i = 0; i < verify.length; i++){
					if(sampleData[i] != verify[i]){
						System.out.println("verification failed at byte " + String.valueOf(i));
						verified = false;
						break;
					}
				}
				
				if(!verified){
					System.out.println("Verification failed");
				}else{
					System.out.println("Data OK!");
				}
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
