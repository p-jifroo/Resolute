package ca.concordia.resolute.core.chat;

import gate.Document;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import ca.concordia.resolute.core.chat.listener.XMLSaver;

/**
 * A class that used for the modeling a chat conversation.
 * @author mjlaali
 *
 */
public class ConversationModel implements ConversationAPI {
	private List<Message> msgs = new LinkedList<>();
	private List<ChatMessageListener> listeners = new LinkedList<>();
	private Document doc;
	private Set<String> users = new TreeSet<>();

	public ConversationModel() {
	}
	
	private void notifyChange(Message msg){
		for (ChatMessageListener listener: listeners){
			listener.newChatMessage(this, msg);
		}

	}
	
	/* (non-Javadoc)
	 * @see ca.concordia.resolute.core.chat.ConversationAPI#addMessage(ca.concordia.resolute.core.chat.Message)
	 */
	@Override
	public synchronized void addMessage(Message msg){
		msgs.add(msg);
		users.add(msg.getId());
		notifyChange(msg);
	}
	
	/* (non-Javadoc)
	 * @see ca.concordia.resolute.core.chat.ConversationAPI#endChat()
	 */
	@Override
	public synchronized void endChat(){
		for (ChatMessageListener listener: listeners){
			listener.endChat();
		}
	}
	
	/* (non-Javadoc)
	 * @see ca.concordia.resolute.core.chat.ConversationAPI#addListener(ca.concordia.resolute.core.chat.FacebookConversation)
	 */
	@Override
	public synchronized void addListener(ChatMessageListener listener){
		listeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see ca.concordia.resolute.core.chat.ConversationAPI#getListeners()
	 */
	@Override
	public List<ChatMessageListener> getListeners() {
		return listeners;
	}
	
	/* (non-Javadoc)
	 * @see ca.concordia.resolute.core.chat.ConversationAPI#simulate()
	 */
	@Override
	public synchronized void simulate(){
		List<Message> msgs = this.msgs;
		this.msgs = new LinkedList<>();
		
		for (Message msg: msgs)
			addMessage(msg);
		endChat();
	}
	
	/* (non-Javadoc)
	 * @see ca.concordia.resolute.core.chat.ConversationAPI#setDoc(gate.Document)
	 */
	@Override
	public synchronized Document setDoc(Document doc) {
		Document old = this.doc;
		this.doc = doc;
		return old;
	}
	
	/* (non-Javadoc)
	 * @see ca.concordia.resolute.core.chat.ConversationAPI#getDoc()
	 */
	@Override
	public synchronized Document getDoc() {
		return doc;
	}
	
	/* (non-Javadoc)
	 * @see ca.concordia.resolute.core.chat.ConversationAPI#toXML()
	 */
	@Override
	public synchronized String toXML(Map<String, String> conversationAttributes) throws UnsupportedEncodingException, XMLStreamException, FactoryConfigurationError{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (conversationAttributes == null)
			conversationAttributes = new TreeMap<>();
		else
			conversationAttributes = new TreeMap<>(conversationAttributes);
		
		conversationAttributes.put("users", users.toString());
		
		XMLSaver xmlSaver = new XMLSaver(baos, conversationAttributes);
		for (Message msg: msgs)
			xmlSaver.newChatMessage(this, msg);
		xmlSaver.endChat();
		
		return baos.toString("UTF-8");
	}
	
	public List<Message> getMsgs() {
		return msgs;
	}
}
