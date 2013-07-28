package ca.concordia.mjlaali.tool;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

/**
 * An xml parser. It is singleton class. For instantiating and using this class, call
 * {@link #getInstance()} function.		
 * @author Majid
 *
 */
public class XMLParser {
	private static XMLParser instance = null;
	private SAXParser saxParser;


	private XMLParser() {
		create();
	}


	private void create() {
		try {
			// Obtain a new instance of a SAXParserFactory.
			SAXParserFactory factory = SAXParserFactory.newInstance();
			// Specifies that the parser produced by this code will provide support for XML namespaces.
			factory.setNamespaceAware(true);
			// Specifies that the parser produced by this code will validate documents as they are parsed.
			factory.setValidating(true);
			// Creates a new instance of a SAXParser using the currently configured factory parameters.
			saxParser = factory.newSAXParser();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void parse(String uri, DefaultHandler handler){
		try{
			saxParser.parse(uri, handler);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}


	public void parse(File file, DefaultHandler handler){
		try{
			saxParser.parse(file, handler);
		} catch (Throwable t) {
			System.err.print("XMLParser.parse(): file parse error { ");
			System.err.println(file.getAbsolutePath() + " }");
			t.printStackTrace();
		}
	}

	public static XMLParser getInstance() {
		if (instance == null){
			instance = new XMLParser();
		}
		
		return instance;
	}
}