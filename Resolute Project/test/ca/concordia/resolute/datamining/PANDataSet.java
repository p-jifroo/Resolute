package ca.concordia.resolute.datamining;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.corpora.SerialCorpusImpl;
import gate.creole.ANNIEConstants;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.persist.PersistenceException;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import ca.concordia.mjlaali.gate.GATEMLPlugin;
import ca.concordia.mjlaali.gate.ml.DocumentInstance;
import ca.concordia.mjlaali.gate.ml.FeatureExtractorPR;
import ca.concordia.mjlaali.gate.ml.FeatureValue;
import ca.concordia.mjlaali.gate.ml.ecoder.BooleanVectorEncoder;
import ca.concordia.mjlaali.gate.ml.ecoder.LuceneToWeka;
import ca.concordia.mjlaali.gate.ml.ecoder.NumaratorEncoder;
import ca.concordia.mjlaali.gate.ml.ecoder.WekaEncoder;
import ca.concordia.mjlaali.tool.ConsolProgressBar;
import ca.concordia.mjlaali.tool.MyGateTools;
import ca.concordia.mjlaali.tool.XMLParser;
import ca.concordia.resolute.core.chat.listener.XMLSaver;
import ca.concordia.resolute.core.textmining.gate.PredatorDetector;
import ca.concorida.resolute.core.textmining.RuleBaseAgeDetectionTest;

public class PANDataSet {
	
	public static final String IDX_TRAIN = "meta-data/idxPANTrain";
	private static final String ATTNAME_WORDS = "WORDS#";
	private static final String ATTNAME_CLASS = "{{CLASS}}";
	private static final String ARFF_FILE = "output/pan_trainComplete.arff";

	private static final String PAN_DATASET_FLD = "/Volumes/Data/Users/Majid/Documents/Course/Concordia/SOEN6951/data-set/PAN 2012/";
	private static final String PAN_TRAIN_FLD = PAN_DATASET_FLD + "pan12-sexual-predator-identification-training-data-2012-05-01/";
	private Corpus persistanceCorpus;

	public void initGate() throws GateException, MalformedURLException{
		Gate.init();
		Gate.getCreoleRegister().registerComponent(FeatureExtractorPR.class);
		Gate.getCreoleRegister().registerComponent(PredatorDetector.class);
		persistanceCorpus = RuleBaseAgeDetectionTest.readPresistanceCorpus(new File(GATEMLPlugin.PAN_DATASTORE_LOC));
		
	}
	
	/**
	 * check the pan converter on the original file
	 * @throws IOException
	 */
	public void createTrainXMLFile() throws IOException{
		
		String predatorsIDFile = PAN_TRAIN_FLD + "sexual-predator-identification-pan12-train-predators-2012-05-01.txt";
		String chatConversationFile = "sexual-predator-identification-pan12-train-2012-05-01.xml";
		XMLParser parser = XMLParser.getInstance();
		PANConverter panConverter = new PANConverter(PAN_TRAIN_FLD + "xml/", predatorsIDFile);
		parser.parse(new File(PAN_TRAIN_FLD + chatConversationFile), panConverter);
		System.out.println("" + panConverter.getTotalChatConversation() + " conversation is converted.");
		System.out.println("The conversations include " + panConverter.getNumPredatorChatConversation() + " conversations belong to a predator.");
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

	public void createTrainArff(int idxChunk, OpenMode openMode) throws Exception{
		//define attribute
		File path = new File(IDX_TRAIN);
		Directory dir = FSDirectory.open(path);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35, new WhitespaceAnalyzer(Version.LUCENE_35));
		iwc.setOpenMode(openMode);
		IndexWriter indexer = new IndexWriter(dir, iwc);
		
		FeatureExtractorPR featureExtractor = createFeatureExtractor();
		
		featureExtractor.setIndexer(indexer);
		
		//calculate value
		SerialAnalyserController controller = (SerialAnalyserController) Factory.createResource(SerialAnalyserController.class.getName());
		controller.add(featureExtractor);
		
		int chunkSize = 100;
		int start = persistanceCorpus.size() * idxChunk / 5;
		int end = persistanceCorpus.size() * (idxChunk + 1) / 5;
		if (start < 0)
			start = 0;
		ConsolProgressBar progressBar = new ConsolProgressBar(end - start, 100);
		for (int i = start; i < end; i += chunkSize){
			Corpus testCorpus = MyGateTools.copyFromCorpus(i, chunkSize, (SerialCorpusImpl)persistanceCorpus);

			controller.setCorpus(testCorpus);
			controller.execute();

			progressBar.progress(chunkSize);
			MyGateTools.releaseCorpus(testCorpus);
		}
		
		//save to file
		indexer.close();
		
	}

