package ca.concordia.mjlaali.gate;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.creole.ANNIEConstants;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.persist.PersistenceException;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.Assert;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import ca.concordia.mjlaali.gate.ml.DocumentInstance;
import ca.concordia.mjlaali.gate.ml.FeatureExtractor;
import ca.concordia.mjlaali.gate.ml.attributeCalculator.FeatureValue;
import ca.concordia.mjlaali.gate.ml.wekaExporter.DefaultWekaExporter;
import ca.concordia.mjlaali.gate.ml.wekaExporter.MemoryWordIndexer;
import ca.concordia.mjlaali.gate.ml.wekaExporter.NumeratorRepresenter;
import ca.concordia.mjlaali.gate.ml.wekaExporter.TFIDFModel;
import ca.concordia.mjlaali.gate.ml.wekaExporter.VectorNumbersRepresenter;
import ca.concordia.resolute.core.chat.listener.XMLSaver;
import ca.concordia.resolute.datamining.PANConverter;
import ca.concordia.resolute.datamining.PANDataSet;
import ca.concorida.resolute.core.textmining.RuleBaseAgeDetectionTest;

public class GATEMLPlugin {
	private static Corpus persistanceCorpus;
	public static final String DEFUALT_OUTFILE = "output/";
	public static final String ORIGINAL_MARKUPS = "Original markups";

	public static final String PAN_DATASTORE_LOC = 
			"/Volumes/Data/Users/Majid/Documents/Course/Concordia/SOEN6951/data-set/PAN 2012/pan12-sexual-predator-identification-training-data-2012-05-01/sds";

	@BeforeClass
	public static void init() throws GateException, MalformedURLException{
		Gate.init();
		Gate.getCreoleRegister().registerComponent(FeatureExtractor.class);
		persistanceCorpus = RuleBaseAgeDetectionTest.readPresistanceCorpus(new File(PAN_DATASTORE_LOC));
		testCorpus = copyFromCorpus(0, 100, persistanceCorpus);
	}

	private static Corpus testCorpus;

	public static Corpus copyFromCorpus(int start, int cnt, Corpus corpus) throws ResourceInstantiationException{
		Corpus testCorpus = Factory.newCorpus("test corpus");
		for (int i = start; i < start + cnt && i < corpus.size(); ++i){
			Document doc = corpus.get(i);
			corpus.unloadDocument(doc);
			Document copyDoc = (Document)Factory.duplicate(doc);
			copyDoc.getAnnotations().addAll(doc.getAnnotations());
			testCorpus.add(copyDoc);
		}
		return testCorpus;
	}

	@AfterClass
	public static void releaseCorpus() throws ResourceInstantiationException{
		releaseCorpus(testCorpus);
		
	}
	
	public static void releaseCorpus(Corpus corpus) throws ResourceInstantiationException{
		for (Document doc: corpus){
//			corpus.unloadDocument(doc);
			Factory.deleteResource(doc);
		}
		corpus.clear();
		Factory.deleteResource(corpus);
	}

	//	@Ignore
	@Test
	public void extractFeatures() throws Exception{
		Document doc = Factory.newDocument("it is a test.");
		Document doc2 = Factory.newDocument("it is another test.");

		String outfile = DEFUALT_OUTFILE + "simple.arff";
		FeatureExtractor featureExtrator = (FeatureExtractor)Factory.createResource(FeatureExtractor.class.getName());
		FeatureValue attributeCalculator = new FeatureValue(null, ANNIEConstants.TOKEN_ANNOTATION_TYPE, ANNIEConstants.TOKEN_STRING_FEATURE_NAME);
		attributeCalculator.setWekaExporter(new DefaultWekaExporter(new VectorNumbersRepresenter(), new MemoryWordIndexer()));
		featureExtrator.setAttributeCalculator(attributeCalculator);
		featureExtrator.setInstanceExtractor(new DocumentInstance());
		featureExtrator.setExportFileName(outfile);

		SerialAnalyserController app = createFeatureExtractor(featureExtrator);

		Corpus corpus = Factory.newCorpus("corpus");
		corpus.add(doc);
		corpus.add(doc2);

		app.setCorpus(corpus);
		app.execute();

		DataSource source = new DataSource(outfile);
		Assert.notNull(source.getDataSet());
	}

