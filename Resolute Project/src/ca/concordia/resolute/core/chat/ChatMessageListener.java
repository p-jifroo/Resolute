package ca.concordia.resolute.core.chat;

public interface ChatMessageListener {

	public void newChatMessage(Conversation conversation, Message msg);
	public void endChat();
}
