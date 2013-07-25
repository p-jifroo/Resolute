package ca.concordia.resolute.core.chat.listener;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import ca.concordia.resolute.core.chat.ChatMessageListener;
import ca.concordia.resolute.core.chat.ConversationAPI;
import ca.concordia.resolute.core.chat.Message;

/**
 * A observer for the chat conversation that is store the chat message as an XML file.
 * @author mjlaali
 *
 */
public class XMLSaver implements ChatMessageListener{
	private XMLStreamWriter out;
	public XMLSaver(String fileName) throws UnsupportedEncodingException, FileNotFoundException, XMLStreamException, FactoryConfigurationError {
		this(new FileOutputStream(fileName));
	}
	
	public XMLSaver(OutputStream outputStream) throws UnsupportedEncodingException, XMLStreamException, FactoryConfigurationError {
		out = XMLOutputFactory.newInstance().createXMLStreamWriter(
				new OutputStreamWriter(outputStream, "UTF-8"));
		out.writeStartDocument("UTF-8","1.0");
		out.writeStartElement("CONVERSATION");
	}

	@Override
	public void newChatMessage(ConversationAPI conversation, Message msg) {
		try {
			msg.toXML(out);
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void endChat() {
		try {
			out.writeEndElement();
			out.writeEndDocument();
			out.flush();
			out.close();
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

}
