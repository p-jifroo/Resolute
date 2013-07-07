package ca.concordia.resolute.core.chat;

import gate.Document;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;


public class Conversation {
	private List<Message> msgs = new LinkedList<>();
	private List<ChatMessageListener> listeners = new LinkedList<>();
	private Document doc;
	

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
	
	public Document setDoc(Document doc) {
		Document old = this.doc;
		this.doc = doc;
		return old;
	}
	
	public Document getDoc() {
		return doc;
	}
	
	public String toXML() throws UnsupportedEncodingException, XMLStreamException, FactoryConfigurationError{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLSaver xmlSaver = new XMLSaver(baos);
		for (Message msg: msgs)
			xmlSaver.newChatMessage(this, msg);
		xmlSaver.endChat();
		
		return baos.toString("UTF-8");
	}
}
