package ca.concordia.mjlaali.gate;

import gate.AnnotationSet;
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
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import ca.concordia.mjlaali.gate.ml.DocumentInstance;
import ca.concordia.mjlaali.gate.ml.FeatureExtractorPR;
import ca.concordia.mjlaali.gate.ml.FeatureValue;
import ca.concordia.mjlaali.gate.ml.ecoder.BooleanVectorEncoder;
import ca.concordia.mjlaali.gate.ml.ecoder.LuceneToWeka;
import ca.concordia.mjlaali.gate.ml.ecoder.NumaratorEncoder;
import ca.concordia.mjlaali.gate.ml.ecoder.WekaEncoder;
import ca.concordia.resolute.core.chat.listener.XMLSaver;
import ca.concordia.resolute.datamining.PANConverter;
import ca.concorida.resolute.core.textmining.RuleBaseAgeDetectionTest;

public class GATEMLPlugin {
	private static final String ATTNAME_WORDS = "WORDS#";
	private static final String ATTNAME_CLASS = "{{CLASS}}";
	private static final String ARFF_FILE = "output/test.arff";
	private static final String APP_FEATURE_EXTRACTOR = "output/test.xapp";
	private static final String IDX_TRAIN = "output/idx_test";
	public static final String ORIGINAL_MARKUPS = "Original markups";

	public static final String PAN_DATASTORE_LOC = 
			"/Volumes/Data/Users/Majid/Documents/Course/Concordia/SOEN6951/data-set/PAN 2012/pan12-sexual-predator-identification-training-data-2012-05-01/sds";

	private static Corpus persistanceCorpus;
	private static Corpus trainCorpus, testCorpus;
	
	@BeforeClass
	public static void init() throws GateException, MalformedURLException{
		Gate.init();
		Gate.getCreoleRegister().registerComponent(FeatureExtractorPR.class);
		persistanceCorpus = RuleBaseAgeDetectionTest.readPresistanceCorpus(new File(PAN_DATASTORE_LOC));
		
		trainCorpus = copyFromCorpus(0, 1000, persistanceCorpus);
		testCorpus = copyFromCorpus(1000, 100, persistanceCorpus);
	}

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

	@Test
	public void train() throws Exception{
		//define attribute
		File path = new File(IDX_TRAIN);
		Directory dir = FSDirectory.open(path);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35, new WhitespaceAnalyzer(Version.LUCENE_35));
		iwc.setOpenMode(OpenMode.CREATE);
		IndexWriter indexer = new IndexWriter(dir, iwc);
		
		FeatureExtractorPR featureExtractor = createFeatureExtractor();
		
		featureExtractor.setIndexer(indexer);
		
		//calculate value
		SerialAnalyserController controller = (SerialAnalyserController) Factory.createResource(SerialAnalyserController.class.getName());
		controller.add(featureExtractor);
		File appFile = new File (APP_FEATURE_EXTRACTOR);
		PersistenceManager.saveObjectToFile(controller, appFile);
		
		Corpus corpus = trainCorpus;
		controller.setCorpus(corpus);
		controller.execute();
		
		//save to file
		indexer.close();
		Map<String, WekaEncoder> name2Encoder = new TreeMap<String, WekaEncoder>();
		name2Encoder.put(ATTNAME_CLASS, new NumaratorEncoder());
		name2Encoder.put(ATTNAME_WORDS, new BooleanVectorEncoder(2, 0.5));
		LuceneToWeka luceneToWeka = new LuceneToWeka(dir, name2Encoder);
		luceneToWeka.buildStructure(dir, "simple train");
		File arffFile = new File(ARFF_FILE);
		luceneToWeka.saveAsArff(arffFile);
		
		DataSource source = new DataSource(ARFF_FILE);
		source.getDataSet();
	}

	private FeatureExtractorPR createFeatureExtractor()
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

	@Test
	public void test() throws Exception{
		//learn classifier
		DataSource source = new DataSource(ARFF_FILE);
		Instances dataSet = source.getDataSet();
		dataSet.setClassIndex(dataSet.numAttributes() - 1);
		
		String[] options = weka.core.Utils.splitOptions("-C 0.25 -M 2");
		Classifier xModel = new J48();
		xModel.setOptions(options);
		xModel.buildClassifier(dataSet);
		
		//load feature extraction application
//		SerialAnalyserController appFeaExt = (SerialAnalyserController) 
//				PersistenceManager.loadObjectFromFile(new File(APP_FEATURE_EXTRACTOR));

		SerialAnalyserController app = (SerialAnalyserController) 
				PersistenceManager.loadObjectFromFile(new File(new File( 
						Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR), 
						ANNIEConstants.DEFAULT_FILE));

		FeatureExtractorPR featureExtractor = createFeatureExtractor();
//		@SuppressWarnings("unchecked")
//		Collection<ProcessingResource> pRs = appFeaExt.getPRs();
//		FeatureExtractorPR featureExtractor = null;
//		for (ProcessingResource pr: pRs){
//			if (pr instanceof FeatureExtractorPR) {
//				featureExtractor = (FeatureExtractorPR) pr;
//			}
//		}
		
		Corpus testCorpus = GATEMLPlugin.testCorpus;
//		app.add(appFeaExt);
		app.add(featureExtractor);
		app.setCorpus(testCorpus);
		Directory dir = new RAMDirectory();

		Directory dirTrain = FSDirectory.open(new File(IDX_TRAIN));
		Map<String, WekaEncoder> name2Encoder = new TreeMap<String, WekaEncoder>();
		name2Encoder.put(ATTNAME_WORDS, new BooleanVectorEncoder(2, 0.5));

		for (Document doc: testCorpus){
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35, new WhitespaceAnalyzer(Version.LUCENE_35));
			iwc.setOpenMode(OpenMode.CREATE);
			IndexWriter iw = new IndexWriter(dir, iwc);
			featureExtractor.setIndexer(iw);

			app.setDocument(doc);
			app.execute();
			
			iw.close();
			LuceneToWeka luceneToWeka = new LuceneToWeka(dir, name2Encoder);
			Instances data = luceneToWeka.buildStructure(dirTrain, "test");
			for (int i = 0; i < luceneToWeka.getNumDoc(); ++i)
				data.add(luceneToWeka.getInstance(i));
			data.setClassIndex(data.numAttributes() - 1);
			
			for (int i = 0; i < data.numInstances(); ++i) {
				Instance instance = data.instance(i);
				double cls = xModel.classifyInstance(instance);
				if (cls > 0){
					AnnotationSet annotationSet = doc.getAnnotations(ORIGINAL_MARKUPS).get(XMLSaver.CONVERSATION_TAG);
					String conversationType = annotationSet.iterator().next().getFeatures().get(PANConverter.CONVERSATION_TYPE).toString();
					System.out.println(conversationType);
					if (conversationType.equals("PREDATOR"))
						System.out.println(doc.getFeatures());
					double[] probs = xModel.distributionForInstance(instance);
					for (double prob: probs)
						System.out.println(prob);
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		GATEMLPlugin gatemlPlugin = new GATEMLPlugin();
		GATEMLPlugin.init();
		gatemlPlugin.test();
	}
}
