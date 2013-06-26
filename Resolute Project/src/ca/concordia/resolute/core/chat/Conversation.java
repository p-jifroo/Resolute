package ca.concordia.resolute.core.chat;

import java.util.List;


public class Conversation {
	private List<Message> msgs;
	private boolean autoSave;
	private String filePath;

	public Conversation(String filePath) {
		this.filePath = filePath;
	}
	
	public void saveAsXMLFile(){
		//TODO: implement this function
	}
	
	public void addMessage(Message msg){
		msgs.add(msg);
		if (autoSave)
			saveAsXMLFile();
	}
	
	public void setAutoSave(boolean autoSave) {
		this.autoSave = autoSave;
	}
}
