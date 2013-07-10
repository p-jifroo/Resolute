package ca.concordia.resolute.core.chat;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Message class represents a text message in a chat.
 * @author mjlaali
 *
 */
public class Message {
	public static final String XML_ATT_NAME_ID = "ID";
	public static final String XML_ATT_NAME_TIME = "TIME";
	public static final String XML_TAG_NAME_MESSAGE = "MESSAGE";
	private String msg, time, id;

	/**
	 * Convention method to construct a Message class
	 * @param msg the text of message
	 * @param time the time of sending this text
	 * @param id the chat ID of the person
	 */
	public Message(String msg, String time, String id) {
		this.msg = convertToUTF8(msg);
		this.time = time;
		
		this.id = id;
	}
	
	/**
	 * Convert this message to XML. See {@link Conversation#toXML()} for more detail.
	 * @param out an XMLStreamWriter to store the XML representaion of this class.
	 * @throws XMLStreamException
	 */
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
	
	/**
	 * Convert a string to UTF-8 format and replace any forbidden UTF-8 character with a space
	 * @param inString the input string
	 * @return the converted string
	 */
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
	
	/**
	 * 
	 * @return the text of chat message
	 */
	public String getMsg() {
		return msg;
	}
	
	/**
	 * 
	 * @return the id of person who send message
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * 
	 * @return the time when chat message has been sent
	 */
	public String getTime() {
		return time;
	}
}
