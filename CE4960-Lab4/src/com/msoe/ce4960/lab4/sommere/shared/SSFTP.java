package com.msoe.ce4960.lab4.sommere.shared;


/**
 * Represents a stateless file server packet
 * @author Erik Sommer
 *
 */
public class SSFTP {
	
	/**
	 * File name used to indicate a directory listing request/response
	 */
	public static final String DIR_LISTING_FILE = ".";
	
	/**
	 * Number of bytes the file name may take up
	 */
	public static final int FILE_NAME_NUM_BYTES = 32;
	
	/**
	 * Bit mask for the EOF flag
	 */
	private static final int IS_EOF_MASK = (1 << 1);
	
	/**
	 * Bit mask for the file not found flag
	 */
	private static final int IS_FILE_NOT_FOUND_MASK = (1 << 2);
	
	/**
	 * Bit mask for the invalid request flag
	 */
	private static final int IS_INVALID_REQUEST_MASK = (1 << 3);
	
	/**
	 * Bit mask for the request flag
	 */
	private static final int IS_REQUEST_MASK = (1 << 0);
	
	/**
	 * The maximum length of the the file name.  Minus 1 is necessary for the
	 * null terminator
	 */
	private static final int MAX_FILE_NAME_LENGTH = FILE_NAME_NUM_BYTES - 1;
	
	/**
	 * Number of bytes in the header
	 */
	public static final int NUM_HEADER_BYTES = 39;
	
	/**
	 * Maximum size of the data that can be returned
	 */
	private static final int MAX_LENGTH = 1400;
	
	/**
	 * Maximum offset of the data that can be returned
	 */
	private static final long MAX_OFFSET = (int)Math.pow(2, 32);
	
	/**
	 * Creates an {@link SSFTP} object from a stream of bytes
	 * @param bytes	the stream of bytes to convert
	 * @return		an SSFTP object based off of the stream of the bytes
	 */
	public static SSFTP fromBytes(byte[] bytes){
		
		int buffPos = 0;
		
		// Grab the flags
		byte flags = bytes[buffPos++];
		
		// Grab  the length
		int length = (unsignedByteToInt(bytes[buffPos++]) << 8)
				+ unsignedByteToInt(bytes[buffPos++]); 
		
		// Grab the offset
		long offset = (unsignedByteToInt(bytes[buffPos++]) << 24) 
				+ (unsignedByteToInt(bytes[buffPos++]) << 16)
				+ (unsignedByteToInt(bytes[buffPos++]) << 8)
				+ unsignedByteToInt(bytes[buffPos++]);
		
		// Grab the start of the name and find the end of it
		int nameStartIndex = buffPos;
		
		while(bytes[buffPos] != 0){
			buffPos++;
		}
		
		// Calculate the length of the name and extract it
		int nameLength = buffPos - nameStartIndex;
		
		byte[] nameBytes = new byte[nameLength];
		System.arraycopy(bytes, nameStartIndex, nameBytes, 0, nameLength);
		
		// Validate the file name
		String fileName = new String(nameBytes);
		
		boolean nameInvalid = (fileName.length() == 0);
		
		if(nameInvalid){
			fileName = " ";
		}
		
		// Validate the size
		boolean sizeInvalid = ((length > MAX_LENGTH) || (length < 0));
		
		if(sizeInvalid){
			length = 0;
		}
		
		// Create the new SSFTP object
		SSFTP ssftp = new SSFTP(fileName, length, offset);
		
		// Set the flags
		ssftp.mIsRequest = ((flags & IS_REQUEST_MASK) == IS_REQUEST_MASK);
		ssftp.mIsEOF = ((flags & IS_EOF_MASK) == IS_EOF_MASK);
		ssftp.mIsFileNotFound = ((flags & IS_FILE_NOT_FOUND_MASK) == IS_FILE_NOT_FOUND_MASK);
		ssftp.mIsInvalidRequest = ((flags & IS_INVALID_REQUEST_MASK) == IS_INVALID_REQUEST_MASK);
		
		// Set a flag if the file name is not valid
		if((nameInvalid) || sizeInvalid){
			ssftp.setIsInvalidRequest(true);
		}
		
		// Move the pointer to the data
		buffPos += MAX_FILE_NAME_LENGTH - nameLength + 1;
		
		// If it is a response and there is data, copy it
		if(ssftp.isResponse() && length != 0){
			
			byte[] data = new byte[length];
			
			System.arraycopy(bytes, buffPos, data, 0, length);
			
			ssftp.setData(data, length);
		}
		
		return ssftp;
	}
	
	/**
	 * Converts an unsigned byte to a java {@code int}
	 * @param b	the byte to convert
	 * @return	the {@code int} form of the byte
	 */
	private static int unsignedByteToInt(byte b){
		return (int) b & 0xFF;
	}
	
	/**
	 * Packet data
	 */
	private byte mData[];
	
	/**
	 * Name of the file
	 */
	private String mFileName;
	
