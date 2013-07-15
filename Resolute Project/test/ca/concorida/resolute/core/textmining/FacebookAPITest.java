package ca.concorida.resolute.core.textmining;

import gate.Gate;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.junit.Assert;
import org.junit.Test;

import ca.concordia.resolute.core.chat.ConsoleMessage;
import ca.concordia.resolute.core.chat.ConversationAPI;
import ca.concordia.resolute.core.chat.ConversationModel;
import ca.concordia.resolute.core.chat.FacebookAPI;
import ca.concordia.resolute.core.chat.FacebookConversation;
import ca.concordia.resolute.core.chat.Message;
import ca.concordia.resolute.core.textmining.ResoluteNLPAnalyzer;
import ca.concordia.resolute.core.textmining.gate.RuleBaseAgeDetectorApp;

public class FacebookAPITest {


	private static final String PASSWORD = "123ABC!";
	private static final String USER_NAME = "soen.resolute";

	@Test
	public void test(){
		ConnectionConfiguration config = new ConnectionConfiguration(FacebookAPI.FB_XMPP_HOST, FacebookAPI.FB_XMPP_PORT);
		config.setDebuggerEnabled(true);
		config.setSASLAuthenticationEnabled(true);
		config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);

		XMPPConnection connection = new XMPPConnection(config);
		try{
			connection.connect();
			connection.login(USER_NAME, PASSWORD);
			//auth.authenticate(login, password, host);
		}
		catch (XMPPException exc){
			exc.printStackTrace();
		}
	}

	@Test
	public void login() throws XMPPException, InterruptedException{
		FacebookAPI soenpre = new FacebookAPI();
		soenpre.connect();
		soenpre.login(USER_NAME, PASSWORD);

		Thread.sleep(1000);
		System.out.println(soenpre.getOnlineFriends().size());

		Assert.assertTrue(soenpre.getOnlineFriends().size() > 0);

		soenpre.disconnect();
	}

	@Test
	public void sentMessage() throws XMPPException, InterruptedException{

		FacebookAPI soenpre = new FacebookAPI();
		soenpre.connect();
		String username = USER_NAME;
		soenpre.login(username, PASSWORD);

		Thread.sleep(1000);
		List<RosterEntry> onlineFriends = soenpre.getOnlineFriends();
		Assert.assertTrue(onlineFriends.size() > 0);

		System.out.println("FacebookAPITest.checkConsolApi(): Start the testing...");
		FacebookConversation facebookConversation = new FacebookConversation(new ConversationModel());
		facebookConversation.setChat(soenpre, onlineFriends.get(0));
		
		facebookConversation.addListener(new ConsoleMessage());
		
		facebookConversation.addMessage(new Message("hey, it is test message", "", username));
		
		facebookConversation.endChat();
		soenpre.disconnect();

	}

	private ConsoleMessage fbml = null;
	
	@Test
	public void recieveMessage() throws XMPPException, InterruptedException, IOException{

		FacebookAPI soenpre = new FacebookAPI();
		//		FBConsoleChatApp soenpre = new FBConsoleChatApp();
		soenpre.connect();
		XMPPConnection conn = soenpre.getConnection();

		fbml = new ConsoleMessage();
		conn.getChatManager().addChatListener(
				new ChatManagerListener() {
					public void chatCreated(Chat chat, boolean createdLocally) {
						if (!createdLocally) {
							chat.addMessageListener(fbml);
						}
					}
				}
				);

		soenpre.login(USER_NAME, PASSWORD);

		System.out.println("Do you get the message");
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		Assert.assertTrue(input.readLine().equals("yes"));

		soenpre.disconnect();
	}
	
	@Test
	public void checkConsolApi() throws XMPPException, InterruptedException, IOException{
		FacebookAPI soenpre = new FacebookAPI();
		soenpre.connect();
		String username = USER_NAME;
		soenpre.login(username, PASSWORD);

		Thread.sleep(1000);
		List<RosterEntry> onlineFriends = soenpre.getOnlineFriends();
		Assert.assertTrue(onlineFriends.size() > 0);

		System.out.println("FacebookAPITest.checkConsolApi(): Start the testing...");
		FacebookConversation facebookConversation = new FacebookConversation(new ConversationModel());
		facebookConversation.setChat(soenpre, onlineFriends.get(0));
		
		facebookConversation.addListener(new ConsoleMessage());
		
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while ((line = input.readLine()).length() > 0){
			facebookConversation.addMessage(new Message(line, "", username));
		}
		
		facebookConversation.endChat();
		soenpre.disconnect();
	}
	
	@Test
	public void testAgeDetection() throws GateException, IOException{
		Gate.init();
		ConversationAPI facebookConversation = new ConversationModel();
		
		SerialAnalyserController controller = new RuleBaseAgeDetectorApp().getController();
		facebookConversation.addListener(new ResoluteNLPAnalyzer(controller));
		facebookConversation.addListener(new ConsoleMessage());

		System.out.println("FacebookAPITest.checkConsolApi(): Start the testing...");
		
		facebookConversation.addMessage(new Message("police", "", "asl?"));
		facebookConversation.addMessage(new Message("predator", "", "25 m montreal"));
		
		Assert.assertTrue(facebookConversation.getDoc().getFeatures().get("Age").equals("25"));
		
		facebookConversation.endChat();

	}

	
	@Test
	public void ageDetector() throws XMPPException, InterruptedException, IOException, GateException{
		Gate.init();
		FacebookAPI soenpre = new FacebookAPI();
		soenpre.connect();
		String username = USER_NAME;
		soenpre.login(username, PASSWORD);

		FacebookConversation facebookConversation = new FacebookConversation(new ConversationModel());
		
		SerialAnalyserController controller = new RuleBaseAgeDetectorApp().getController();
		facebookConversation.addListener(new ResoluteNLPAnalyzer(controller));
		facebookConversation.addListener(new ConsoleMessage());

		System.out.println("FacebookAPITest.checkConsolApi(): Start the testing...");
		List<RosterEntry> onlineFriends = soenpre.getOnlineFriends();
		Thread.sleep(1000);
		Assert.assertTrue(onlineFriends.size() > 0);
		facebookConversation.setChat(soenpre, onlineFriends.get(0));
		
		System.out.println("Try to ask the age of person by saying \"asl?\" and answer it from other facebook account");
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while ((line = input.readLine()).length() > 0){
			facebookConversation.addMessage(new Message(line, "", username));
		}
		
		facebookConversation.endChat();
		soenpre.disconnect();
	}
	

}
