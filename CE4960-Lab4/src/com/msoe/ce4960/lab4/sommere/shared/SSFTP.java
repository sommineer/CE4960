package com.msoe.ce4960.lab4.sommere.shared;

/**
 * Represents a stateless file server packet
 * @author Erik Sommer
 *
 */
public class SSFTP {
	
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
	 * Maximum size of the data that can be returned
	 */
	private static final int MAX_LENGTH = (int)Math.pow(2, 16);
	
	/**
	 * Maximum offset of the data that can be returned
	 */
	private static final long MAX_OFFSET = (int)Math.pow(2, 32);
	
	/**
	 * Number of bytes in the header
	 */
	public static final int NUM_HEADER_BYTES = 39;
	
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
		
		// Create the new SSFTP object
		SSFTP ssftp = new SSFTP(new String(nameBytes), length, offset);
		
		// Set the flags
		ssftp.mIsRequest = ((flags & IS_REQUEST_MASK) == IS_REQUEST_MASK);
		ssftp.mIsEOF = ((flags & IS_EOF_MASK) == IS_EOF_MASK);
		ssftp.mIsFileNotFound = ((flags & IS_FILE_NOT_FOUND_MASK) == IS_FILE_NOT_FOUND_MASK);
		ssftp.mIsInvalidRequest = ((flags & IS_INVALID_REQUEST_MASK) == IS_INVALID_REQUEST_MASK);
		
		// Increment the position (was pointing to the null terminator before)
		buffPos++;
		
		// Copy the data if there is any
		if(buffPos != bytes.length){
			int numLeft = bytes.length - buffPos;
			
			byte[] data = new byte[numLeft];
			
			System.arraycopy(bytes, buffPos, data, 0, numLeft);
			
			ssftp.setData(data);
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
		
	}
	
	public byte[] getData(){
		return this.mData;
	}
	
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
	
	public int getNumBytes(){
		return NUM_HEADER_BYTES + ((mData != null) ? mData.length : 0);
	}
	
	public long getOffset(){
		return this.mOffset;
	}
	
	public boolean isEOF(){
		return mIsEOF;
	}
	public boolean isFileNotFound(){
		return this.mIsFileNotFound;
	}
	public boolean isInvaldRequest(){
		return this.mIsInvalidRequest;
	} 
	
	public boolean isRequest(){
		return mIsRequest;
	}
	
	public void setData(byte data[]){
		this.mData = data;
	}
	
	public void setFileName(String fileName){
		
		if(fileName == null){
			throw new IllegalArgumentException("fileName is null");
		}else if((fileName.length() == 0) || (fileName.length() > MAX_FILE_NAME_LENGTH)){
			throw new IllegalArgumentException("fileName is not between 1 and " + MAX_FILE_NAME_LENGTH + " characters");
		}
		
		this.mFileName = fileName;
	}
	
	public void setIsEOF(boolean isEOF){
		this.mIsEOF = isEOF;
	}
	public void setIsFileNotFound(boolean isFileNotFound){
		this.mIsFileNotFound = isFileNotFound;
	}
	public void setIsInvalidRequest(boolean isInvalidRequest){
		this.mIsInvalidRequest = isInvalidRequest;
	}
	public void setIsRequest(boolean isRequest){
		this.mIsRequest = isRequest;
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
		byte returnBytes[] = new byte[NUM_HEADER_BYTES + ((mData != null) ? mData.length : 0)];
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
			System.arraycopy(mData, 0, returnBytes, buffPos, mData.length);
		}
		
		return returnBytes;
	}
}
