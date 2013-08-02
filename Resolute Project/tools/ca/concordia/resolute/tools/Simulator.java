package ca.concordia.resolute.tools;

import java.io.IOException;

import org.xml.sax.Attributes;

import ca.concordia.mjlaali.tool.XMLHandler;
import ca.concordia.mjlaali.tool.XMLParser;
import ca.concordia.resolute.core.chat.ConversationAPI;
import ca.concordia.resolute.core.chat.ConversationModel;
import ca.concordia.resolute.core.chat.Message;
import ca.concordia.resolute.core.chat.listener.ConsoleMessage;

public class Simulator extends XMLHandler implements Runnable{
	private ConversationAPI api;
	private long pause;
	private XMLParser parser;
	private String file;
	
	public Simulator(ConversationAPI api, long pause) throws IOException {
		super();
		this.api = api;
		this.pause = pause;
		parser = XMLParser.getInstance();
	}

	@Override
	public void processATag(String tagName, StringBuilder tagText,
			Attributes attributes) {
		if (tagName.equals(Message.XML_TAG_NAME_MESSAGE)){
			api.addMessage(new Message(tagText.toString().trim(), attributes.getValue(Message.XML_ATT_NAME_TIME), 
					attributes.getValue(Message.XML_ATT_NAME_ID)));
			try {
				Thread.sleep(pause);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void simulate(String file){
		this.file = file;
		Thread t = new Thread(this);
        t.start();
    }
	
	public static void main(String[] args) throws IOException {
		ConversationAPI conversation = new ConversationModel();
		conversation.addListener(new ConsoleMessage());
		
		String fld = "/Volumes/Data/Users/Majid/Documents/Course/Concordia/SOEN6951/data-set/PAN 2012/pan12-sexual-predator-identification-training-data-2012-05-01/sampleXML/";
		String file = "0000604306a283600b730276a2039471.xml";
		Simulator simulator = new Simulator(conversation, 2000);
		simulator.simulate(fld + file);
	}

	@Override
	public void run() {
		parser.parse(file, this);
		
	}
	
}
