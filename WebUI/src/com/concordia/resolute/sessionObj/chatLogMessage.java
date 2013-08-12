package com.concordia.resolute.sessionObj;

public class chatLogMessage {

	private String message;
	private boolean newMessageFlag;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message+"\n";
	}
	
	@Override
	public String toString(){
		return message;
	}
	
	public boolean hasMessage(){
		return this.newMessageFlag;
	}
	
	public void newMessage(){
		this.newMessageFlag = true;
	} 
	
	public void setDefaultMessageFlag(){
		this.newMessageFlag = false;

	}
	


}
