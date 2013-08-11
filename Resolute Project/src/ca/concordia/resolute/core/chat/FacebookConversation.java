package ca.concordia.resolute.core.chat;

import gate.Document;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

/**
 * It is decorate pattern. FacebookConversation is {@link ConversationAPI} that is able to decorate a {@link ConversationAPI}
 * to Facebook conversation. Facebook conversation is a conversation that is sent and receive messages to/from Facebook
 * 
 * @author mjlaali
 *
 */
public class FacebookConversation implements ConversationAPI, MessageListener{
	private ConversationAPI datamodel;
	private Chat chat;
	private String clienUsername;

	/**
	 * Initial this class with a ConversationApi
	 * @param api previous conversation that should be decorated
	 */
	public FacebookConversation(ConversationAPI api){
		this.datamodel = api;

	}

	/**
	 * Set the chat which the conversation is connected to.
	 * @param facebookAPI Api that is used to control connection of Facebook
	 * @param aFriend a friend to create chat conversation
	 * @throws XMPPException
	 */
	public void setChat(FacebookAPI facebookAPI, RosterEntry aFriend) throws XMPPException{
		chat = facebookAPI.createChat(aFriend, this);
		clienUsername = aFriend.getName();
	}

	/**
	 * see {@link ConversationAPI.#addMessage(Message)}
	 */
	@Override
	public void addMessage(Message msg) {
		try {
			chat.sendMessage(msg.getMsg());
		} catch (XMPPException e) {
			throw new RuntimeException(e);
		}
		datamodel.addMessage(msg);
	}

	/**
	 * see {@link ConversationAPI.#endChat()}
	 */
	@Override
	public void endChat() {
		datamodel.endChat();
	}

	/**
	 * see {@link ConversationAPI.#addListener(ChatMessageListener)}
	 */
	@Override
	public void addListener(ChatMessageListener listener) {
		datamodel.addListener(listener);
	}

	/**
	 * see {@link ConversationAPI.#getListeners()}
	 */
	@Override
	public List<ChatMessageListener> getListeners() {
		return datamodel.getListeners();
	}

	/**
	 * see {@link ConversationAPI.#simulate()}
	 */
	@Override
	public void simulate() {
		datamodel.simulate();
	}

	/**
	 * see {@link ConversationAPI.#setDoc(Document)}
	 */
	@Override
	public Document setDoc(Document doc) {
		return datamodel.setDoc(doc);
	}

	/**
	 * see {@link ConversationAPI.#getDoc()}
	 */
	@Override
	public Document getDoc() {
		return datamodel.getDoc();
	}

	/**
	 * see {@link ConversationAPI.#toXML(Map)}
	 */
	@Override
	public String toXML(Map<String, String> conversationAttributes) throws UnsupportedEncodingException,
	XMLStreamException, FactoryConfigurationError {
		return datamodel.toXML(conversationAttributes);
	}

	/**
	 * see {@link MessageListener.#processMessage(Chat, org.jivesoftware.smack.packet.Message)}
	 */
	@Override
	public void processMessage(Chat chat,
			org.jivesoftware.smack.packet.Message message) {
		if ((message != null) && (message.getBody() != null)) {
			Calendar cal = Calendar.getInstance();
			cal.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

			Message msg = new Message(message.getBody(), sdf.format(cal.getTime()), clienUsername);
			datamodel.addMessage(msg);
		}

	}
}
