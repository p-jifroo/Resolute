package ca.concordia.resolute.core.chat;

import gate.Document;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

public class FacebookConversation implements ConversationAPI, MessageListener{
	private ConversationAPI datamodel;
	private Chat chat;
	private String clienUsername;

	public FacebookConversation(ConversationAPI api){
		this.datamodel = api;

	}

	public void setChat(FacebookAPI facebookAPI, RosterEntry aFriend) throws XMPPException{
		chat = facebookAPI.createChat(aFriend, this);
		clienUsername = aFriend.getName();
	}

	@Override
	public void addMessage(Message msg) {
		try {
			chat.sendMessage(msg.getMsg());
		} catch (XMPPException e) {
			throw new RuntimeException(e);
		}
		datamodel.addMessage(msg);
	}

	@Override
	public void endChat() {
		datamodel.endChat();
	}

	@Override
	public void addListener(ChatMessageListener listener) {
		datamodel.addListener(listener);
	}

	@Override
	public List<ChatMessageListener> getListeners() {
		return datamodel.getListeners();
	}

	@Override
	public void simulate() {
		datamodel.simulate();
	}

	@Override
	public Document setDoc(Document doc) {
		return datamodel.setDoc(doc);
	}

	@Override
	public Document getDoc() {
		return datamodel.getDoc();
	}

	@Override
	public String toXML() throws UnsupportedEncodingException,
	XMLStreamException, FactoryConfigurationError {
		return datamodel.toXML();
	}

	@Override
	public void processMessage(Chat chat,
			org.jivesoftware.smack.packet.Message message) {
		if ((message != null) && (message.getBody() != null)) {
			Message msg = new Message(message.getBody(), "", clienUsername);
			datamodel.addMessage(msg);
		}

	}
}
