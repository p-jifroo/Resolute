package ca.concordia.resolute.datamining;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.Attributes;

import ca.concordia.mjlaali.tool.XMLHandler;
import ca.concordia.mjlaali.tool.XMLParser;
import ca.concordia.resolute.core.chat.ConversationAPI;
import ca.concordia.resolute.core.chat.ConversationModel;
import ca.concordia.resolute.core.chat.Message;

/**
 * This class convert the PAN data set to Resolute XML file format which used to import 
 * conversations to GATE.
 * @author mjlaali
 *
 */
public class PANConverter extends XMLHandler{
	public static final String PREDATOR_ID_ATT = "PredatorID";
	public static final String PREDATOR_TYPE = "Predator";
	public static final String NON_PREDATOR_TYPE = "NonPredator";
	public static final String CONVERSATION_TYPE = "Type";
	public static final String TEXT_TAG = "text";
	public static final String TIME_TAG = "time";
	public static final String ID_TAG = "author";
	public static final String CONVERSATION_TAG = "conversation";
	public static final String MESSAGE_TAG = "message";
	public static final String CONVERSATION_ID_PROPERTY = "id";
	
	private ConversationAPI conversation = new ConversationModel();
	private Set<String> predatorsIds = new TreeSet<>();
	private String currentPredator = null;
	private String msg, time, id;
	private String fldOut;
	private int numPredatorChatConversation, totalChatConversation;
	
	/**
	 * @param fldOut the output folder for converted chat conversation.
	 * @throws IOException
	 */
	public PANConverter(String fldOut, String predatorsId) throws IOException {
		super();
		File fld = new File(fldOut);
		if (!fld.exists())
			fld.mkdirs();	
		this.fldOut = fldOut;
		
		loadPredatorIds(predatorsId);
	}

	
	private void loadPredatorIds(String predatorsId) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(predatorsId));
		
		String line;
		while ((line = br.readLine()) != null){
			predatorsIds.add(line.trim());
		}
		br.close();
	}


	@Override
	public void processATag(String tagName, StringBuilder tagText,
			Attributes attributes) {
		if (tagName.equals(TEXT_TAG))
			msg = tagText.toString();
		else if (tagName.equals(TIME_TAG))
			time = tagText.toString();
		else if (tagName.equals(ID_TAG)){
			id = tagText.toString().trim();
			if (predatorsIds.contains(id)){
				if (currentPredator != null && !currentPredator.equals(id)){
					currentPredator += " " + id;
					System.err.println("Multiple predator in the same chat conver");
				} else
					currentPredator = id;
			}
		}
		else if (tagName.equals(MESSAGE_TAG))
			conversation.addMessage(new Message(msg, time, id));
		else if (tagName.equals(CONVERSATION_TAG)){
			try {
				String fileName = fldOut + attributes.getValue(CONVERSATION_ID_PROPERTY) + ".xml";
				BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
				Map<String, String> atts = new TreeMap<>();
				if (currentPredator != null){
					atts.put(PREDATOR_ID_ATT, currentPredator);
					atts.put(CONVERSATION_TYPE, PREDATOR_TYPE);
					++numPredatorChatConversation;
				} else
					atts.put(CONVERSATION_TYPE, NON_PREDATOR_TYPE);
				
				bw.write(conversation.toXML(atts));
				bw.close();
				conversation = new ConversationModel();
				currentPredator = null;
				
				++totalChatConversation;
				if (totalChatConversation % 1000 == 0)
					System.err.println("" + totalChatConversation + " chat conversation is converted. " + numPredatorChatConversation + " conversations have a predator");
			} catch (XMLStreamException
					| FactoryConfigurationError | IOException e) {
				e.printStackTrace();
			}
		}
			
	}
	
	public int getNumPredatorChatConversation() {
		return numPredatorChatConversation;
	}
	
	public int getTotalChatConversation() {
		return totalChatConversation;
	}
	
	public static String PAN_FLD = "/Volumes/Data/Users/Majid/Documents/Course/Concordia/SOEN6951/data-set/PAN 2012/pan12-sexual-predator-identification-training-data-2012-05-01/";
	public static void main(String[] args) throws IOException {
		
		String predatorsIDFile = PAN_FLD + "sexual-predator-identification-pan12-train-predators-2012-05-01.txt";
		String chatConversationFile = "sample.xml";
		XMLParser parser = XMLParser.getInstance();
		parser.parse(new File(PAN_FLD + chatConversationFile), new PANConverter(PAN_FLD + "xml/", predatorsIDFile));
	}

	
}
