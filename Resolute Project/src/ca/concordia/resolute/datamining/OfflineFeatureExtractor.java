package ca.concordia.resolute.datamining;

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
import java.util.Date;

import ca.concordia.mjlaali.tool.ConsolProgressBar;

public class OfflineFeatureExtractor {
	private Corpus persistCorp;
	private SerialDataStore sds;
	private SerialAnalyserController controller;

	public OfflineFeatureExtractor(String datastoreLocation, String corpusName) throws PersistenceException, ResourceInstantiationException, 
	IOException, UnsupportedOperationException, SecurityException{
		controller = (SerialAnalyserController) 
				PersistenceManager.loadObjectFromFile(new File(new File( 
						Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR), 
						ANNIEConstants.DEFAULT_FILE));
		initPresistCorpus(datastoreLocation, corpusName);
	}
	
	public void test() throws ResourceInstantiationException, ExecutionException, PersistenceException, IOException{
		Corpus corpus = Factory.newCorpus("test");
		Document doc = Factory.newDocument("it is a test");
		Document doc2 = Factory.newDocument("it is a test");
		
		controller.setCorpus(corpus);
		controller.setDocument(doc);
		controller.execute();
		controller.setDocument(doc2);
		controller.execute();
		System.out.println(doc);
		
	}
	
	public void load(File fld) throws ResourceInstantiationException, MalformedURLException, IOException, PersistenceException, SecurityException{
		Corpus corpus = Factory.newCorpus("test");
		controller.setCorpus(corpus);
		
		File[] files = fld.listFiles();
		ConsolProgressBar progressbar = new ConsolProgressBar(files.length, 100);
//		int cnt = 0;
		for (File f: files){
			if (f.getName().endsWith(".xml")){
//				++cnt;
				Document document = Factory.newDocument(f.toURI().toURL());
				controller.setDocument(document);
				try {
					controller.execute();
					persistCorp.add(document);
					persistCorp.sync();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
//				if (cnt % 1000 == 0)
				Factory.deleteResource(document);
				
			}
			progressbar.progress(1L);
		}
		persistCorp.sync();
		sds.close();
	}
	
	public void initPresistCorpus(String datastoreLocation, String name) throws ResourceInstantiationException, PersistenceException, UnsupportedOperationException, MalformedURLException, SecurityException{
		File DS_DIR = new File(datastoreLocation);

		Corpus corpus = Factory.newCorpus(name);
		sds = (SerialDataStore)Factory.createDataStore("gate.persist.SerialDataStore",
				DS_DIR.toURI().toURL().toString());
		sds.open();

		persistCorp = (Corpus)sds.adopt(corpus,null);
		sds.sync(persistCorp);
	}
	
	
	
	public static void main(String[] args) throws MalformedURLException, IOException, GateException {
		Gate.init();
		Date date = new Date();
		System.out.println(date.getTime());
		String fld = PANConverter.PAN_FLD + "xml/";
		OfflineFeatureExtractor offlineFeatureExtractor = new OfflineFeatureExtractor(PANConverter.PAN_FLD + "sds/", "PAN dataset");
//		offlineFeatureExtractor.test();
		offlineFeatureExtractor.load(new File(fld));
		date = new Date();
		System.out.println(date.getTime());
	}
}
