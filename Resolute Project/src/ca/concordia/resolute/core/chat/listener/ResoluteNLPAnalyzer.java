package ca.concordia.resolute.core.chat.listener;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Resource;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.persist.PersistenceException;
import gate.util.persistence.PersistenceManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import ca.concordia.resolute.core.chat.ConversationAPI;
import ca.concordia.resolute.core.chat.ChatMessageListener;
import ca.concordia.resolute.core.chat.ConversationModel;
import ca.concordia.resolute.core.chat.Message;

/**
 * An observer for the chat conversation ({@link ConversationModel}) in which do text mining on the chat messages and elicit 
 * required information from it. 
 * @author mjlaali
 *
 */
public class ResoluteNLPAnalyzer implements ChatMessageListener{
	
	private SerialAnalyserController controller;
	private Corpus corpus = Factory.newCorpus("Corpus");
	private File filename;
	
	/**
	 * Construct a {@link ResoluteNLPAnalyzer} and load a GATE application as text mining model.
	 * @param gateAppFile
	 * @throws PersistenceException
	 * @throws ResourceInstantiationException
	 * @throws IOException
	 */
	public ResoluteNLPAnalyzer(File gateAppFile) throws PersistenceException, ResourceInstantiationException, IOException {
		this((SerialAnalyserController)
				PersistenceManager.loadObjectFromFile(gateAppFile));
	}
	
	public ResoluteNLPAnalyzer(SerialAnalyserController controller) throws IOException, ResourceInstantiationException {
		this.controller = controller;
		controller.setCorpus(corpus);
		
		String property = "java.io.tmpdir";
	    String tempDir = System.getProperty(property);
	    File dir = new File(tempDir);
	    filename = File.createTempFile("resolute", ".tmp", dir);
	}
	

	private void saveToTmpFile(String content) throws IOException{
		filename.delete();
	    BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true));
	    bw.write(content);
	    bw.close();
	}
	
	/**
	 * Any text mining process is done here. All text processes is model in the GATE application. The output of text mining
	 * process is store in the conversation's GATE document.
	 */
	@Override
	public void newChatMessage(ConversationAPI conversation, Message msg) {
		try {
			saveToTmpFile(conversation.toXML(null));
			Document doc = Factory.newDocument(filename.toURI().toURL());
			corpus.add(doc);
			controller.setCorpus(corpus);
			controller.setDocument(doc);
			controller.execute();
			corpus.clear();
			Object prevDoc = conversation.setDoc(doc);
			if (prevDoc != null)
				Factory.deleteResource((Resource) prevDoc);
		} catch (ResourceInstantiationException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException | XMLStreamException
				| FactoryConfigurationError e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


	@Override
	public void endChat() {
		
	}

}
