package ca.concordia.mjlaali.gate;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.creole.ANNIEConstants;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.persist.PersistenceException;
import gate.persist.SerialDataStore;
import gate.security.SecurityException;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.BeforeClass;
import org.junit.Test;

import ca.concordia.resolute.datamining.PANConverter;

public class GATEApi {


	public static SerialAnalyserController controller;
	@BeforeClass
	public static void init() throws GateException, IOException{
		Gate.init();
		controller = (SerialAnalyserController) 
				PersistenceManager.loadObjectFromFile(new File(new File( 
						Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR), 
						ANNIEConstants.DEFAULT_FILE));

	}

	public void deleteFile(File file){
		if (file.isDirectory())
			for (File f: file.listFiles())
				deleteFile(f);

		file.delete();
	}

	@Test
	public void presistOutput() throws ResourceInstantiationException, MalformedURLException, ExecutionException, PersistenceException, UnsupportedOperationException, SecurityException{
		String sampelDocXMLFile = PANConverter.PAN_FLD + "xml/0a0acab63770dcec26a3c5e5b4cf2d30.xml";
		File DS_DIR = new File("output/test/dsDir");

		deleteFile(DS_DIR);
		DS_DIR.mkdirs();

		Document doc = Factory.newDocument(new File(sampelDocXMLFile).toURI().toURL());
		Corpus corpus = Factory.newCorpus("corpus");

		corpus.add(doc);
		controller.setCorpus(corpus);
		controller.execute();

		//create&open a new Serial Data Store
		//    pass the datastore class and path as parameteres
		SerialDataStore sds  = (SerialDataStore)Factory.createDataStore("gate.persist.SerialDataStore",
				DS_DIR.toURI().toURL().toString());
		sds.open();

		Corpus persistCorp = (Corpus)sds.adopt(corpus,null);
		sds.sync(persistCorp);
		
		sds.close();
		
		Factory.deleteResource(doc);
	}

}
