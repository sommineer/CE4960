package com.msoe.ce4960.lab6;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class HTTPRequest {

	public static HTTPRequest fromStream(InputStream is) throws IOException{
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		
		String request;
		
		request = reader.readLine();
		
		HTTPRequest returnRequest = new HTTPRequest();
		
		while(!request.isEmpty()){
			String splitRequest[] = request.split(":");
			
			if(splitRequest.length == 1){
				processRequest(splitRequest[0], returnRequest);
			}else{
				
				String key = splitRequest[0];
				String value = splitRequest[1].trim();
				
				if(key.equalsIgnoreCase(KEY_HOST)){
					returnRequest.mHostAddress = InetAddress.getByName(value);
					returnRequest.mPort = Integer.parseInt(splitRequest[2]);
				}else if(key.equalsIgnoreCase(KEY_USER_AGENT)){
					StringBuilder builder = new StringBuilder();
					
					boolean first = true;
					for(int i = 1; i < splitRequest.length; i++){
						
						if(!first){
							builder.append(":");
						}else{
							first = false;
						}
						
						builder.append(splitRequest[i]);
					}
					returnRequest.mUserAgent = builder.toString();
				}else if(key.equalsIgnoreCase(KEY_ACCEPT)){
					returnRequest.mAccept = value.split(",");
				}else if(key.equalsIgnoreCase(KEY_ACCEPT_LANGUAGE)){
					returnRequest.mAcceptLanguage = value.split(",");
				}else if(key.equalsIgnoreCase(KEY_ACCEPT_ENCODING)){
					returnRequest.mAcceptEncoding = value.split(",");
				}else if(key.equalsIgnoreCase(KEY_CONNECTION)){
					returnRequest.mConnection = value;
				}else if(key.equalsIgnoreCase(KEY_CACHE_CONTROL)){
					returnRequest.mCacheControl = value;
				}
			}
			
			request = reader.readLine();
		}
		
		return returnRequest;
	}
	
	private static void processRequest(String string, HTTPRequest returnRequest) {
		String splitString[] = string.split(" ");
		String requestType = splitString[0].trim();
		String fileName = splitString[1].trim();
		String protocol = splitString[2].trim();
		
		if(requestType.equals("GET")){
			returnRequest.mRequestType = HTTPRequestType.GET;
		}else if(requestType.equals("POST")){
			returnRequest.mRequestType = HTTPRequestType.POST;
		}
		
		if(fileName.equals("/")){
			fileName = "index.html";
		}
		
		returnRequest.file = new File(fileName);
		returnRequest.mProtocol = protocol;
	}

	public int mPort;
	public String mProtocol;
	public HTTPRequestType mRequestType;
	public InetAddress mHostAddress;
	public String mUserAgent;
	public String[] mAccept;
	public String[] mAcceptLanguage;
	public String[] mAcceptEncoding;
	public String mConnection;
	public String mCacheControl;
	public File file;
	
	private static final String KEY_HOST = "Host";
	private static final String KEY_USER_AGENT = "User-Agent";
	private static final String KEY_ACCEPT = "Accept";
	private static final String KEY_ACCEPT_LANGUAGE = "Accept-Language";
	private static final String KEY_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String KEY_CONNECTION = "Connection";
	private static final String KEY_CACHE_CONTROL = "Cache-Control";

	
	
	
	
	public enum HTTPRequestType{
		GET, POST;
	}
}
