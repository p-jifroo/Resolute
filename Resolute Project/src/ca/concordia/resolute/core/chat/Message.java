package ca.concordia.resolute.core.chat;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class Message {
	public static final String XML_ATT_NAME_ID = "ID";
	public static final String XML_ATT_NAME_TIME = "TIME";
	public static final String XML_TAG_NAME_MESSAGE = "MESSAGE";
	private String msg, time, id;

	public Message(String msg, String time, String id) {
		this.msg = convertToUTF8(msg);
		this.time = time;
		
		this.id = id;
	}
	
	public void toXML(XMLStreamWriter out) throws XMLStreamException{
		out.writeStartElement(XML_TAG_NAME_MESSAGE);
		out.writeAttribute(XML_ATT_NAME_TIME, time);
		out.writeAttribute(XML_ATT_NAME_ID, id);

		out.writeCharacters(msg + "\n");

		out.writeEndElement();
		out.flush();

	}
	
	@Override
	public String toString() {
		return id + " (" + time + ") --> " + msg;
	}
	
	public static void main(String[] args) throws XMLStreamException, FactoryConfigurationError {
		Message message = new Message("Hey, how's going, you & family", "12/03/2013 5:30", "ka_reso");
		message.toXML(XMLOutputFactory.newInstance().createXMLStreamWriter(System.out));
		System.out.println("Message.main()");
	}
	
	public static String convertToUTF8(String inString)
	{
	    if (inString == null) return null;

	    StringBuilder newString = new StringBuilder();
	    char ch;

	    for (int i = 0; i < inString.length(); i++)
	    {

	        ch = inString.charAt(i);
	        // remove any characters outside the valid UTF-8 range as well as all control characters
	        // except tabs and new lines
	        if ((ch < 0x00FD && ch > 0x001F) || ch == '\t' || ch == '\n' || ch == '\r'){
	            newString.append(ch);
	        } else
	        	newString.append(' ');
	    }
	    return newString.toString();

	}
	
	public String getMsg() {
		return msg;
	}
	
	public String getId() {
		return id;
	}
	
	public String getTime() {
		return time;
	}
}
