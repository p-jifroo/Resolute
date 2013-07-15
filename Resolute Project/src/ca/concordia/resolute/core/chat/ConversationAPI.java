package ca.concordia.resolute.core.chat;

import gate.Document;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

public interface ConversationAPI {

	/**
	 * add a new message to the chat conversation.
	 * @param msg a new message
	 */
	public abstract void addMessage(Message msg);

	/**
	 * This method inform to all listener that the chat conversation finished. So, the listeners will clean up their data.
	 */
	public abstract void endChat();

	/**
	 * Add new listener to the chat conversation. 
	 * @param listener a new listener
	 */
	public abstract void addListener(ChatMessageListener listener);

	/**
	 * The class should be thread safe. So getting listeners may cause some errors in multi-thread application 
	 * @return a non-thread safe list of listeners
	 */
	public abstract List<ChatMessageListener> getListeners();

	/**
	 * Simulate chat conversation from start, as if it happens again. This will be useful to test a new listener in simulation mode. 
	 */
	public abstract void simulate();

	/**
	 * This will set a GATE document model for the chat client. In the GATE document, all the text processing result has been saved.
	 * @param doc a GATE document model of the chat conversation
	 * @return old GATE document
	 */
	public abstract Document setDoc(Document doc);

	public abstract Document getDoc();

	/**
	 * Convert the chat message to an XML string. this string can be loaded to the GATE for any text processes
	 * @return XML representation of chat conversation.
	 * @throws UnsupportedEncodingException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 */
	public abstract String toXML() throws UnsupportedEncodingException,
			XMLStreamException, FactoryConfigurationError;

}