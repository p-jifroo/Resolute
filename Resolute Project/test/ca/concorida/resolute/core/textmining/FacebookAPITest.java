package ca.concorida.resolute.core.textmining;

import gate.Gate;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;

import java.io.IOException;
import java.util.List;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.concordia.resolute.core.chat.ConversationModel;
import ca.concordia.resolute.core.chat.FacebookAPI;
import ca.concordia.resolute.core.chat.FacebookConversation;
import ca.concordia.resolute.core.chat.Message;
import ca.concordia.resolute.core.chat.listener.ConsoleMessage;
import ca.concordia.resolute.core.chat.listener.ResoluteNLPAnalyzer;
import ca.concordia.resolute.core.textmining.gate.ResouluteApp;
import ca.concordia.resolute.core.textmining.gate.RuleBaseAgeDetection;

public class FacebookAPITest {


	private static final String PASSWORD = "123ABC!";
	private static final String USER_NAME_SOEN = "soen.resolute";
	private static final String USER_NAME_PRE = "soenpre.resolute";
	
	private static FacebookAPI pre, soen;

	@BeforeClass
	public static void init() throws XMPPException, InterruptedException{
		pre = new FacebookAPI();
		soen = new FacebookAPI();
		pre.connect();
		pre.login(USER_NAME_PRE, PASSWORD);
		soen.connect();
		soen.login(USER_NAME_SOEN, PASSWORD);

		Thread.sleep(1000);

	}
	
	@AfterClass
	public static void disconnect(){
		soen.disconnect();
		pre.disconnect();
	}
	
	@Test
	public void testFacebookConnection(){
		ConnectionConfiguration config = new ConnectionConfiguration(FacebookAPI.FB_XMPP_HOST, FacebookAPI.FB_XMPP_PORT);
		config.setDebuggerEnabled(true);
		config.setSASLAuthenticationEnabled(true);
		config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);

		XMPPConnection connection = new XMPPConnection(config);
		try{
			connection.connect();
			connection.login(USER_NAME_SOEN, PASSWORD);
			//auth.authenticate(login, password, host);
		}
		catch (XMPPException exc){
			exc.printStackTrace();
		}
	}

	@Test
	public void login() throws XMPPException, InterruptedException{
		System.out.println(pre.getOnlineFriends().size());
		Assert.assertTrue(pre.getOnlineFriends().size() > 0);
	}
	
	@Test
	public void sent_reciveMessage() throws XMPPException, InterruptedException, IOException{

		System.out.println("FacebookAPITest.checkConsolApi(): Start the testing...");
		ConversationModel conversationModel = new ConversationModel();
		FacebookConversation soenConversation = new FacebookConversation(conversationModel);
		FacebookConversation preConversation = new FacebookConversation(conversationModel);
		
		List<RosterEntry> onlineFriends = soen.getOnlineFriends();
		Assert.assertTrue(onlineFriends.size() > 0);
		soenConversation.setChat(soen, onlineFriends.get(0));

		onlineFriends = pre.getOnlineFriends();
		Assert.assertTrue(onlineFriends.size() > 0);
		preConversation.setChat(pre, onlineFriends.get(0));

		conversationModel.addListener(new ConsoleMessage());
		
		soenConversation.addMessage(new Message("hey, it is test message", "", USER_NAME_SOEN));
		
		soenConversation.endChat();
		
		Thread.sleep(2000);
		Assert.assertEquals(2, conversationModel.getMsgs().size());
	}

//	private void checkUser(String msg) throws IOException {
//		System.out.println(msg + " <Press Enter if it you answer is YES>");
//		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
//		Assert.assertTrue(input.readLine().length() == 0);
//	}
	
//	@Test
//	public void checkConsolApi() throws XMPPException, InterruptedException, IOException{
//		FacebookAPI soenpre = new FacebookAPI();
//		soenpre.connect();
//		String username = USER_NAME_SOEN;
//		soenpre.login(username, PASSWORD);
//
//		Thread.sleep(1000);
//		List<RosterEntry> onlineFriends = soenpre.getOnlineFriends();
//		Assert.assertTrue(onlineFriends.size() > 0);
//
//		System.out.println("FacebookAPITest.checkConsolApi(): Start the testing...");
//		FacebookConversation facebookConversation = new FacebookConversation(new ConversationModel());
//		facebookConversation.setChat(soenpre, onlineFriends.get(0));
//		
//		facebookConversation.addListener(new ConsoleMessage());
//		
//		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
//		String line;
//		while ((line = input.readLine()).length() > 0){
//			facebookConversation.addMessage(new Message(line, "", username));
//		}
//		
//		facebookConversation.endChat();
//		soenpre.disconnect();
//	}
	
//	@Test
//	public void testAgeDetection() throws GateException, IOException{
//		Gate.init();
//		ConversationAPI facebookConversation = new ConversationModel();
//		
//		SerialAnalyserController controller = new ResouluteApp().getController();
//		facebookConversation.addListener(new ResoluteNLPAnalyzer(controller));
//		facebookConversation.addListener(new ConsoleMessage());
//
//		System.out.println("FacebookAPITest.checkConsolApi(): Start the testing...");
//		
//		facebookConversation.addMessage(new Message("police", "", "asl?"));
//		facebookConversation.addMessage(new Message("predator", "", "25 m montreal"));
//		
//		Assert.assertTrue(facebookConversation.getDoc().getFeatures().get("Age").equals("25"));
//		
//		facebookConversation.endChat();
//
//	}

	
	@Test
	public void ageDetector() throws XMPPException, InterruptedException, IOException, GateException{
		Gate.init();
		FacebookAPI soenpre = new FacebookAPI();
		soenpre.connect();
		String username = USER_NAME_SOEN;
		soenpre.login(username, PASSWORD);

		ConversationModel api = new ConversationModel();
		FacebookConversation facebookConversation = new FacebookConversation(api);
		
		SerialAnalyserController controller = new ResouluteApp().getController();
		facebookConversation.addListener(new ResoluteNLPAnalyzer(controller));
		facebookConversation.addListener(new ConsoleMessage());

		System.out.println("FacebookAPITest.checkConsolApi(): Start the testing...");
		List<RosterEntry> onlineFriends = soenpre.getOnlineFriends();
		Thread.sleep(1000);
		Assert.assertTrue(onlineFriends.size() > 0);
		facebookConversation.setChat(soenpre, onlineFriends.get(0));
		
		facebookConversation.addMessage(new Message("asl?", "", username));
		facebookConversation.addMessage(new Message("15 m montreal", "", username));

		facebookConversation.endChat();
		
		String age = api.getDoc().getFeatures().get(RuleBaseAgeDetection.AGE_DOC_FEATURE).toString();
		Assert.assertEquals("15", age);
	}
	

}
