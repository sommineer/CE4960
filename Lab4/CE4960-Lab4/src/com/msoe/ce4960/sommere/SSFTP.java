package com.msoe.ce4960.sommere;

public class SSFTP {
	
	public static final int FILE_NAME_NUM_BYTES = 32;
	private static final int IS_EOF_MASK = (1 << 1);
	private static final int IS_FILE_NOT_FOUND_MASK = (1 << 2);
	
	private static final int IS_INVALID_REQUEST_MASK = (1 << 3);
	
	private static final int IS_REQUEST_MASK = (1 << 0);
	
	private static final int MAX_FILE_NAME_LENGTH = 16;
	private static final int MAX_LENGTH = 2^16;
	private static final long MAX_OFFSET = 2^32;
	public static final int NUM_HEADER_BYTES = 39;
	public static SSFTP fromBytes(byte[] bytes){
		
		int buffPos = 0;
		
		byte flags = bytes[buffPos++];
		
		int length = (unsignedByteToInt(bytes[buffPos++]) << 8)
				+ unsignedByteToInt(bytes[buffPos++]); 
		long offset = (unsignedByteToInt(bytes[buffPos++]) << 24) 
				+ (unsignedByteToInt(bytes[buffPos++]) << 16)
				+ (unsignedByteToInt(bytes[buffPos++]) << 8)
				+ unsignedByteToInt(bytes[buffPos++]);
		
		int nameStartIndex = buffPos;
		
		while(bytes[buffPos] != 0){
			buffPos++;
		}
		
		int nameLength = buffPos - nameStartIndex;
		
		byte[] nameBytes = new byte[nameLength];
		System.arraycopy(bytes, buffPos, nameBytes, 0, nameLength);
		
		SSFTP ssftp = new SSFTP(new String(nameBytes), length, offset);
		
		ssftp.mIsRequest = ((flags & IS_REQUEST_MASK) != 0);
		ssftp.mIsEOF = ((flags & IS_EOF_MASK) != 0);
		ssftp.mIsFileNotFound = ((flags & IS_FILE_NOT_FOUND_MASK) != 0);
		ssftp.mIsInvalidRequest = ((flags & IS_INVALID_REQUEST_MASK) != 0);
		
		return ssftp;
	}
	private static int unsignedByteToInt(byte b){
		return (int) b & 0xFF;
	}
	private byte mData[];
	private String mFileName;
	
	private boolean mIsEOF;
	
	private boolean mIsFileNotFound;
	
	private boolean mIsInvalidRequest;
	
	private boolean mIsRequest;
	
	private int mLength;
	
	private long mOffset;
	
	public SSFTP(String fileName, int length, long offset){
		
		if(fileName == null){
			throw new IllegalArgumentException("fileName is null");
		}else if((fileName.length() == 0) || (fileName.length() > MAX_FILE_NAME_LENGTH)){
			throw new IllegalArgumentException("fileName is not between 1 and " + MAX_FILE_NAME_LENGTH + " characters");
		}else if((length > MAX_LENGTH) || (length < 0)){
			throw new IllegalArgumentException("length is not between 0 and " + MAX_LENGTH);
		}else if((offset < 0) || (offset > MAX_OFFSET)){
			throw new IllegalArgumentException("offset is not between 0 and " + MAX_OFFSET);
		}
		
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
	
	public byte[] toBytes(){
		
		byte returnBytes[] = new byte[NUM_HEADER_BYTES + mData.length];
		int buffPos = 0;
	
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
		
		returnBytes[buffPos++] = (byte)((0xFF00 & mLength) >> 8);
		returnBytes[buffPos++] = (byte)(0x00FF & mLength);
		
		returnBytes[buffPos++] = (byte)((0xFF000000 & mOffset) >> 24);
		returnBytes[buffPos++] = (byte)((0x00FF0000 & mOffset) >> 16);
		returnBytes[buffPos++] = (byte)((0x0000FF00 & mOffset) >> 8);
		returnBytes[buffPos++] = (byte)(0x000000FF & mOffset);
		
		byte[] fileNameBytes = mFileName.getBytes();
		
		System.arraycopy(fileNameBytes, 0, returnBytes, buffPos, fileNameBytes.length);
		
		buffPos += fileNameBytes.length;
		
		for(int i = fileNameBytes.length; i < FILE_NAME_NUM_BYTES; i++){
			returnBytes[buffPos++] = 0;
		}
		
		for(int i = 0; i < mFileName.length(); i++){
			returnBytes[buffPos++] = 0;
		}
		
		System.arraycopy(mData, 0, returnBytes, buffPos, mData.length);
		
		return returnBytes;
	}
}
