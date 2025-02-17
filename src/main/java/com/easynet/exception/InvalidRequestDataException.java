package com.easynet.exception;

public class InvalidRequestDataException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String customMessage;
	
	public InvalidRequestDataException(String message){
		super(message);
	}
	
	public InvalidRequestDataException(String message,String asCustomizedMsg){
		this(message);
		customMessage=asCustomizedMsg;
	}

	public String getCustomMessage() {
		return customMessage;
	}
}
