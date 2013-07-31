package ca.concordia.resolute.datamining;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.creole.ANNIEConstants;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.Random;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import ca.concordia.mjlaali.gate.GATEMLPlugin;
import ca.concordia.mjlaali.gate.ml.DocumentInstance;
import ca.concordia.mjlaali.gate.ml.FeatureExtractor;
import ca.concordia.mjlaali.gate.ml.attributeCalculator.FeatureValue;
import ca.concordia.mjlaali.gate.ml.wekaExporter.DefaultWekaExporter;
import ca.concordia.mjlaali.gate.ml.wekaExporter.MemoryWordIndexer;
import ca.concordia.mjlaali.gate.ml.wekaExporter.NumeratorRepresenter;
import ca.concordia.mjlaali.gate.ml.wekaExporter.TFIDFModel;
import ca.concordia.mjlaali.gate.ml.wekaExporter.VectorNumbersRepresenter;
import ca.concordia.mjlaali.tool.ConsolProgressBar;
import ca.concordia.mjlaali.tool.XMLParser;
import ca.concordia.resolute.core.chat.listener.XMLSaver;
import ca.concorida.resolute.core.textmining.RuleBaseAgeDetectionTest;

public class PANDataSet {
	
	private static final String WORD_IDX = "output/idxTrain";
	private static final String PAN_DATASET_FLD = "/Volumes/Data/Users/Majid/Documents/Course/Concordia/SOEN6951/data-set/PAN 2012/";
	private static final String PAN_TRAIN_FLD = PAN_DATASET_FLD + "pan12-sexual-predator-identification-training-data-2012-05-01/";
	private static final String CLASSIFIER_FILE = "output/classifier.obj";
//	private static final String PAN_TEST_FLD = PAN_DATASET_FLD + "pan12-sexual-predator-identification-test-data-2012-05-21/";
	public static String COMPLETE_ATTR = GATEMLPlugin.DEFUALT_OUTFILE + "composit-complete.arff";
	private static Corpus persistanceCorpus;

	@BeforeClass
	public static void init() throws GateException, MalformedURLException{
		Gate.init();
		Gate.getCreoleRegister().registerComponent(FeatureExtractor.class);
		persistanceCorpus = RuleBaseAgeDetectionTest.readPresistanceCorpus(new File(GATEMLPlugin.PAN_DATASTORE_LOC));
	}


	/**
	 * test the pan converter on sample xml file
	 * @throws IOException
	 */
	@Ignore
	@Test
	public void checkOnSampleXML() throws IOException{
		String panDataSetFolder = PAN_TRAIN_FLD;
		
		String predatorsIDFile = panDataSetFolder + "sexual-predator-identification-pan12-train-predators-2012-05-01.txt";
//		String chatConversationFile = "sexual-predator-identification-pan12-train-2012-05-01.xml";
		String chatConversationFile = "sample.xml";
		XMLParser parser = XMLParser.getInstance();
		PANConverter panConverter = new PANConverter(panDataSetFolder + "xml/", predatorsIDFile);
		parser.parse(new File(panDataSetFolder + chatConversationFile), panConverter);
		Assert.assertEquals(132, panConverter.getTotalChatConversation());
		Assert.assertEquals(3, panConverter.getNumPredatorChatConversation());
	}
	
	/**
	 * check the pan converter on the original file
	 * @throws IOException
	 */
	@Ignore
	@Test
	public void createTrainData() throws IOException{
		
		String predatorsIDFile = PAN_TRAIN_FLD + "sexual-predator-identification-pan12-train-predators-2012-05-01.txt";
		String chatConversationFile = "sexual-predator-identification-pan12-train-2012-05-01.xml";
		XMLParser parser = XMLParser.getInstance();
		PANConverter panConverter = new PANConverter(PAN_TRAIN_FLD + "xml/", predatorsIDFile);
		parser.parse(new File(PAN_TRAIN_FLD + chatConversationFile), panConverter);
		System.out.println("" + panConverter.getTotalChatConversation() + " conversation is converted.");
		System.out.println("The conversations include " + panConverter.getNumPredatorChatConversation() + " conversations belong to a predator.");
	}
	
	public void createTestData() throws IOException{
		
	}
	