	private SerialAnalyserController createFeatureExtractor(FeatureExtractor featureExtrator)
			throws PersistenceException, IOException,
			ResourceInstantiationException {
		SerialAnalyserController app = (SerialAnalyserController) 
				PersistenceManager.loadObjectFromFile(new File(new File( 
						Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR), 
						ANNIEConstants.DEFAULT_FILE));
		app.add(featureExtrator);
		return app;
	}

	@Test
	public void compositFeature() throws Exception{

		String outfile = DEFUALT_OUTFILE + "composit.arff";
		FeatureExtractor featureExtrator = (FeatureExtractor)Factory.createResource(FeatureExtractor.class.getName());
		FeatureValue attributeCalculator = null;
		attributeCalculator = new FeatureValue(ORIGINAL_MARKUPS, XMLSaver.CONVERSATION_TAG, PANConverter.CONVERSATION_TYPE, attributeCalculator);
		attributeCalculator.setWekaExporter(new DefaultWekaExporter(new NumeratorRepresenter("@@Class@@"), new MemoryWordIndexer()));
		attributeCalculator = new FeatureValue(null, ANNIEConstants.TOKEN_ANNOTATION_TYPE, ANNIEConstants.TOKEN_STRING_FEATURE_NAME, attributeCalculator);
		attributeCalculator.setWekaExporter(new DefaultWekaExporter(new VectorNumbersRepresenter(), new TFIDFModel("output/tfidf", 0, 10, 0.3)));

		featureExtrator.setAttributeCalculator(attributeCalculator);
		featureExtrator.setInstanceExtractor(new DocumentInstance());
		featureExtrator.setExportFileName(outfile);

		SerialAnalyserController app = createFeatureExtractor(featureExtrator);
		app.setCorpus(testCorpus);
		app.execute();

		DataSource source = new DataSource(outfile);
		Assert.notNull(source.getDataSet());
		
	}
	
	@Ignore
	@Test
	public void testFeature() throws Exception{
		//init the app
		FeatureExtractor featureExtrator = (FeatureExtractor)Factory.createResource(FeatureExtractor.class.getName());
		FeatureValue attributeCalculator = null;
		attributeCalculator = new FeatureValue(ORIGINAL_MARKUPS, XMLSaver.CONVERSATION_TAG, PANConverter.CONVERSATION_TYPE, attributeCalculator);
		attributeCalculator.setWekaExporter(new DefaultWekaExporter(new NumeratorRepresenter("@@Class@@"), new MemoryWordIndexer()));
		attributeCalculator = new FeatureValue(null, ANNIEConstants.TOKEN_ANNOTATION_TYPE, ANNIEConstants.TOKEN_STRING_FEATURE_NAME, attributeCalculator);
		attributeCalculator.setWekaExporter(new DefaultWekaExporter(new VectorNumbersRepresenter(), new TFIDFModel("output/tfidf", 0, 10, 0.3)));
		featureExtrator.setAttributeCalculator(attributeCalculator);
		featureExtrator.setInstanceExtractor(new DocumentInstance());

//		SerialAnalyserController app = createFeatureExtractor(featureExtrator);

		//init classifier
		DataSource source = new DataSource(PANDataSet.COMPLETE_ATTR);
		Instances trainData = source.getDataSet();
		trainData.setClassIndex(trainData.numAttributes() - 1);
		
//		Classifier cModel = new SMO();
//		cModel.setOptions(options);

		
		//init test corpus
		
		//check the corpus output
	}

}
