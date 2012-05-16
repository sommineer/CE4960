package com.msoe.ce4960.lab6;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Parses an HTTP request
 * @author Erik Sommer
 *
 */
public class HTTPRequest {

	/**
	 * Enum used to specify the type of request
	 * @author Erik Sommer
	 *
	 */
	public enum HTTPRequestType{
		/**
		 * A DELETE request
		 */
		DELETE, 

		/**
		 * A GET request
		 */
		GET, 

		/**
		 * A POST request
		 */
		POST, 

		/**
		 * A PUT request
		 */
		PUT;
	}

	/**
	 * Root directory for files
	 */
	private static final String FILE_ROOT = "files/";

	/**
	 * Key for the Accept field in the request
	 */
	private static final String KEY_ACCEPT = "Accept";

	/**
	 * Key for the Accept-Encoding field in the request
	 */
	private static final String KEY_ACCEPT_ENCODING = "Accept-Encoding";

	/**
	 * Key for the Accept-Language field in the request
	 */
	private static final String KEY_ACCEPT_LANGUAGE = "Accept-Language";

	/**
	 * Key for the Cache-Control field in the request
	 */
	private static final String KEY_CACHE_CONTROL = "Cache-Control";

	/**
	 * Key for the Connection field in the request
	 */
	private static final String KEY_CONNECTION = "Connection";

	/**
	 * Key for the Content-Length field in the request
	 */
	private static final String KEY_CONTENT_LENGTH = "Content-Length";

	/**
	 * Key for the Host field in the request
	 */
	private static final String KEY_HOST = "Host";

	/**
	 * Key for the User-Agent field in the request
	 */
	private static final String KEY_USER_AGENT = "User-Agent";

	/**
	 * Creates an {@link HTTPRequest} from the input stream
	 * @param reader	read to read the data from
	 * @return			an {@code HTTPRequest} if it is successfully parsed,
	 * 					{@code null} otherwise
	 * @throws IOException	if there was an I/O error reading the stream
	 */
	public static HTTPRequest fromStream(BufferedReader reader) throws IOException{

		String request;
		HTTPRequest returnRequest = new HTTPRequest();

		// Read the line of the request
		request = reader.readLine();

		// While the line isn't empty (not just "\n")
		while(!request.isEmpty()){

			// Split the request into key-value pairs
			String splitRequest[] = request.split(":");

			if(splitRequest.length == 1){
				// If there isn't a ":" then it's the first line of the request
				// process it
				processRequest(splitRequest[0], returnRequest);
			}else{

				// Separate the key and value
				String key = splitRequest[0];
				String value = splitRequest[1].trim();

				// Look at the key and process it appropriately
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
				}else if(key.equalsIgnoreCase(KEY_CONTENT_LENGTH)){
					returnRequest.mLength = Integer.parseInt(value);
				}
			}

			// Read the next line of data
			request = reader.readLine();
		}