	/**
	 * Indicates whether the EOF has been reached
	 */
	private boolean mIsEOF;
	
	/**
	 * Indicates whether the file was not found
	 */
	private boolean mIsFileNotFound;
	
	/**
	 * Indicates whether the request is invalid
	 */
	private boolean mIsInvalidRequest;
	
	/**
	 * Indicates whether this packet is a request or response
	 */
	private boolean mIsRequest;
	
	/**
	 * Number of bytes requested/returned
	 */
	private int mLength;
	
	/**
	 * Offset to start reading at
	 */
	private long mOffset;
	
	/**
	 * Size of the data
	 */
	private int mDataSize;
	
	/**
	 * Constructor.  Creates a new request packet
	 * @param fileName	name of the file
	 * @param length	number of bytes to request
	 * @param offset	start position to start reading
	 */
	public SSFTP(String fileName, int length, long offset){
		
		// Argument Validation
		// TODO: Fix the unsigned range issue (only have half the range)
		if(fileName == null){
			throw new IllegalArgumentException("fileName is null");
		}else if((fileName.length() == 0) || (fileName.length() > MAX_FILE_NAME_LENGTH)){
			throw new IllegalArgumentException("fileName is not between 1 and " + MAX_FILE_NAME_LENGTH + " characters");
		}else if((length > MAX_LENGTH) || (length < 0)){
			throw new IllegalArgumentException("length is not between 0 and " + MAX_LENGTH);
		}else if((offset < 0) || (offset > MAX_OFFSET)){
			throw new IllegalArgumentException("offset is not between 0 and " + MAX_OFFSET);
		}
		
		mLength = length;
		mFileName = fileName;
		mOffset = offset;
		
		// Set the flags
		mIsRequest = true;
		mIsEOF = false;
		mIsFileNotFound = false;
		mIsInvalidRequest = false;
		
		mData = null;
	}
	
	/**
	 * Gets the data
	 * @return	the data
	 */
	public byte[] getData(){
		return this.mData;
	}
	
	/**
	 * Gets the file name
	 * @return	the file name
	 */
	public String getFileName(){
		return this.mFileName;
	}
	
	/**
	 * Gets the length of the data requested/returned
	 * @return	the length of the data requested/returned
	 */
	public int getLength(){
		return this.mLength;
	}
	
	/**
	 * Gets the size of the packet (including the header and data)
	 * @return	the size of the packet
	 */
	public int getNumBytes(){
		return NUM_HEADER_BYTES + mDataSize;
	}
	
	/**
	 * Gets the offset
	 * @return	the offset
	 */
	public long getOffset(){
		return this.mOffset;
	}
	
	/**
	 * Indicates whether the EOF flag has been set
	 * @return	{@code true} if EOF flag has been set, {@code false} otherwise
	 */
	public boolean isEOF(){
		return mIsEOF;
	}
	
	/**
	 * Indicates whether the FileNotFound flag has been set
	 * @return	{@code true} if the FileNotFound flag has been set, 
	 * 			{@code false} otherwise
	 */
	public boolean isFileNotFound(){
		return this.mIsFileNotFound;
	}
	
	/**
	 * Indicates whether the InvalidRequest flag has been set
	 * @return	{@code true} if the InvalidRequest flag has been set, 
	 * 			{@code false} otherwise
	 */
	public boolean isInvaldRequest(){
		return this.mIsInvalidRequest;
	} 
	
	/**
	 * Indicates whether this packet is a request or response
	 * @return	{@code true} if this packet is a request,
	 * 			{@code false} if this packet is a response
	 */
	public boolean isRequest(){
		return mIsRequest;
	}
	
	/**
	 * Gets whether this packet is a response
	 * @return	{@code true} if this packet is a response, {@code false} if
	 * 			this packet is a response
	 */
	public boolean isResponse(){
		return !mIsRequest;
	}
	
	/**
	 * Sets the data 
	 * @param data	the data
	 * @param numBytes	the number of bytes to send
	 */
	public void setData(byte data[], int numBytes){
		this.mData = data;
		this.mDataSize = numBytes;
	}
	
	/**
	 * Sets the file name
	 * @param fileName	the file name
	 * @throws IllegalArgumentException if {@code fileName} is null, has a
	 * 									length of 0, or is greater than
	 * 									{@code MAX_FILE_NAME_LENGTH} characters
	 */
	public void setFileName(String fileName){
		
		if(fileName == null){
			throw new IllegalArgumentException("fileName is null");
		}else if((fileName.length() == 0) || (fileName.length() > MAX_FILE_NAME_LENGTH)){
			throw new IllegalArgumentException("fileName is not between 1 and " + MAX_FILE_NAME_LENGTH + " characters");
		}
		
		this.mFileName = fileName;
	}
	
	/**
	 * Sets whether the EOF flag has been set
	 * @param isEOF	{@code true} if the EOF flag has been set, {@code false}
	 * 				otherwise
	 */
	public void setIsEOF(boolean isEOF){
		this.mIsEOF = isEOF;
	}
	
