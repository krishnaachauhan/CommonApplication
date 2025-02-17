package com.easynet.exception;

public class CustomeException extends Exception{
	
	private String customMessage;
	public CustomeException(String message,String customMessage) {
		super(message);
		this.customMessage = customMessage;
	}
	public String getCustomMessage() {
		return customMessage;
	}
	
}
