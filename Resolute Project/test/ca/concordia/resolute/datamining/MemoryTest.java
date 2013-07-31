package ca.concordia.resolute.datamining;

import gate.Corpus;
import gate.DataStore;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.corpora.SerialCorpusImpl;
import gate.persist.SerialDataStore;
import gate.util.GateException;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

import org.junit.Test;

import ca.concordia.mjlaali.gate.GATEMLPlugin;
import ca.concordia.mjlaali.tool.ConsolProgressBar;

public class MemoryTest {

	@SuppressWarnings("unchecked")
	@Test
	public void memoryleak() throws MalformedURLException, GateException{
		File path = new File(GATEMLPlugin.PAN_DATASTORE_LOC);
		Gate.init();

		SerialDataStore sds = new SerialDataStore(path.toURI().toURL().toString());
		sds.open();
		List<String> corpusID = sds.getLrIds(SerialCorpusImpl.class.getName());

		FeatureMap corpFeatures = Factory.newFeatureMap();
		corpFeatures.put(DataStore.LR_ID_FEATURE_NAME, corpusID.get(0));
		corpFeatures.put(DataStore.DATASTORE_FEATURE_NAME, sds);
		
		//tell the factory to load the Serial Corpus with the specified ID from the specified  datastore
		Corpus persistanceCorpus = (Corpus)Factory.createResource(SerialCorpusImpl.class.getName(), corpFeatures);

		ConsolProgressBar progressBar = new ConsolProgressBar(persistanceCorpus.size(), 100);
//		Corpus dummyCorpus = Factory.newCorpus("dummy");
		for (int i = 0; i < persistanceCorpus.size(); ++i){
			//load document
			Document doc = persistanceCorpus.get(i);
			Document copyDoc = (Document)Factory.duplicate(doc);
			copyDoc.getAnnotations().addAll(doc.getAnnotations());
			
			
			
			//unload document
			Factory.deleteResource(copyDoc);
			persistanceCorpus.unloadDocument(doc);
			
			progressBar.progress(1L);
//			dummyCorpus.add(copyDoc);
//			dummyCorpus.
		}
	}

}