	public void extractToArff(Directory dir) throws CorruptIndexException,
			IOException {
		Map<String, WekaEncoder> name2Encoder = new TreeMap<String, WekaEncoder>();
		name2Encoder.put(ATTNAME_CLASS, new NumaratorEncoder());
		name2Encoder.put(ATTNAME_WORDS, new BooleanVectorEncoder(2, 0.5));
		LuceneToWeka luceneToWeka = new LuceneToWeka(dir, name2Encoder);
		luceneToWeka.buildStructure(dir, "simple train");
		File arffFile = new File(ARFF_FILE);
		luceneToWeka.saveAsArff(arffFile);
	}
	
	public void learnClassifier() throws Exception{
		DataSource source = new DataSource("output/pan_train1_5-68-54.arff");
		Instances dataSet = source.getDataSet();
		dataSet.setClassIndex(dataSet.numAttributes() - 1);
		
		String[] options = weka.core.Utils.splitOptions("-C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\"");
		Classifier xModel = new SMO();
		xModel.setOptions(options);
		xModel.buildClassifier(dataSet);

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(PredatorDetector.CLASSIFIER_FILE));
		out.writeObject(xModel);
		out.close();
	}
	
	public void learnLogisticClassifier() throws Exception{
		DataSource source = new DataSource("output/pan_train1_5-68-54.arff");
		Instances dataSet = source.getDataSet();
		dataSet.setClassIndex(dataSet.numAttributes() - 1);
		
		String[] options = weka.core.Utils.splitOptions("weka.classifiers.meta.AttributeSelectedClassifier -E " +
				"\"weka.attributeSelection.ChiSquaredAttributeEval\"" +
				" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 1000\" -W weka.classifiers.trees.J48 " +
				"-- -C 0.25 -M 2");
		Classifier xModel = new AttributeSelectedClassifier();
		xModel.setOptions(options);
		xModel.buildClassifier(dataSet);

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(PredatorDetector.CLASSIFIER_FILE));
		out.writeObject(xModel);
		out.close();
		
	}
	
	public void testClassifier() throws PersistenceException, ResourceInstantiationException, IOException, ExecutionException{
		SerialAnalyserController app = (SerialAnalyserController) 
				PersistenceManager.loadObjectFromFile(new File(new File( 
						Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR), 
						ANNIEConstants.DEFAULT_FILE));
		
		PredatorDetector pr = (PredatorDetector) Factory.createResource(PredatorDetector.class.getName());
		app.add(pr);
		
		int start = persistanceCorpus.size() * 4 / 5;
		int chunkSize = 1000;
		Corpus testCorpus = MyGateTools.copyFromCorpus(start, chunkSize, (SerialCorpusImpl)persistanceCorpus);

		app.setCorpus(testCorpus);
		app.execute();
		
		for (Document doc: testCorpus){
			
			String convType = doc.getAnnotations(GATEMLPlugin.ORIGINAL_MARKUPS).get(XMLSaver.CONVERSATION_TAG).
					iterator().next().getFeatures().get(PANConverter.CONVERSATION_TYPE).toString();
			if (convType.equals("Predator")){
				System.out.print(convType);
				System.out.print(" : ");
				String prob = doc.getFeatures().get(PredatorDetector.PREDATOR_PROB_DOC_FEATURE).toString();
				System.out.println(prob);
				if (prob.equals("1.0")){
					System.out.println(doc.getFeatures());
					System.out.println(doc.getContent().size());
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("PANDataSet.main()");
		PANDataSet panDataSet = new PANDataSet();
		panDataSet.initGate();
//		panDataSet.createTrainArff(0, OpenMode.CREATE);
//		panDataSet.createTrainArff(1, OpenMode.CREATE_OR_APPEND);
//		panDataSet.createTrainArff(2, OpenMode.CREATE_OR_APPEND);
//		panDataSet.createTrainArff(3, OpenMode.CREATE_OR_APPEND);
//		panDataSet.createTrainArff(4, OpenMode.CREATE_OR_APPEND);
		panDataSet.extractToArff(FSDirectory.open(new File(IDX_TRAIN)));
//		panDataSet.learnLogisticClassifier();
//		panDataSet.testClassifier();
	}
	
}
