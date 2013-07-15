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


public class FacebookAPI {

	public static final String FB_XMPP_HOST = "chat.facebook.com";
	public static final int FB_XMPP_PORT = 5222;

	private ConnectionConfiguration config;
	private XMPPConnection connection;

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

	public void disconnect() {
		if ((connection != null) && (connection.isConnected())) {
			Presence presence = new Presence(Presence.Type.unavailable);
			presence.setStatus("offline");
			connection.disconnect(presence);
		}
	}

	public boolean login(String username, String password) 
			throws XMPPException {
		if ((connection != null) && (connection.isConnected())) {
			connection.login(username, password);
			return true;
		}
		return false;
	}

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