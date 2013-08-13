package ca.concordia.mjlaali.gate;

import gate.Annotation;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.creole.ANNIEConstants;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.ExtensionFileFilter;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import weka.core.Instances;
import ca.concordia.mjlaali.gate.ml.DocumentInstance;
import ca.concordia.mjlaali.gate.ml.FeatureExtractorPR;
import ca.concordia.mjlaali.gate.ml.FeatureValue;
import ca.concordia.mjlaali.gate.ml.ecoder.BooleanVectorEncoder;
import ca.concordia.mjlaali.gate.ml.ecoder.LuceneToWeka;
import ca.concordia.mjlaali.gate.ml.ecoder.NumaratorEncoder;
import ca.concordia.mjlaali.gate.ml.ecoder.WekaEncoder;
import ca.concordia.resolute.core.chat.listener.XMLSaver;
import ca.concordia.resolute.datamining.PANConverter;

public class GATEMLPlugin {
	private static final String ATTNAME_WORDS = "WORDS#";
	private static final String ATTNAME_CLASS = "{{CLASS}}";
	public static final String ORIGINAL_MARKUPS = "Original markups";

	public static final String PAN_DATASTORE_LOC = 
			"/Volumes/Data/Users/Majid/Documents/Course/Concordia/SOEN6951/data-set/PAN 2012/pan12-sexual-predator-identification-training-data-2012-05-01/sds";
	public static final String DATA_TEST_XML_FLD = "data/test/";
	public static final String DATA_SAMPLE_XML_FLD = "data/sampleXML/";

	private static Corpus trainCorpus, testCorpus;
	private static Instances trainData, testData;
	
	@BeforeClass
	public static void init() throws GateException, IOException{
		//initialization
		Gate.init();
		Gate.getCreoleRegister().registerComponent(FeatureExtractorPR.class);
		trainCorpus = Factory.newCorpus("Sample");
		testCorpus = Factory.newCorpus("test");
		ExtensionFileFilter filter = new ExtensionFileFilter("XML files", "xml"); 
		trainCorpus.populate(new File(DATA_SAMPLE_XML_FLD).toURI().toURL(), filter, "UTF-8", false);
		testCorpus.populate(new File(DATA_TEST_XML_FLD).toURI().toURL(), filter, "UTF-8", false);
		
		//create text analyzer
		SerialAnalyserController app = (SerialAnalyserController) 
				PersistenceManager.loadObjectFromFile(new File(new File( 
						Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR), 
						ANNIEConstants.DEFAULT_FILE));

		FeatureExtractorPR featureExtractor = createFeatureExtractor();
		app.add(featureExtractor);
		
		//start extracting feature
		app.setCorpus(trainCorpus);
		
		//
		Directory dirTrain = new RAMDirectory();
		Map<String, WekaEncoder> name2Encoder = new TreeMap<String, WekaEncoder>();
		name2Encoder.put(ATTNAME_WORDS, new BooleanVectorEncoder(1, 1));
		name2Encoder.put(ATTNAME_CLASS, new NumaratorEncoder());

		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35, new WhitespaceAnalyzer(Version.LUCENE_35));
		iwc.setOpenMode(OpenMode.CREATE);
		IndexWriter iw = new IndexWriter(dirTrain, iwc);
		featureExtractor.setIndexer(iw);
		
		app.execute();

		iw.close();
		LuceneToWeka luceneToWeka = new LuceneToWeka(dirTrain, name2Encoder);
		trainData = luceneToWeka.buildStructure(dirTrain, "train");
		for (int i = 0; i < luceneToWeka.getNumDoc(); ++i)
			trainData.add(luceneToWeka.getInstance(i));
		trainData.setClassIndex(trainData.numAttributes() - 1);

		luceneToWeka.saveAsArff(new File("output/samplexml.arff"));
		
		//testing
		Directory dirTest = new RAMDirectory();
		
		for (Document doc: testCorpus){
			iwc = new IndexWriterConfig(Version.LUCENE_35, new WhitespaceAnalyzer(Version.LUCENE_35));
			iwc.setOpenMode(OpenMode.CREATE);
			iw = new IndexWriter(dirTest, iwc);
			featureExtractor.setIndexer(iw);
			
			app.setDocument(doc);
			app.execute();

			iw.close();
			LuceneToWeka luceneToWekaTest = new LuceneToWeka(dirTest, name2Encoder);
			Instances structure = luceneToWekaTest.buildStructure(dirTrain, "test");
			if (testCorpus == null) 
				testData = structure;
			
			for (int i = 0; i < luceneToWekaTest.getNumDoc(); ++i)
				testData.add(luceneToWekaTest.getInstance(i));
			
			if (testData.classIndex() == -1)
				trainData.setClassIndex(trainData.numAttributes() - 1);

		}


	}

//	@AfterClass
//	public static void releaseCorpus() throws ResourceInstantiationException{
//		MyGateTools.releaseCorpus(trainCorpus);
//		
//	}
	
	@Test
	public void checkCorpusPopulation(){
		File[] listFiles = new File(DATA_SAMPLE_XML_FLD).listFiles();
		Assert.assertEquals(listFiles.length, trainCorpus.size());
	}
	
	@Test
	public void checkDoumentInstanceGenerator(){
		Assert.assertEquals(trainCorpus.size(), trainData.numInstances());
	}
	
	@Test
	public void checkExtractedFeatureNumarator(){
		int numPredator = 0;
		for (int i = 0; i < trainData.numInstances(); ++i){
			numPredator += trainData.instance(i).classValue();
		}
		Assert.assertEquals(1, numPredator);
	}
	
	
	@Test
	public void checkExtratedFeatureBooleanVector(){
		for (int idx = 0; idx < trainCorpus.size(); ++idx){
			Set<String> words = new TreeSet<>();
			for (Annotation ann: trainCorpus.get(idx).getAnnotations().get(ANNIEConstants.TOKEN_ANNOTATION_TYPE))
				words.add(ann.getFeatures().get(ANNIEConstants.TOKEN_STRING_FEATURE_NAME).toString());
			int numTokens = words.size();
			int numNonZeroAttribute = 0;
			int classValue = (int) trainData.instance(idx).classValue(); 

			for (int i = 0; i < trainData.numAttributes(); ++i){
				numNonZeroAttribute += trainData.instance(idx).value(i);
			}

			Assert.assertEquals(numTokens + classValue, numNonZeroAttribute);
		}
	}
	
	
	@Test
	public void checkTestCorpusSize(){
		File[] listFiles = new File(DATA_TEST_XML_FLD).listFiles();
		Assert.assertEquals(listFiles.length, testCorpus.size());
		Assert.assertEquals(listFiles.length, testData.numInstances());
	}

	private static FeatureExtractorPR createFeatureExtractor()
			throws ResourceInstantiationException {
		FeatureExtractorPR featureExtractor = (FeatureExtractorPR) Factory.createResource(FeatureExtractorPR.class.getName());
		featureExtractor.setInstanceExtractor(new DocumentInstance());
		featureExtractor.addAttributeCalculator(
				new FeatureValue(ATTNAME_WORDS, null, ANNIEConstants.TOKEN_ANNOTATION_TYPE, ANNIEConstants.TOKEN_STRING_FEATURE_NAME));
		featureExtractor.addAttributeCalculator(
				new FeatureValue(ATTNAME_CLASS, GATEMLPlugin.ORIGINAL_MARKUPS, 
						XMLSaver.CONVERSATION_TAG, PANConverter.CONVERSATION_TYPE));
		return featureExtractor;
	}


}
