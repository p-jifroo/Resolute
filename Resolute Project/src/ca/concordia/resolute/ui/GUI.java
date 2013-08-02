package ca.concordia.resolute.ui;

import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

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
import ca.concordia.resolute.tools.Simulator;

public class GUI extends JFrame implements ChatMessageListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textFieldPassword;
	private JTextField textFieldUsername;
	private JTextField textFieldAge;
	private JTextArea textAreaChatLogs;
	
	private FacebookAPI soenpre = new FacebookAPI();
	private SerialAnalyserController controller;
	private FacebookConversation facebookConversation = null;
	private JTextField textFieldChatLog;
	private JButton btnLogin;
	private JTextField textFieldPredatorProbability;
	private JLabel lblPredatorProb;
	private JButton btnSimulate;
	
	private ConversationModel conversationModel;
	private Simulator simulator;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ConversationAPI getChat() throws ResourceInstantiationException, IOException, XMPPException, XMLStreamException, FactoryConfigurationError{
		if (facebookConversation == null){
			List<RosterEntry> onlineFriends = soenpre.getOnlineFriends();
			if (onlineFriends.size() > 0){
				facebookConversation = new FacebookConversation(conversationModel);
				facebookConversation.setChat(soenpre, onlineFriends.get(0));
			}
		}
		return facebookConversation;
	}
	
	private void initConversation() throws ResourceInstantiationException, IOException, XMLStreamException, FactoryConfigurationError{
		conversationModel = new ConversationModel();
		conversationModel.addListener(new ResoluteNLPAnalyzer(controller));
		conversationModel.addListener(this);
		conversationModel.addListener(new XMLSaver("output/chat.xml"));

	}
	/**
	 * Create the frame.
	 * @throws GateException 
	 * @throws IOException 
	 * @throws FactoryConfigurationError 
	 * @throws XMLStreamException 
	 */
	public GUI() throws GateException, IOException, XMLStreamException, FactoryConfigurationError {
		Gate.init();
		controller = new ResouluteApp().getController();
		initConversation();
		simulator = new Simulator(conversationModel, 2000);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 618, 506);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panleLogin = new JPanel();
		contentPane.add(panleLogin, BorderLayout.NORTH);
		
		JLabel lblUserName = new JLabel("User name:");
		panleLogin.add(lblUserName);
		
		textFieldUsername = new JTextField();
		textFieldUsername.setText("soen.resolute");
		panleLogin.add(textFieldUsername);
		textFieldUsername.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Password");
		panleLogin.add(lblNewLabel);
		
		textFieldPassword = new JTextField();
		textFieldPassword.setText("123ABC!");
		panleLogin.add(textFieldPassword);
		textFieldPassword.setColumns(10);
		
		btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					soenpre.connect();
					soenpre.login(textFieldUsername.getText(), textFieldPassword.getText());
					btnLogin.setEnabled(false);
					System.out
							.println("GUI.GUI().new ActionListener() {...}.actionPerformed(): Login sucessfully");
				} catch (XMPPException e1) {
					e1.printStackTrace();
				}
			}
		});
		panleLogin.add(btnLogin);
		
		btnSimulate = new JButton("Simulate");
		btnSimulate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fld = "/Volumes/Data/Users/Majid/Documents/Course/Concordia/SOEN6951/data-set/PAN 2012/pan12-sexual-predator-identification-training-data-2012-05-01/sampleXML/";
				String file = "0000604306a283600b730276a2039471.xml";
				String predatorFile = "/Volumes/Data/Users/Majid/Documents/Course/Concordia/SOEN6951/data-set/PAN%202012/pan12-sexual-predator-identification-training-data-2012-05-01/xml/cf6cafbf41d2bd9b32ca79f8d7a0c2d2.xml";
				String j48file = "/Volumes/Data/Users/Majid/Documents/Course/Concordia/SOEN6951/data-set/PAN%202012/pan12-sexual-predator-identification-training-data-2012-05-01/xml/cd5fb308f9ffb74ebaf0142ea9c8266e.xml";
				simulator.simulate(j48file);
			}
		});
		panleLogin.add(btnSimulate);
		
		JPanel panelChat = new JPanel();
		contentPane.add(panelChat, BorderLayout.CENTER);
		panelChat.setLayout(new BorderLayout(0, 0));
		
		textFieldChatLog = new JTextField();
		textFieldChatLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					ConversationAPI chat = getChat();
					if (chat != null){
						Calendar cal = Calendar.getInstance();
						cal.getTime();
						SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
						chat.addMessage(new Message(textFieldChatLog.getText(), sdf.format(cal.getTime()), textFieldUsername.getText()));
					} else
						System.out
								.println("GUI.GUI().new ActionListener() {...}.actionPerformed(): No user is online");
					textFieldChatLog.setText("");
				} catch (ResourceInstantiationException | IOException
						| XMPPException | XMLStreamException | FactoryConfigurationError e1) {
					e1.printStackTrace();
				}

			}
		});
		panelChat.add(textFieldChatLog, BorderLayout.SOUTH);
		textFieldChatLog.setColumns(10);
		
		textAreaChatLogs = new JTextArea();
		textAreaChatLogs.setEnabled(false);
		textAreaChatLogs.setEditable(false);
		JScrollPane sp = new JScrollPane(textAreaChatLogs);
		panelChat.add(sp, BorderLayout.CENTER);
		
		JPanel panelInformation = new JPanel();
		contentPane.add(panelInformation, BorderLayout.SOUTH);
		
		JLabel lblAge = new JLabel("Age:");
		panelInformation.add(lblAge);
		
		textFieldAge = new JTextField();
		textFieldAge.setEnabled(false);
		textFieldAge.setEditable(false);
		panelInformation.add(textFieldAge);
		textFieldAge.setColumns(10);
		
		lblPredatorProb = new JLabel("Predator Probability:");
		panelInformation.add(lblPredatorProb);
		
		textFieldPredatorProbability = new JTextField();
		textFieldPredatorProbability.setEnabled(false);
		textFieldPredatorProbability.setEditable(false);
		panelInformation.add(textFieldPredatorProbability);
		textFieldPredatorProbability.setColumns(10);
	}

	@Override
	public void newChatMessage(ConversationAPI conversation, Message msg) {
		textAreaChatLogs.append(msg.toString() + "\n");
		Object age = conversation.getDoc().getFeatures().get(RuleBaseAgeDetection.AGE_DOC_FEATURE);
		if (age != null && !age.toString().equals("-1"))
			textFieldAge.setText(age.toString());
		textFieldPredatorProbability.setText(conversation.getDoc().getFeatures().get(PredatorDetector.PREDATOR_PROB_DOC_FEATURE).toString());
	}

	@Override
	public void endChat() {
		
	}

}
