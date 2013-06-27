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
		this.msg = msg;
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
}
