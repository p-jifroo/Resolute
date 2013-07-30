package ca.concordia.mjlaali.gate;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.creole.ANNIEConstants;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.net.MalformedURLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import weka.core.converters.ConverterUtils.DataSource;
import ca.concordia.mjlaali.gate.ml.DocumentInstance;
import ca.concordia.mjlaali.gate.ml.FeatureExtractor;
import ca.concordia.mjlaali.gate.ml.attributeCalculator.FeatureValue;
import ca.concordia.mjlaali.gate.ml.wekaExporter.DefaultAttExporter;
import ca.concordia.mjlaali.gate.ml.wekaExporter.MemoryWordIndexer;
import ca.concordia.mjlaali.gate.ml.wekaExporter.NumeratorRepresenter;
import ca.concordia.mjlaali.gate.ml.wekaExporter.TFIDFModel;
import ca.concordia.mjlaali.gate.ml.wekaExporter.VectorNumbersRepresenter;
import ca.concordia.mjlaali.tool.ConsolProgressBar;
import ca.concordia.resolute.core.chat.listener.XMLSaver;
import ca.concordia.resolute.datamining.PANConverter;
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
		initTestCorpus(0, 100);
	}

	private static Corpus testCorpus;

	public static void initTestCorpus(int start, int cnt) throws ResourceInstantiationException{
		testCorpus = Factory.newCorpus("test corpus");
		for (int i = start; i < start + cnt && i < persistanceCorpus.size(); ++i){
			Document doc = persistanceCorpus.get(i);
			Document copyDoc = (Document)Factory.duplicate(doc);
			copyDoc.getAnnotations().addAll(doc.getAnnotations());
			testCorpus.add(copyDoc);
		}
	}

	@AfterClass
	public static void releaseCorpus() throws ResourceInstantiationException{
		for (Document doc: testCorpus){
			testCorpus.unloadDocument(doc);
			Factory.deleteResource(doc);
		}
		Factory.deleteResource(testCorpus);
	}

	//	@Ignore
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
		attributeCalculator.setWekaExporter(new DefaultAttExporter(new VectorNumbersRepresenter(), new MemoryWordIndexer()));
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
		attributeCalculator.setWekaExporter(new DefaultAttExporter(new NumeratorRepresenter(), new MemoryWordIndexer()));

		attributeCalculator = new FeatureValue(null, ANNIEConstants.TOKEN_ANNOTATION_TYPE, ANNIEConstants.TOKEN_STRING_FEATURE_NAME, attributeCalculator);
		attributeCalculator.setWekaExporter(new DefaultAttExporter(new VectorNumbersRepresenter(), new TFIDFModel("output/tfidf", true, 10, 0.3)));

		featureExtrator.setAttributeCalculator(attributeCalculator);
		featureExtrator.setInstanceExtractor(new DocumentInstance());
		featureExtrator.setExportFileName(outfile);

		app.add(featureExtrator);
		app.setCorpus(testCorpus);
		app.execute();

		DataSource source = new DataSource(outfile);
		source.getDataSet();
	}

	@Test
	public void learnClassifier() throws Exception{
		FeatureExtractor featureExtrator = null;
		String outfile = DEFUALT_OUTFILE + "composit-complete.arff";
		featureExtrator = (FeatureExtractor)Factory.createResource(FeatureExtractor.class.getName());

		FeatureValue attributeCalculator = null;

		attributeCalculator = new FeatureValue(ORIGINAL_MARKUPS, XMLSaver.CONVERSATION_TAG, PANConverter.CONVERSATION_TYPE, attributeCalculator);
		attributeCalculator.setWekaExporter(new DefaultAttExporter(new NumeratorRepresenter(), new MemoryWordIndexer()));

		attributeCalculator = new FeatureValue(null, ANNIEConstants.TOKEN_ANNOTATION_TYPE, ANNIEConstants.TOKEN_STRING_FEATURE_NAME, attributeCalculator);
		attributeCalculator.setWekaExporter(new DefaultAttExporter(new VectorNumbersRepresenter(), new TFIDFModel("output/tfidf", true, 10, 0.3)));

		featureExtrator.setAttributeCalculator(attributeCalculator);
		featureExtrator.setInstanceExtractor(new DocumentInstance());
		featureExtrator.setExportFileName(outfile);
		featureExtrator.setManual(true);

		System.out.println("GATEMLPlugin.learnClassifier(): Corpus has been created");
		SerialAnalyserController app = (SerialAnalyserController)Factory.createResource(SerialAnalyserController.class.getName(), 
				Factory.newFeatureMap(), Factory.newFeatureMap(), "Simple app");

		app.add(featureExtrator);

		int chunkSize = 1000;
		ConsolProgressBar progressBar = new ConsolProgressBar(persistanceCorpus.size(), 100);
		for (int i = 0; i < persistanceCorpus.size() / 4; i += chunkSize){
			releaseCorpus();
			initTestCorpus(i, chunkSize);

			app.setCorpus(testCorpus);
			app.execute();

			progressBar.progress(chunkSize);
		}
		
		System.out.println("GATEMLPlugin.compositFeature(): starting exporting to arff");
		featureExtrator.exportFeature();

		DataSource source = new DataSource(outfile);
		source.getDataSet();

	}
}
