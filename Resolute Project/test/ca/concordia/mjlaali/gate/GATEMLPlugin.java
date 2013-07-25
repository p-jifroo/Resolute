package ca.concordia.mjlaali.gate;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.ProcessingResource;
import gate.creole.ANNIEConstants;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import weka.core.converters.ConverterUtils.DataSource;

public class GATEMLPlugin {

	@BeforeClass
	public static void init() throws GateException{
		Gate.init();
		Gate.getCreoleRegister().registerComponent(FeatureExtractor.class);
	}

	@Test
	public void extractFeatures() throws Exception{
		Document doc = Factory.newDocument("it is a test.");
		Document doc2 = Factory.newDocument("it is another test.");

		SerialAnalyserController app = (SerialAnalyserController) 
				PersistenceManager.loadObjectFromFile(new File(new File( 
						Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR), 
						ANNIEConstants.DEFAULT_FILE));

		app.add((ProcessingResource)Factory.createResource(FeatureExtractor.class.getName()));

		Corpus corpus = Factory.newCorpus("corpus");
		corpus.add(doc);
		corpus.add(doc2);

		app.setCorpus(corpus);
		app.execute();

		DataSource source = new DataSource(FeatureExtractor.DEFUALT_OUTFILE);
		source.getDataSet();
	}

	public void learnClassifier() throws Exception{
		Document doc = Factory.newDocument("it is a test.");
		Document doc2 = Factory.newDocument("it is another test.");

		SerialAnalyserController app = (SerialAnalyserController) 
				PersistenceManager.loadObjectFromFile(new File(new File( 
						Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR), 
						ANNIEConstants.DEFAULT_FILE));

		app.add((ProcessingResource)Factory.createResource(FeatureExtractor.class.getName()));

		Corpus corpus = Factory.newCorpus("corpus");
		corpus.add(doc);
		corpus.add(doc2);

		app.setCorpus(corpus);
		app.execute();

		DataSource source = new DataSource(FeatureExtractor.DEFUALT_OUTFILE);
		source.getDataSet();
		
	}
}
