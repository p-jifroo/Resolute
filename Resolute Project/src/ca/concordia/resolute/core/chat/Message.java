package ca.concordia.resolute.core.chat;

public class Message {
	private String msg, time, id;

	public Message(String msg, String time, String id) {
		this.msg = msg;
		this.time = time;
		this.id = id;
	}
	
	public String toXML(){
		return null;
	}
}
