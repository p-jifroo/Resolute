package ca.concordia.resolute.core.chat;

/**
 * This interface will be used to implement the observer pattern for chat conversation ({@link Conversation}) data model.
 *  
 * @author mjlaali
 *
 */
public interface ChatMessageListener {

	/**
	 * A new message is added to the conversation
	 * @param conversation the current conversation
	 * @param msg a new message
	 */
	public void newChatMessage(Conversation conversation, Message msg);
	
	/**
	 * The end of message event. Do your clean up here
	 */
	public void endChat();
}