	/**
	 * Sets whether the FileNotFound flag has been set
	 * @param isFileNotFound	{@code true} if the FileNotFound flag has been 
	 * 							set, {@code false} otherwise
	 */
	public void setIsFileNotFound(boolean isFileNotFound){
		this.mIsFileNotFound = isFileNotFound;
	}
	
	/**
	 * Sets whether the InvalidRequest flag has been set
	 * @param isInvalidRequest	{@code true} if the InvalidRequest flag has been
	 * 							set, {@code false} otherwise
	 */
	public void setIsInvalidRequest(boolean isInvalidRequest){
		this.mIsInvalidRequest = isInvalidRequest;
	}
	
	/**
	 * Sets whether the packet is a request or a response
	 * @param isRequest	{@code true} if this packet is a request, {@code false}
	 * 					if this packet is a response
	 */
	public void setIsRequest(boolean isRequest){
		this.mIsRequest = isRequest;
	}
	
	/**
	 * Sets whether this packet is a response
	 * @param isResponse	{@code true} if this packet is a response,
	 * 						{@code false} if this packet is a request
	 */
	public void setIsResponse(boolean isResponse){
		mIsRequest = !isResponse;
	}
	
	/**
	 * Sets the length of the data requested/returned
	 * @param length	the length of the data requested/returned
	 * @throws IllegalArgumentException	if length is less than 0 or greater than {@link MAX_LENGTH}
	 */
	public void setLength(int length){
		
		if((length > MAX_LENGTH) || (length < 0)){
			throw new IllegalArgumentException("length is not between 0 and " + MAX_LENGTH);
		}
		
		this.mLength = length;
	}
	
	public void setOffset(long offset){
		
		if((offset < 0) || (offset > MAX_OFFSET)){
			throw new IllegalArgumentException("offset is not between 0 and " + MAX_OFFSET);
		}
	}
	
	/**
	 * Converts the {@link SSFTP} object into bytes
	 * @return	an array of bytes that represent the SSFTP object
	 */
	public byte[] toBytes(){
		
		// Create the array (size of the header + size of the data)
		byte returnBytes[] = new byte[NUM_HEADER_BYTES + mDataSize];
		int buffPos = 0;
	
		// Set the flags
		int flags = 0;
		
		if(mIsRequest){
			flags |= IS_REQUEST_MASK;
		}
		
		if(mIsEOF){
			flags |= IS_EOF_MASK;
		}
		
		if(mIsFileNotFound){
			flags |= IS_FILE_NOT_FOUND_MASK;
		}
		
		if(mIsInvalidRequest){
			flags |= IS_INVALID_REQUEST_MASK;
		}
		
		returnBytes[buffPos++] = (byte)(0xFF & flags);
		
		// Set the length
		returnBytes[buffPos++] = (byte)((0xFF00 & mLength) >> 8);
		returnBytes[buffPos++] = (byte)(0x00FF & mLength);
		
		returnBytes[buffPos++] = (byte)((0xFF000000 & mOffset) >> 24);
		returnBytes[buffPos++] = (byte)((0x00FF0000 & mOffset) >> 16);
		returnBytes[buffPos++] = (byte)((0x0000FF00 & mOffset) >> 8);
		returnBytes[buffPos++] = (byte)(0x000000FF & mOffset);
		
		// Set the file name
		byte[] fileNameBytes = mFileName.getBytes();
		
		System.arraycopy(fileNameBytes, 0, returnBytes, buffPos, fileNameBytes.length);
		
		buffPos += fileNameBytes.length;
		
		// Fill the remaining area with zeroes (null termination)
		for(int i = fileNameBytes.length; i < FILE_NAME_NUM_BYTES; i++){
			returnBytes[buffPos++] = 0;
		}
		
		// Copy the data if there is any
		if(mData != null){
			System.arraycopy(mData, 0, returnBytes, buffPos, mDataSize);
		}
		
		return returnBytes;
	}
	
	/**
	 * Prints out the string representation of the packet
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Read Back\nIsRequest:\t\t" + String.valueOf(mIsRequest));
		builder.append("\nIsEOF:\t\t\t" + String.valueOf(mIsEOF));
		builder.append("\nIsFileNotFound:\t\t" + String.valueOf(mIsFileNotFound));
		builder.append("\nIsInvalidRequest:\t" + String.valueOf(mIsInvalidRequest));
		builder.append("\nLength:\t\t\t" + String.valueOf(mLength));
		builder.append("\nOffset:\t\t\t" + String.valueOf(mOffset));
		builder.append("\nFileName:\t\t\"" + String.valueOf(mFileName) + "\"");

		if((getData() != null) && !getFileName().equalsIgnoreCase(".")){
			builder.append("\nDataSize:\t\t" + String.valueOf(mDataSize));
		}
		
		return builder.toString();
	}
}
