package com.concordia.resolute.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;
import ca.concordia.resolute.core.chat.ChatMessageListener;
import ca.concordia.resolute.core.chat.ConversationAPI;
import ca.concordia.resolute.core.chat.ConversationModel;
import ca.concordia.resolute.core.chat.FacebookAPI;
import ca.concordia.resolute.core.chat.FacebookConversation;
import ca.concordia.resolute.core.chat.Message;
import ca.concordia.resolute.core.chat.listener.ResoluteNLPAnalyzer;
import ca.concordia.resolute.core.chat.listener.XMLSaver;
import ca.concordia.resolute.core.textmining.gate.PredatorDetector;
import ca.concordia.resolute.core.textmining.gate.ResouluteApp;
import ca.concordia.resolute.core.textmining.gate.RuleBaseAgeDetection;

import com.concordia.resolute.sessionObj.chatLogMessage;
import com.concordia.ssh.user.Predator;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @version 1.1
 * @author Thomas
 * @since 2013.07.29
 */
public class FacebookAction extends ActionSupport implements ChatMessageListener, SessionAware { 

	private static final String USER_NAME_KEY = "userName";
	private static final String CONTROLLER_KEY = "controller";
	private FacebookAPI soenpre = new FacebookAPI();
	private SerialAnalyserController controller;
	private FacebookConversation facebookConversation = null;

	private chatLogMessage chatlogMessge = new chatLogMessage();
	private ConversationModel conversationModel = new ConversationModel();
	private Predator predator = new Predator();
	
	
	private Map<String, Object> session;
	
	private String facebookUserName;
	private String facebookPassword;
	
	/**
	 * This method use for login to the face book, and save the login  
	 * parameters.
	 * @return
	 * @throws GateException
	 * @throws IOException
	 * @throws FactoryConfigurationError 
	 * @throws XMLStreamException 
	 */
	public String facebookLogin()throws GateException, IOException, XMLStreamException, FactoryConfigurationError{
		//STEP: initialized GATE
//		Gate.init();
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		
		//set response content type 
		response.setContentType("text/xml;charset=UTF-8");
		//no cache for explorer
		response.setHeader("Cache-Control", "no-cache");
		
		String facebookUser = java.net.URLDecoder.decode(request
				.getParameter("facebookUser"), "UTF-8");//chatContent
		String facebookPassword = java.net.URLDecoder.decode(request.getParameter("facebookPassword"),"UTF-8");
		
		PrintWriter responseOut;
		responseOut = response.getWriter();
		StringBuffer stringBuffer = new StringBuffer();
		
		System.out.println(facebookUser);
		System.out.println(facebookPassword);
		
		
		String basePath = "C:/Users/thomas/Workspaces/MyEclipse 10/.metadata/.me_tcat/webapps/ResoluteProject/";
		PredatorDetector.setBasePath(basePath);
		controller = new ResouluteApp().getController();
		if (controller == null)
			throw new RuntimeException("Can not load the nlp analyzer.");
//		Factory.newDocument("it is test");
		try{
		
			soenpre.connect();
			soenpre.login(facebookUser, facebookPassword);
			
			conversationModel.addListener(new ResoluteNLPAnalyzer(controller));
			conversationModel.addListener(this);
			conversationModel.addListener(new XMLSaver(basePath + "chat.xml"));

			session.put("conversationModel", this.conversationModel);
			session.put(CONTROLLER_KEY, this.controller);
			
			session.put("predatorAgeInfo", this.predator);

			session.put("facebookSoenpre", this.soenpre);
			session.put("textAreaChatLogs", this.chatlogMessge);
			session.put(USER_NAME_KEY, facebookUser);
			System.out
			.println("Facebook: Login sucessfully");
			
			this.initConversation();
			
		}catch(XMPPException e1){
			e1.printStackTrace();
		}
		
		ServletActionContext.getServletContext().setAttribute("faceFlag", "true");
		String facebookFlag=(String)ServletActionContext.getServletContext().getAttribute("faceFlag");
		
		//set the XML format
		stringBuffer.append("<Lists>");
			stringBuffer.append("<facebookFlagInfo>");
			stringBuffer.append(facebookFlag);
			stringBuffer.append("</facebookFlagInfo>");
		stringBuffer.append("</Lists>");
		
		responseOut.print(stringBuffer.toString());
		responseOut.close();
		
		
		
		return SUCCESS;
	}	
	
	
	
	
	
	
	
	
	/**
	 * This method for submitting the dialog to the server.
	 * @return
	 * @throws Exception
	 */
	public String submitChat() throws Exception {
		
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		
		//set response content type 
		response.setContentType("text/xml;charset=UTF-8");
		//no cache for explorer
		response.setHeader("Cache-Control", "no-cache");
		
		String content = java.net.URLDecoder.decode(request
				.getParameter("content"), "UTF-8");//chatContent
		
		String username=(String)session.get(USER_NAME_KEY);
		
		//submit not empty dialog
		if(""!=content && null!=content){
			
			//STEP: show how you can sent a message in the chat conversation
			try {
				ConversationAPI chat = getChat();
				if (chat != null){
					//create a time of the message
					Calendar cal = Calendar.getInstance();
					cal.getTime();
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
					System.out.println("Message sending:"+content+sdf.format(cal.getTime())+username);
					//send the message
					chat.addMessage(new Message(content, sdf.format(cal.getTime()), username));
				} else
					System.out
							.println("Facebook friends: No user is online ");

			} catch (Exception e1) {
				e1.printStackTrace();
			}
			//set flag to true, to tell that new message has already been written.
			ServletActionContext.getServletContext().setAttribute("app_flag","true");

		}

		return SUCCESS;
	}
	
	
	
	
	