		return returnRequest;
	}

	/**
	 * Processes the request from an HTTP header
	 * @param request		the request to process
	 * @param returnRequest	the {@link HTTPRequest} to fill in
	 */
	private static void processRequest(String request, HTTPRequest returnRequest) {

		// Split the string on spaces and grab the parameters
		String splitString[] = request.split(" ");
		String requestType = splitString[0].trim();
		String fileName = splitString[1].trim();
		String protocol = splitString[2].trim();

		// Set the protocol type
		if(requestType.equals("GET")){
			returnRequest.mRequestType = HTTPRequestType.GET;
		}else if(requestType.equals("POST")){
			returnRequest.mRequestType = HTTPRequestType.POST;
		}

		// If the root file is requested, actually use "index.html"
		if(fileName.equals("/")){
			fileName = "index.html";
		}

		// Set the request file and the protocol
		returnRequest.mFile = new File(FILE_ROOT + fileName);
		returnRequest.mProtocol = protocol;
	}

	/**
	 * Accepted content types of the request
	 */
	private String[] mAccept;

	/**
	 * Accepted encodings of the request
	 */
	private String[] mAcceptEncoding;

	/**
	 * Accepted languages of the request
	 */
	private String[] mAcceptLanguage;



	/**
	 * Cache-control flags of the request
	 */
	private String mCacheControl;

	/**
	 * Connection type of the request
	 */
	private String mConnection;

	/**
	 * Requested file
	 */
	private File mFile;

	/**
	 * Host address of the request
	 */
	private InetAddress mHostAddress;

	/**
	 * Length of the request
	 */
	private int mLength;

	/**
	 * Port for the request
	 */
	private int mPort;

	/**
	 * Protocol for the request
	 */
	private String mProtocol;

	/**
	 * Type of the request
	 */
	private HTTPRequestType mRequestType;

	/**
	 * User agent of the request
	 */
	private String mUserAgent;

	/**
	 * Gets the content-types that are accepted
	 * @return the content-types that are accepted
	 */
	public String[] getAccept() {
		return mAccept;
	}

	/**
	 * Gets the encodings that are accepted
	 * @return the encodings that are accepted
	 */
	public String[] getAcceptEncoding() {
		return mAcceptEncoding;
	}

	/**
	 * Gets the languages that are accepted
	 * @return the languages that are accepted
	 */
	public String[] getAcceptLanguage() {
		return mAcceptLanguage;
	}

	/**
	 * Gets the cache-control flags
	 * @return the cache-control flags
	 */
	public String getCacheControl() {
		return mCacheControl;
	}

	/**
	 * Gets the connection flags
	 * @return the connection flags
	 */
	public String getConnection() {
		return mConnection;
	}

	/**
	 * Gets the file requested
	 * @return	the file requested
	 */
	public File getFile() {
		return mFile;
	}

	/**
	 * Gets the host address
	 * @return the host address
	 */
	public InetAddress getHostAddress() {
		return mHostAddress;
	}

	/**
	 * Gets the length of the request
	 * @return	the length of the request
	 */
	public int getLength() {
		return mLength;
	}

	/**
	 * Gets the port of the request
	 * @return	the port of the request
	 */
	public int getPort() {
		return mPort;
	}

	/**
	 * Gets the protocol of the request
	 * @return the protocol of the request
	 */
	public String getProtocol() {
		return mProtocol;
	}

	/**
	 * Gets the type of request
	 * @return the type of request
	 */
	public HTTPRequestType getRequestType() {
		return mRequestType;
	}

	/**
	 * Gets the user agent
	 * @return the user agent
	 */
	public String getUserAgent() {
		return mUserAgent;
	}

	/**
	 * Sets the content-types that are accepted
	 * @param accept the content-types that are accepted
	 */
	public void setAccept(String[] accept) {
		this.mAccept = accept;
	}

	/**
	 * Sets the encodings that are accepted
	 * @param acceptEncoding the encodings that are accepted
	 */
	public void setAcceptEncoding(String[] acceptEncoding) {
		this.mAcceptEncoding = acceptEncoding;
	}

	/**
	 * Sets the languages that are accepted
	 * @param acceptLanguage	the languages that are accepted
	 */
	public void setAcceptLanguage(String[] acceptLanguage) {
		this.mAcceptLanguage = acceptLanguage;
	}

	/**
	 * Sets the cache-control flags
	 * @param cacheControl	the cache control flags
	 */
	public void setCacheControl(String cacheControl) {
		this.mCacheControl = cacheControl;
	}

	/**
	 * Sets the connection flags
	 * @param connection	the connection flags 
	 */
	public void setConnection(String connection) {
		this.mConnection = connection;
	}

	/**
	 * Sets the file requested
	 * @param file	the file requested
	 * @throws IllegalArgumentException	if {@code file} is {@code null}
	 */
	public void setFile(File file) {

		// Parameter validation
		if(file == null){
			throw new IllegalArgumentException("file is null");
		}

		this.mFile = file;
	}

	/**
	 * Sets the host address
	 * @param hostAddress	the host address
	 */
	public void setHostAddress(InetAddress hostAddress) {
		this.mHostAddress = hostAddress;
	}

	/**
	 * Sets the length of the request
	 * @param length	the length of the request
	 * @throws IllegalArgumentException	if {@code length} is less than 0
	 */
	public void setLength(int length) {

		// Parameter validation
		if(length < 0){
			throw new IllegalArgumentException("length is less than 0");
		}

		this.mLength = length;
	}

	/**
	 * Sets the port of the request
	 * @param port	the port of the request
	 * @throws IllegalArgumentException	if {@code port} is not between 0 and 
	 * 									65535
	 */
	public void setPort(int port) {

		// Parameter validation
		if((port < 0) || (port > 65535)){
			throw new IllegalArgumentException("port is not between 0 and 65535");
		}
		this.mPort = port;
	}

	/**
	 * Sets the protocol of the request
	 * @param protocol	the protocol of the request
	 * @throws IllegalArgumentException	if {@code protocol} is {@code null} or
	 * 									empty 
	 */
	public void setProtocol(String protocol) {

		// Parameter validation
		if((protocol == null) || protocol.isEmpty()){
			throw new IllegalArgumentException("protocol is null or empty");
		}

		this.mProtocol = protocol;
	}

	/**
	 * Sets the type of request
	 * @param requestType	the type of the request
	 * @throws IllegalArgumentException if {@code requestType} is {@code null}
	 */
	public void setRequestType(HTTPRequestType requestType) {

		// Parameter validation
		if(requestType == null){
			throw new IllegalArgumentException("requestType is null");
		}

		this.mRequestType = requestType;
	}


	/**
	 * Sets the user agent
	 * @param userAgent	the user agent
	 */
	public void setUserAgent(String userAgent) {
		this.mUserAgent = userAgent;
	}
}
