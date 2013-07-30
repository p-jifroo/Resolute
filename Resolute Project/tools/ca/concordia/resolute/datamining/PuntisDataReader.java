package ca.concordia.resolute.datamining;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import ca.concordia.resolute.core.chat.ConversationModel;
import ca.concordia.resolute.core.chat.ConversationAPI;
import ca.concordia.resolute.core.chat.Message;
import ca.concordia.resolute.core.chat.listener.XMLSaver;

/**
 * This class loads text file chat conversation to a {@link ConversationModel}
 * @author mjlaali
 *
 */
public class PuntisDataReader {

	private final String LINE_REGEX = "(.*)\\(([^)]*)\\):(.*)";
	private final Pattern LINE_PATTERN = Pattern.compile(LINE_REGEX);
	
	private Message parseLine(String line){
		if (line.trim().length() == 0)
			return null;
		
		Matcher matcher = LINE_PATTERN.matcher(line);
		if (matcher.matches()){
			String id = matcher.group(1).trim();
			String time = matcher.group(2).trim();
			String msg = matcher.group(3).trim();
			return new Message(msg, time, id);
		} else {
			throw new RuntimeException(line);
		}
	}
	
	/**
	 * load a chat conversation
	 * @param file a text file of chat conversation
	 * @return loaded chat conversation
	 * @throws IOException
	 */
	public ConversationAPI importFromFile(File file) throws IOException{
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		ConversationAPI conversation = new ConversationModel();
		while ((line = br.readLine()) != null){
			Message msg = parseLine(line);
			if (msg != null)
				conversation.addMessage(msg);
		}
		
		conversation.endChat();
		br.close();
		return conversation;
	}
	
	/**
	 * convert all text chat conversations with ".txt" extension in a directory to XML file format
	 * @param dir directory contains all text file
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 */
	public void convertToXML(File dir) throws IOException, XMLStreamException, FactoryConfigurationError{
		for (File f: dir.listFiles()){
			if (f.isFile() && f.getName().endsWith(".txt")){
				ConversationAPI conversation = importFromFile(f);
				conversation.addListener(new XMLSaver(f.getAbsolutePath().replace(".txt", ".xml")));
				conversation.simulate();
			}
		}
		
	}
	
	public static void main(String[] args) throws IOException, XMLStreamException, FactoryConfigurationError {
		String dataSetFld = "/Volumes/Data/Users/Majid/Documents/Course/Concordia/SOEN6951/data-set/PervertedJustice/Puntis";
		PuntisDataReader puntisDataReader = new PuntisDataReader();
		puntisDataReader.convertToXML(new File(dataSetFld));
		System.out.println("PuntisDataReader.main(): Done!");
	}
}
