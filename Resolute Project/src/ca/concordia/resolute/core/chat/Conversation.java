package ca.concordia.resolute.core.chat;

import gate.Document;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

/**
 * A class that used for the modeling a chat conversation.
 * @author mjlaali
 *
 */
public class Conversation {
	private List<Message> msgs = new LinkedList<>();
	private List<ChatMessageListener> listeners = new LinkedList<>();
	private Document doc;
	

	public Conversation() {
	}
	
	private void notifyChange(Message msg){
		for (ChatMessageListener listener: listeners){
			listener.newChatMessage(this, msg);
		}

	}
	
	/**
	 * add a new message to the chat conversation.
	 * @param msg a new message
	 */
	public void addMessage(Message msg){
		msgs.add(msg);
		notifyChange(msg);
	}
	
	/**
	 * This method inform to all listener that the chat conversation finished. So, the listeners will clean up their data.
	 */
	public void endChat(){
		for (ChatMessageListener listener: listeners){
			listener.endChat();
		}
	}
	
	/**
	 * Add new listener to the chat conversation. 
	 * @param listener a new listener
	 */
	public void addListener(ChatMessageListener listener){
		listeners.add(listener);
	}
	
	public List<ChatMessageListener> getListeners() {
		return listeners;
	}
	
	/**
	 * Simulate chat conversation from start, as if it happens again. This will be useful to test a new listener in simulation mode. 
	 */
	public void simulate(){
		List<Message> msgs = this.msgs;
		this.msgs = new LinkedList<>();
		
		for (Message msg: msgs)
			addMessage(msg);
		endChat();
	}
	
	/**
	 * This will set a GATE document model for the chat client. In the GATE document, all the text processing result has been saved.
	 * @param doc a GATE document model of the chat conversation
	 * @return old GATE document
	 */
	public Document setDoc(Document doc) {
		Document old = this.doc;
		this.doc = doc;
		return old;
	}
	
	public Document getDoc() {
		return doc;
	}
	
	/**
	 * Convert the chat message to an XML string. this string can be loaded to the GATE for any text processes
	 * @return XML representation of chat conversation.
	 * @throws UnsupportedEncodingException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 */
	public String toXML() throws UnsupportedEncodingException, XMLStreamException, FactoryConfigurationError{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLSaver xmlSaver = new XMLSaver(baos);
		for (Message msg: msgs)
			xmlSaver.newChatMessage(this, msg);
		xmlSaver.endChat();
		
		return baos.toString("UTF-8");
	}
}