	public String checkUpdate() throws IOException{
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		
		response.setContentType("text/xml;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		
		PrintWriter responseOut;
		responseOut = response.getWriter();
		StringBuffer stringBuffer = new StringBuffer();
		
		//check if it has new message (receive/send)
		if(((chatLogMessage)session.get("textAreaChatLogs")).hasMessage())
		{
			ServletActionContext.getServletContext().setAttribute("hasChatFlag", "true");
		}
		
		if(((Predator)session.get("predatorAgeInfo")).getAge()!=0){
			ServletActionContext.getServletContext().setAttribute("ageFlag", "true");
		}
		
		
		String hasChatFlag=(String)ServletActionContext.getServletContext().getAttribute("hasChatFlag");
		String ageFlag=(String)ServletActionContext.getServletContext().getAttribute("ageFlag");
		
		
		//set the XML format, and transfer the app_flag info to the client side.
		stringBuffer.append("<Lists>");
			stringBuffer.append("<hasNewChat>");
			stringBuffer.append(hasChatFlag);
			stringBuffer.append("</hasNewChat>");
			stringBuffer.append("<ageDetect>");
			stringBuffer.append(ageFlag);
			stringBuffer.append("</ageDetect>");
		stringBuffer.append("</Lists>");
		
		responseOut.print(stringBuffer.toString());
		responseOut.close();
		
		return SUCCESS;
	}
	
	
	public String updateInfo() throws IOException{
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		
		
		
		System.out.println("into updateInfo function");
		//set response content type 
		response.setContentType("text/xml;charset=UTF-8");
		//no cache for explorer
		response.setHeader("Cache-Control", "no-cache");
		
		PrintWriter printWriterOut = response.getWriter();
		StringBuffer stringBuffer = new StringBuffer();
		
		String updateType = java.net.URLDecoder.decode(request
				.getParameter("updateType"), "UTF-8");
		
		String updatedChatContent=null, updatedAgeInfo=null;
		
		
		//first integer is 1 equals chat update
		if(Integer.parseInt(updateType.substring(0, 1))==1 ){
			//both chat and age should update
			updatedChatContent=(String)ServletActionContext.getServletContext().getAttribute("wholeChatContent");		
			if(updatedChatContent!="null" && updatedChatContent!=null){
				ServletActionContext.getServletContext().setAttribute("wholeChatContent",updatedChatContent+((chatLogMessage)session.get("textAreaChatLogs")).getMessage());
			}else{
				ServletActionContext.getServletContext().setAttribute("wholeChatContent",((chatLogMessage)session.get("textAreaChatLogs")).getMessage());
			}
			//after read the message, set the flag to false. waiting for another new message comes
			((chatLogMessage)session.get("textAreaChatLogs")).setDefaultMessageFlag();
			//get the new chat content
			updatedChatContent=(String)ServletActionContext.getServletContext().getAttribute("wholeChatContent");
			ServletActionContext.getServletContext().setAttribute("hasChatFlag", "false");
			
		}
		
		if(Integer.parseInt(updateType.substring(1, 2))==1){	
			ServletActionContext.getServletContext().setAttribute("ageInfo",((Predator)session.get("predatorAgeInfo")).getAge());
			updatedAgeInfo=ServletActionContext.getServletContext().getAttribute("ageInfo").toString();
			((Predator)session.get("predatorAgeInfo")).setAge(0);	
			ServletActionContext.getServletContext().setAttribute("ageFlag", "false");
		}
		
		
		//set the XML format
		stringBuffer.append("<Lists>");
			stringBuffer.append("<updatedChatContent>");
			stringBuffer.append(updatedChatContent);
			stringBuffer.append("</updatedChatContent>");
			stringBuffer.append("<updatedAgeInfo>");
			stringBuffer.append(updatedAgeInfo);
			stringBuffer.append("</updatedAgeInfo>");
		stringBuffer.append("</Lists>");
		
		printWriterOut.print(stringBuffer.toString());
		printWriterOut.close();
		
		
		
		return SUCCESS;
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Check the dialog update, if has new info
	 * @return "true" if has new message receiving.
	 * @throws Exception
	 */
	public String checkChatUpdate() throws Exception{
		
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		
		response.setContentType("text/xml;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		
		PrintWriter responseOut;
		responseOut = response.getWriter();
		StringBuffer stringBuffer = new StringBuffer();
		
		//check if it has new message (receive/send)
		if(((chatLogMessage)session.get("textAreaChatLogs")).hasMessage()){
			ServletActionContext.getServletContext().setAttribute("app_flag", "true");
			System.out.println("always has new message");
		}
		
		String app_flag=(String)ServletActionContext.getServletContext().getAttribute("app_flag");
		
		//set the XML format, and transfer the app_flag info to the client side.
		stringBuffer.append("<Lists>");
			stringBuffer.append("<app_flag>");
			stringBuffer.append(app_flag);
			stringBuffer.append("</app_flag>");
		stringBuffer.append("</Lists>");
		
		responseOut.print(stringBuffer.toString());
		responseOut.close();
		return SUCCESS;
	}
	
	
	public String processDialog() throws Exception {
		
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		
		response.setContentType("text/xml;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		
		PrintWriter printWriterOut = response.getWriter();
		StringBuffer stringBuffer = new StringBuffer();

		//in avoid with repeatedly read
		if(((chatLogMessage)session.get("textAreaChatLogs")).hasMessage())
		{
			String chatContent=(String)ServletActionContext.getServletContext().getAttribute("wholeChatContent");
			
			if(chatContent!="null" && chatContent!=null){
				ServletActionContext.getServletContext().setAttribute("wholeChatContent",chatContent+((chatLogMessage)session.get("textAreaChatLogs")).getMessage());
			}else{
				ServletActionContext.getServletContext().setAttribute("wholeChatContent",((chatLogMessage)session.get("textAreaChatLogs")).getMessage());
			}
		}
		
		//after read the message, set the flag to false. waiting for another new message comes
		((chatLogMessage)session.get("textAreaChatLogs")).setDefaultMessageFlag();
		//get the new chat content
		String updatedChatContent=(String)ServletActionContext.getServletContext().getAttribute("wholeChatContent");
		
		//set the XML format
		stringBuffer.append("<Lists>");
			stringBuffer.append("<app_content>");
			stringBuffer.append(updatedChatContent);
			stringBuffer.append("</app_content>");
		stringBuffer.append("</Lists>");
		
		printWriterOut.print(stringBuffer.toString());
		printWriterOut.close();
		return SUCCESS;
	}
	
	
	
	public String ageDetection() throws Exception{
		
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		
		response.setContentType("text/xml;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		
		PrintWriter responseOut;
		responseOut = response.getWriter();
		StringBuffer stringBuffer = new StringBuffer();
		
	//	System.out.println("the age detected is:"+((Predator)session.get("textAreaChatLogs")).getAge());
		if(((Predator)session.get("predatorAgeInfo")).getAge()!=0){
			ServletActionContext.getServletContext().setAttribute("age", "true");
//			System.out.println("age is appear here!");
			
			String ageFlag=(String)ServletActionContext.getServletContext().getAttribute("age");
			
			System.out.println("age Flag value is: "+ageFlag);
			
			//set the XML format, and transfer the app_flag info to the client side.
			stringBuffer.append("<Lists>");
				stringBuffer.append("<age>");
				stringBuffer.append(ageFlag);
				stringBuffer.append("</age>");
			stringBuffer.append("</Lists>");
		}
		
		
	
		
		responseOut.print(stringBuffer.toString());
		responseOut.close();
		return SUCCESS;
	}
	
	
	
	public String getAgeInfo() throws Exception {
		System.out.println("Enter into getAgeInfo Function");
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		
		response.setContentType("text/xml;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		
		PrintWriter printWriterOut = response.getWriter();
		StringBuffer stringBuffer = new StringBuffer();
		
//		String ageInfo = (String)ServletActionContext.getServletContext().getAttribute("ageInfo");
		ServletActionContext.getServletContext().setAttribute("ageInfo",((Predator)session.get("predatorAgeInfo")).getAge());
		
		String updatedAgeInfo=ServletActionContext.getServletContext().getAttribute("ageInfo").toString();
		
		System.out.println("showing the age"+updatedAgeInfo);
		
		((Predator)session.get("predatorAgeInfo")).setAge(0);

		
		//set the XML format
		stringBuffer.append("<Lists>");
			stringBuffer.append("<ageValue>");
			stringBuffer.append(updatedAgeInfo);
			stringBuffer.append("</ageValue>");
		stringBuffer.append("</Lists>");
		
		printWriterOut.print(stringBuffer.toString());
		printWriterOut.close();
		return SUCCESS;
	}
	
	
	
	
	/**
	 * create a conversation (chat session) in the facebook. you should call this method after login to the facebook account.
	 * 
	 * @return The return object is the chat conversation and you use it for sending a message.
	 * @throws ResourceInstantiationException
	 * @throws IOException
	 * @throws XMPPException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 */
	public ConversationAPI getChat() throws ResourceInstantiationException, IOException, XMPPException, XMLStreamException, FactoryConfigurationError{
		if (facebookConversation == null){
			List<RosterEntry> onlineFriends = ((FacebookAPI)session.get("facebookSoenpre")).getOnlineFriends();
		
			if (onlineFriends.size() > 0){
				facebookConversation = new FacebookConversation((ConversationModel)session.get("conversationModel"));
				
				//initiate the conversation from logined user and the first online friend
				facebookConversation.setChat(((FacebookAPI)session.get("facebookSoenpre")), onlineFriends.get(0));
			}
		}
		return facebookConversation;
	}
	
	
	private void initConversation() throws ResourceInstantiationException, IOException, XMLStreamException, FactoryConfigurationError{
		conversationModel = new ConversationModel();
		SerialAnalyserController controller = (SerialAnalyserController)session.get(CONTROLLER_KEY);
		System.err.println("controller"+controller.toString());
		System.err.println("key:"+session.get(USER_NAME_KEY));
		if (controller == null)
			throw new RuntimeException("Can not retrive the nlp application");
		conversationModel.addListener(new ResoluteNLPAnalyzer(controller));
		conversationModel.addListener(this);
//		conversationModel.addListener(new XMLSaver("output/chat.xml"));

	}
	
	
	
	
	
	public String getFacebookUserName() {
		return facebookUserName;
	}

	public void setFacebookUserName(String facebookUserName) {
		this.facebookUserName = facebookUserName;
	}

	public String getFacebookPassword() {
		return facebookPassword;
	}

	public void setFacebookPassword(String facebookPassword) {
		this.facebookPassword = facebookPassword;
	}


	/**
	 * STEP:
	 * show the message to the users. For every messages (sent/recived), this method will be called
	 */
	@Override
	public void newChatMessage(ConversationAPI conversation, Message msg) {
		
		//tell the client, new message is coming
		((chatLogMessage)session.get("textAreaChatLogs")).newMessage();
		//put new message info into session
		((chatLogMessage)session.get("textAreaChatLogs")).setMessage(msg.toString());
		
		
		System.out.println(""+msg.toString());
		Object age = conversation.getDoc().getFeatures().get(RuleBaseAgeDetection.AGE_DOC_FEATURE);

		if (age != null && !age.toString().equals("-1")){
			((Predator)session.get("predatorAgeInfo")).setAge(Integer.parseInt(age.toString()));
			System.out.println("detecting the age: "+age.toString());	
	
		}
	}


	@Override
	public void endChat() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}

	public Map<String, Object> getSession() {
		return session;
	}
	
}
