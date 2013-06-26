package ca.concordia.resolute.tool;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A handler for XML parser. This handler should be use for XML SAX parser. In the handler, 
 * each tag, with its text and attribute is saved in a stack. To full parse, it is needed to extend 
 * this class and implement {@link #processATag(String, StringBuilder, Attributes)} function. 
 * @author Majid
 *
 */
public abstract class XMLHandler extends DefaultHandler implements FileProcessor{
    private LinkedList<String> seenTag = new LinkedList<String>();
    private LinkedList<StringBuilder> buffers = new LinkedList<StringBuilder>();
    private LinkedList<Attributes> attributes = new LinkedList<Attributes>();

    public XMLHandler() throws IOException {
	}

    /**
     * The content of each tag is given in this function.
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        String buf = new String(ch, start, length);
        buffers.getFirst().append(buf);
    }

    /**
     * This function indicate reading a XML tag is finished. you can store your data or structure here
     */
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        String tagName = seenTag.pop();
        StringBuilder buffer = buffers.pop();
        Attributes attribute = attributes.pop();
        processATag(tagName, buffer, attribute);
    }

    /**
     * Implement this function as you need.
     * @param tagName: the name of the tag
     * @param tagText: the text of the tag
     * @param attributes: all attribute of the tag
     */
    public abstract void processATag(String tagName, StringBuilder tagText, Attributes attributes);
	
	/**
     * This function start whenever the parser see a tag. You should save tag name.
     * The tag name is store in localName
     */
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException {
    	
    	seenTag.push(localName);
    	buffers.push(new StringBuilder());
    	attributes.push(new AttributesImpl(atts));
    }
    
    /**
     * parse an XML file 
     */
    public void process(File file) {
    	if (file.getName().endsWith(".xml"))
    		XMLParser.getInstance().parse(file, this);
    	else
    		System.err.println("XMLHandler.process(): Folder contain non-xml file: " + file.getAbsolutePath());
    };
}