	@Ignore
	@Test
	public void generateCompleteArff() throws Exception{
		FeatureExtractor featureExtrator = null;
		featureExtrator = (FeatureExtractor)Factory.createResource(FeatureExtractor.class.getName());

		FeatureValue attributeCalculator = null;

		attributeCalculator = new FeatureValue(GATEMLPlugin.ORIGINAL_MARKUPS, XMLSaver.CONVERSATION_TAG, PANConverter.CONVERSATION_TYPE, attributeCalculator);
		attributeCalculator.setWekaExporter(new DefaultWekaExporter(new NumeratorRepresenter("@@Class@@"), new MemoryWordIndexer()));

		attributeCalculator = new FeatureValue(null, ANNIEConstants.TOKEN_ANNOTATION_TYPE, ANNIEConstants.TOKEN_STRING_FEATURE_NAME, attributeCalculator);
		attributeCalculator.setWekaExporter(new DefaultWekaExporter(new VectorNumbersRepresenter(), new TFIDFModel(WORD_IDX, 0, 10, 0.3)));

		featureExtrator.setAttributeCalculator(attributeCalculator);
		featureExtrator.setInstanceExtractor(new DocumentInstance());
		featureExtrator.setExportFileName(COMPLETE_ATTR);
		featureExtrator.setManual(true);

		System.out.println("GATEMLPlugin.learnClassifier(): Corpus has been created");
		SerialAnalyserController app = (SerialAnalyserController)Factory.createResource(SerialAnalyserController.class.getName(), 
				Factory.newFeatureMap(), Factory.newFeatureMap(), "Simple app");

		app.add(featureExtrator);

		int chunkSize = 1000;
		
		int totalDocument = persistanceCorpus.size() / 5;
		ConsolProgressBar progressBar = new ConsolProgressBar(totalDocument, 100);
		for (int i = 0; i < totalDocument; i += chunkSize){
			Corpus testCorpus = GATEMLPlugin.copyFromCorpus(i, chunkSize, persistanceCorpus);

			app.setCorpus(testCorpus);
			app.execute();

			progressBar.progress(chunkSize);
			GATEMLPlugin.releaseCorpus(testCorpus);
		}
		
		System.out.println("GATEMLPlugin.compositFeature(): starting exporting to arff");
		featureExtrator.exportFeature();

		DataSource source = new DataSource(COMPLETE_ATTR);
		source.getDataSet();
	}

	@Ignore
	@Test
	public void evaluteClassifier() throws Exception{
		DataSource source = new DataSource(COMPLETE_ATTR);
		Instances trainData = source.getDataSet();
		trainData.setClassIndex(trainData.numAttributes() - 1);

//		Classifier cModel = new SMO();
//		cModel.buildClassifier(trainData);
		
		Evaluation eTest = new Evaluation(trainData);
		String[] options = weka.core.Utils.splitOptions("-C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\"");
		eTest.crossValidateModel(SMO.class.getName(), trainData, 10, options, new Random());
		System.out.println(eTest.toClassDetailsString());
		System.out.println(eTest.toString());
	}

	@Test
	public void learnClassifier() throws Exception{
		//learn classifier
		DataSource source = new DataSource(COMPLETE_ATTR);
		Instances trainData = source.getDataSet();
		trainData.setClassIndex(trainData.numAttributes() - 1);

		Classifier cModel = new SMO();
		String[] options = weka.core.Utils.splitOptions("-C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\"");
		cModel.setOptions(options);
		cModel.buildClassifier(trainData);
		
		System.out.println("PANDataSet.learnClassifier()");
		ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(CLASSIFIER_FILE));
		output.writeObject(cModel);
		output.close();
	}
	
	@Test
	public void testClassifier() throws Exception{
		//load classifier
		ObjectInputStream input = new ObjectInputStream(new FileInputStream(CLASSIFIER_FILE));
		Classifier cModel = (Classifier)input.readObject();
		input.close();

		//init feature extractor
		FeatureExtractor featureExtrator = null;
		featureExtrator = (FeatureExtractor)Factory.createResource(FeatureExtractor.class.getName());
		FeatureValue attributeCalculator = null;
		attributeCalculator = new FeatureValue(null, ANNIEConstants.TOKEN_ANNOTATION_TYPE, ANNIEConstants.TOKEN_STRING_FEATURE_NAME, attributeCalculator);
		attributeCalculator.setWekaExporter(new DefaultWekaExporter(new VectorNumbersRepresenter(), new TFIDFModel(WORD_IDX, 1, 10, 0.3)));
		featureExtrator.setAttributeCalculator(attributeCalculator);
		featureExtrator.setInstanceExtractor(new DocumentInstance());
		featureExtrator.setManual(true);

		//create application
		SerialAnalyserController app = (SerialAnalyserController)Factory.createResource(SerialAnalyserController.class.getName(), 
				Factory.newFeatureMap(), Factory.newFeatureMap(), "Simple app");
		app.add(featureExtrator);
		
		int start = persistanceCorpus.size() * 4 / 5 ;
		int chunkSize = 100;
		Corpus testCorpus = GATEMLPlugin.copyFromCorpus(start, chunkSize, persistanceCorpus);
		
		app.setCorpus(testCorpus);
		for (Document doc: testCorpus){
			app.setDocument(doc);
			app.execute();
			
			Instance instance = featureExtrator.getInstance();
			double classifyInstance = cModel.classifyInstance(instance);
			System.out.println(doc.getAnnotations(GATEMLPlugin.ORIGINAL_MARKUPS).get(XMLSaver.CONVERSATION_TAG).get(PANConverter.CONVERSATION_TYPE));
			System.out.println(classifyInstance);
		}

	}
	
}
