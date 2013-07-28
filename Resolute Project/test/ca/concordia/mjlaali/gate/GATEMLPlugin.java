package ca.concordia.mjlaali.gate;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.ProcessingResource;
import gate.creole.ANNIEConstants;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.net.MalformedURLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import weka.core.converters.ConverterUtils.DataSource;
import ca.concordia.mjlaali.gate.ml.DocumentInstance;
import ca.concordia.mjlaali.gate.ml.FeatureExtractor;
import ca.concordia.mjlaali.gate.ml.attributeCalculator.FeatureValue;
import ca.concordia.mjlaali.gate.ml.wekaExporter.BooleanVectorModel;
import ca.concordia.mjlaali.gate.ml.wekaExporter.NumeratorRepresenter;
import ca.concordia.mjlaali.gate.ml.wekaExporter.VectorModel;
import ca.concordia.resolute.core.chat.listener.XMLSaver;
import ca.concordia.resolute.datamining.PANConverter;
import ca.concorida.resolute.core.textmining.RuleBaseAgeDetectionTest;

public class GATEMLPlugin {
	private static Corpus presistanceCorpus;
	public static final String DEFUALT_OUTFILE = "output/";
	public static final String ORIGINAL_MARKUPS = "Original markups";

	public static final String PAN_DATASTORE_LOC = 
			"/Volumes/Data/Users/Majid/Documents/Course/Concordia/SOEN6951/data-set/PAN 2012/pan12-sexual-predator-identification-training-data-2012-05-01/sds";

	@BeforeClass
	public static void init() throws GateException, MalformedURLException{
		Gate.init();
		Gate.getCreoleRegister().registerComponent(FeatureExtractor.class);
		presistanceCorpus = RuleBaseAgeDetectionTest.readPresistanceCorpus(new File(PAN_DATASTORE_LOC));
		initTestCorpus();
	}
	
	private static Corpus testCorpus;

	public static void initTestCorpus() throws ResourceInstantiationException{
		testCorpus = Factory.newCorpus("test corpus");
		int cnt = 1000;
		for (Document doc: presistanceCorpus){
			Document copyDoc = (Document)Factory.duplicate(doc);
			copyDoc.getAnnotations().addAll(doc.getAnnotations());
			testCorpus.add(copyDoc);
			if (testCorpus.size() == cnt)
				break;
		}
	}

	@AfterClass
	public static void releaseCorpus() throws ResourceInstantiationException{
		for (Document doc: presistanceCorpus){
			Factory.deleteResource(doc);
		}
		Factory.deleteResource(testCorpus);
	}
	
	@Ignore
	@Test
	public void extractFeatures() throws Exception{
		Document doc = Factory.newDocument("it is a test.");
		Document doc2 = Factory.newDocument("it is another test.");

		SerialAnalyserController app = (SerialAnalyserController) 
				PersistenceManager.loadObjectFromFile(new File(new File( 
						Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR), 
						ANNIEConstants.DEFAULT_FILE));

		String outfile = DEFUALT_OUTFILE + "simple.arff";
		FeatureExtractor featureExtrator = (FeatureExtractor)Factory.createResource(FeatureExtractor.class.getName());
		FeatureValue attributeCalculator = new FeatureValue(null, ANNIEConstants.TOKEN_ANNOTATION_TYPE, ANNIEConstants.TOKEN_STRING_FEATURE_NAME);
		attributeCalculator.setWekaExporter(new BooleanVectorModel(new VectorModel()));
		featureExtrator.setAttributeCalculator(attributeCalculator);
		featureExtrator.setInstanceExtractor(new DocumentInstance());
		featureExtrator.setExportFileName(outfile);
		
		app.add(featureExtrator);

		Corpus corpus = Factory.newCorpus("corpus");
		corpus.add(doc);
		corpus.add(doc2);

		app.setCorpus(corpus);
		app.execute();

		DataSource source = new DataSource(outfile);
		source.getDataSet();
	}
	
	@Test
	public void compositFeature() throws Exception{
		SerialAnalyserController app = (SerialAnalyserController)Factory.createResource(SerialAnalyserController.class.getName(), 
				Factory.newFeatureMap(), Factory.newFeatureMap(), "Simple app");
		
		String outfile = DEFUALT_OUTFILE + "composit.arff";
		FeatureExtractor featureExtrator = (FeatureExtractor)Factory.createResource(FeatureExtractor.class.getName());
		
		FeatureValue attributeCalculator = null;

		attributeCalculator = new FeatureValue(ORIGINAL_MARKUPS, XMLSaver.CONVERSATION_TAG, PANConverter.CONVERSATION_TYPE, attributeCalculator);
		attributeCalculator.setWekaExporter(new BooleanVectorModel(new NumeratorRepresenter()));
		
		attributeCalculator = new FeatureValue(null, ANNIEConstants.TOKEN_ANNOTATION_TYPE, ANNIEConstants.TOKEN_STRING_FEATURE_NAME, attributeCalculator);
		attributeCalculator.setWekaExporter(new BooleanVectorModel(new VectorModel()));
		
		featureExtrator.setAttributeCalculator(attributeCalculator);
		featureExtrator.setInstanceExtractor(new DocumentInstance());
		featureExtrator.setExportFileName(outfile);
		
		app.add(featureExtrator);
		app.setCorpus(testCorpus);
		app.execute();
		
		DataSource source = new DataSource(outfile);
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

//		DataSource source = new DataSource(FeatureExtractor.DEFUALT_OUTFILE);
//		source.getDataSet();
		
	}
}
