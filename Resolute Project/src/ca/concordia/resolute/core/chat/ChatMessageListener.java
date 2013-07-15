package ca.concordia.resolute.core.chat;

/**
 * This interface will be used to implement the observer pattern for chat conversation ({@link ConversationModel}) data model.
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
	public void newChatMessage(ConversationAPI conversation, Message msg);
	
	/**
	 * The end of message event. Do your clean up here
	 */
	public void endChat();
}
