package ca.concordia.resolute.core.chat;

import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import com.fb.xmppchat.helper.MySASLDigestMD5Mechanism;

/**
 * This class implement Facebook API for chat conversation. For creating a chat conversation these steps should be done:
 * 1- {@link #connect()}
 * 2- {@link #login(String, String)} with username and password
 * 3- {@link #getOnlineFriends()} get list of online friends of this logined account
 * 4- {@link #createChat(RosterEntry, MessageListener)} create a chat conversation with specific user. 
 * 5- {@link #disconnect()} disconnect from chat conversation.
 * @author mjlaali
 *
 */
public class FacebookAPI {

	public static final String FB_XMPP_HOST = "chat.facebook.com";
	public static final int FB_XMPP_PORT = 5222;

	private ConnectionConfiguration config;
	private XMPPConnection connection;

	/**
	 * Connect to the Facebook website
	 * @return the connection ID
	 * @throws XMPPException
	 */
	public String connect() throws XMPPException {
		config = new ConnectionConfiguration(FB_XMPP_HOST, FB_XMPP_PORT);
		SASLAuthentication.registerSASLMechanism("DIGEST-MD5"
				,MySASLDigestMD5Mechanism.class);
		config.setSASLAuthenticationEnabled(true);
		config.setDebuggerEnabled(false);
		connection = new XMPPConnection(config);
		connection.connect();
		return connection.getConnectionID();
	}
	
	public XMPPConnection getConnection() {
		return connection;
	}

	/**
	 * disconnect from Facebook
	 */
	public void disconnect() {
		if ((connection != null) && (connection.isConnected())) {
			Presence presence = new Presence(Presence.Type.unavailable);
			presence.setStatus("offline");
			connection.disconnect(presence);
		}
	}

	/**
	 * Login to the Facebook with specific user name and password
	 * @param username the Facebook account user name
	 * @param password the Facebook account password
	 * @return true if it is login successfully, false otherwise
	 * @throws XMPPException
	 */
	public boolean login(String username, String password) 
			throws XMPPException {
		if ((connection != null) && (connection.isConnected())) {
			connection.login(username, password);
			return true;
		}
		return false;
	}

	/**
	 * @return list of all the friends of the user that are online.
	 */
	public List<RosterEntry> getOnlineFriends() {
		List<RosterEntry> onlineFriends = new LinkedList<>();
		if ((connection != null) && (connection.isConnected())) {
			Roster roster = connection.getRoster();
			for (RosterEntry entry : roster.getEntries()) {
				Presence presence = roster.getPresence(entry.getUser());
				if ((presence != null) 
						&& (presence.getType() != Presence.Type.unavailable)) {
					onlineFriends.add(entry);
				}
			}
		}
		return onlineFriends;
	}

	/**
	 * Create a chat conversation with specific friend of the user.
	 * @param friend The friend of person to create a chat conversation.
	 * @param listener a listener that receive all messages from the friend
	 * @return Chat conversation
	 * @throws XMPPException
	 */
	public Chat createChat(final RosterEntry friend, MessageListener listener) 
			throws XMPPException {
		if ((connection != null) && (connection.isConnected())) {
			ChatManager chatManager = connection.getChatManager();
			Chat chat = chatManager.createChat(friend.getUser(), listener);
			return chat;
		}
		return null;
	}

}