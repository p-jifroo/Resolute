package ca.concordia.resolute.datamining;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import ca.concordia.mjlaali.tool.XMLParser;

public class PANConverterTest {

	@Test
	public void checkOnSampleXML() throws IOException{
		String panDataSetFolder = "/Volumes/Data/Users/Majid/Documents/Course/Concordia/SOEN6951/data-set/PAN 2012/pan12-sexual-predator-identification-training-data-2012-05-01/";
		
		String predatorsIDFile = panDataSetFolder + "sexual-predator-identification-pan12-train-predators-2012-05-01.txt";
//		String chatConversationFile = "sexual-predator-identification-pan12-train-2012-05-01.xml";
		String chatConversationFile = "sample.xml";
		XMLParser parser = XMLParser.getInstance();
		PANConverter panConverter = new PANConverter(panDataSetFolder + "xml/", predatorsIDFile);
		parser.parse(new File(panDataSetFolder + chatConversationFile), panConverter);
		Assert.assertEquals(132, panConverter.getTotalChatConversation());
		Assert.assertEquals(3, panConverter.getNumPredatorChatConversation());
	}
	
	@Test
	public void checkOnPanFile() throws IOException{
		String panDataSetFolder = "/Volumes/Data/Users/Majid/Documents/Course/Concordia/SOEN6951/data-set/PAN 2012/pan12-sexual-predator-identification-training-data-2012-05-01/";
		
		String predatorsIDFile = panDataSetFolder + "sexual-predator-identification-pan12-train-predators-2012-05-01.txt";
		String chatConversationFile = "sexual-predator-identification-pan12-train-2012-05-01.xml";
		XMLParser parser = XMLParser.getInstance();
		PANConverter panConverter = new PANConverter(panDataSetFolder + "xml/", predatorsIDFile);
		parser.parse(new File(panDataSetFolder + chatConversationFile), panConverter);
		System.out.println("" + panConverter.getTotalChatConversation() + " conversation is converted.");
		System.out.println("The conversations include " + panConverter.getNumPredatorChatConversation() + " conversations belong to a predator.");
	}
}
