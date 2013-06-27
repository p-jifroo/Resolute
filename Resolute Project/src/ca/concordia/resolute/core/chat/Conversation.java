package ca.concordia.resolute.core.chat;

import java.util.LinkedList;
import java.util.List;


public class Conversation {
	private List<Message> msgs = new LinkedList<>();
	private List<ChatMessageListener> listeners = new LinkedList<>();

	public Conversation() {
	}
	
	public void notifyChange(Message msg){
		for (ChatMessageListener listener: listeners){
			listener.newChatMessage(this, msg);
		}

	}
	
	public void addMessage(Message msg){
		msgs.add(msg);
		notifyChange(msg);
	}
	
	public void endChat(){
		for (ChatMessageListener listener: listeners){
			listener.endChat();
		}
	}
	
	public void addListener(ChatMessageListener listener){
		listeners.add(listener);
	}
	
	public List<ChatMessageListener> getListeners() {
		return listeners;
	}
	
	public void simulate(){
		List<Message> msgs = this.msgs;
		this.msgs = new LinkedList<>();
		
		for (Message msg: msgs)
			addMessage(msg);
		endChat();
	}
}
