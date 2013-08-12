package ca.concordia.resolute.core.textmining.gate;

import gate.Resource;
import gate.creole.ANNIEConstants;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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

import weka.classifiers.Classifier;
import weka.core.Instances;
import ca.concordia.mjlaali.gate.GATEMLPlugin;
import ca.concordia.mjlaali.gate.ml.DocumentInstance;
import ca.concordia.mjlaali.gate.ml.FeatureExtractorPR;
import ca.concordia.mjlaali.gate.ml.FeatureValue;
import ca.concordia.mjlaali.gate.ml.ecoder.BooleanVectorEncoder;
import ca.concordia.mjlaali.gate.ml.ecoder.LuceneToWeka;
import ca.concordia.mjlaali.gate.ml.ecoder.NumaratorEncoder;
import ca.concordia.mjlaali.gate.ml.ecoder.WekaEncoder;
import ca.concordia.resolute.core.chat.listener.XMLSaver;
import ca.concordia.resolute.datamining.PANConverter;
import ca.concordia.resolute.datamining.PANDataSet;

/**
 * Detect a predator base on a classifier. The classifier should be trained and store in a file.  
 * @author mjlaali
 *
 */
public class PredatorDetector extends FeatureExtractorPR{

	private static final long serialVersionUID = 1L;
	private static final String ATTNAME_WORDS = "WORDS#";
	private static final String ATTNAME_CLASS = "{{CLASS}}";
	
	static private String basePath = "";
	
	public static final String IDX_TRAIN = basePath + PANDataSet.IDX_TRAIN;
	public static final String CLASSIFIER_FILE = "meta-data/classifier-logistic.obj";
	public static final String PREDATOR_PROB_DOC_FEATURE = "Predator";

	private Classifier xModel;
	private Directory dirTrain, dir;
	private Map<String, WekaEncoder> name2Encoder;
	
	
	public static void setBasePath(String basePath){
		PredatorDetector.basePath = basePath;
	}
	/**
	 * Initialize class and load the classifier from default path
	 */
	@Override
	public Resource init() throws ResourceInstantiationException {
		//crate instance generator
		setInstanceExtractor(new DocumentInstance());
		//define features
		addAttributeCalculator(
				new FeatureValue(ATTNAME_WORDS, null, ANNIEConstants.TOKEN_ANNOTATION_TYPE, ANNIEConstants.TOKEN_STRING_FEATURE_NAME));
		addAttributeCalculator(
				new FeatureValue(ATTNAME_CLASS, GATEMLPlugin.ORIGINAL_MARKUPS, 
						XMLSaver.CONVERSATION_TAG, PANConverter.CONVERSATION_TYPE));
		
		//load classifier
		ObjectInputStream oin;
		try {
			oin = new ObjectInputStream(new FileInputStream(basePath + CLASSIFIER_FILE));
			xModel = (Classifier) oin.readObject();
			oin.close();

			dirTrain = FSDirectory.open(new File(IDX_TRAIN));
		} catch (ClassNotFoundException | IOException e) {
			throw new ResourceInstantiationException(e);
		}
		//initialize lucene directory
		dir = new RAMDirectory();

		//define encoders for attributes
		name2Encoder = new TreeMap<String, WekaEncoder>();
		name2Encoder.put(ATTNAME_CLASS, new NumaratorEncoder());
		name2Encoder.put(ATTNAME_WORDS, new BooleanVectorEncoder(2, 0.5));
		return super.init();
	}
	
	@Override
	public void execute() throws ExecutionException {
		//create a directory
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35, new WhitespaceAnalyzer(Version.LUCENE_35));
		iwc.setOpenMode(OpenMode.CREATE);
		IndexWriter iw;
		try {
			iw = new IndexWriter(dir, iwc);
			setIndexer(iw);
			//extract and  save features.
			super.execute();
			iw.close();
			
			//convert features to instance
			LuceneToWeka luceneToWeka = new LuceneToWeka(dir, name2Encoder);
			Instances data = luceneToWeka.buildStructure(dirTrain, "test");

			data.add(luceneToWeka.getInstance(0));
			data.setClassIndex(data.numAttributes() - 1);
			
			//classify the extracted instance.
			double[] probs = xModel.distributionForInstance(data.instance(0));
			//store result to GATE document
			getDocument().getFeatures().put(PREDATOR_PROB_DOC_FEATURE, "" + probs[1]);
		} catch (Exception e) {
			throw new ExecutionException(e);
		}

	}
	
}
