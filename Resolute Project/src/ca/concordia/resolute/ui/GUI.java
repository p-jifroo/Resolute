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
import ca.concordia.resolute.core.textmining.gate.RuleBaseAgeDetection;
import ca.concordia.resolute.core.textmining.gate.RuleBaseAgeDetectorApp;

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
				facebookConversation = new FacebookConversation(new ConversationModel());
				facebookConversation.setChat(soenpre, onlineFriends.get(0));
				facebookConversation.addListener(new ResoluteNLPAnalyzer(controller));
				facebookConversation.addListener(this);
				facebookConversation.addListener(new XMLSaver("output/chat.xml"));
			}
		}
		return facebookConversation;
	}
	/**
	 * Create the frame.
	 * @throws GateException 
	 * @throws IOException 
	 */
	public GUI() throws GateException, IOException {
		Gate.init();
		controller = new RuleBaseAgeDetectorApp().getController();
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
		panelChat.add(textAreaChatLogs, BorderLayout.CENTER);
		
		JPanel panelInformation = new JPanel();
		contentPane.add(panelInformation, BorderLayout.SOUTH);
		
		JLabel lblAge = new JLabel("Age:");
		panelInformation.add(lblAge);
		
		textFieldAge = new JTextField();
		textFieldAge.setEnabled(false);
		textFieldAge.setEditable(false);
		panelInformation.add(textFieldAge);
		textFieldAge.setColumns(10);
	}

	@Override
	public void newChatMessage(ConversationAPI conversation, Message msg) {
		textAreaChatLogs.append(msg.toString() + "\n");
		Object age = conversation.getDoc().getFeatures().get(RuleBaseAgeDetection.AGE_DOC_FEATURE);
		if (age != null && !age.toString().equals("-1"))
			textFieldAge.setText(age.toString());
	}

	@Override
	public void endChat() {
		
	}

}
