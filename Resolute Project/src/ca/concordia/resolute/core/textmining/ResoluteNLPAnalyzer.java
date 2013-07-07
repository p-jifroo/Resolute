package ca.concordia.resolute.core.textmining;

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

import ca.concordia.resolute.core.chat.ChatMessageListener;
import ca.concordia.resolute.core.chat.Conversation;
import ca.concordia.resolute.core.chat.Message;

public class ResoluteNLPAnalyzer implements ChatMessageListener{
	
	private SerialAnalyserController controller;
	private Corpus corpus = Factory.newCorpus("Corpus");
	private File filename;
	
	public ResoluteNLPAnalyzer(File gateAppFile) throws PersistenceException, ResourceInstantiationException, IOException {
		controller = (SerialAnalyserController)
				PersistenceManager.loadObjectFromFile(gateAppFile);
		controller.setCorpus(corpus);
		
		String property = "java.io.tmpdir";
	    String tempDir = System.getProperty(property);
	    File dir = new File(tempDir);
	    filename = File.createTempFile("resolute", ".tmp", dir);
	}

	private void saveToTmpFile(String content) throws IOException{
	    BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true));
	    bw.write(content);
	    bw.close();
	}
	
	@Override
	public void newChatMessage(Conversation conversation, Message msg) {
		try {
			saveToTmpFile(conversation.toXML());
			Document doc = Factory.newDocument(filename.toURI().toURL());
			corpus.add(doc);
			controller.execute();
			corpus.clear();
			Object prevDoc = conversation.setDoc(doc);
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
