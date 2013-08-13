package ca.concorida.resolute.core.textmining;

import gate.Corpus;
import gate.DataStore;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.Resource;
import gate.corpora.SerialCorpusImpl;
import gate.creole.ANNIEConstants;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.persist.PersistenceException;
import gate.persist.SerialDataStore;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.concordia.resolute.core.evaluation.AnnotationEvaluation;
import ca.concordia.resolute.core.textmining.gate.AgeCandidDetector;
import ca.concordia.resolute.core.textmining.gate.ResouluteApp;

public class RuleBaseAgeDetectionTest {
	private static final String CORPUS_WITH_ANNOTATION = "data/index";
	private static SerialCorpusImpl persistCorp = null;
	
	public static Corpus readPresistanceCorpus(File path) throws PersistenceException, MalformedURLException, ResourceInstantiationException{
		//reopen it
		SerialDataStore sds = new SerialDataStore(path.toURI().toURL().toString());
		sds.open();
		@SuppressWarnings("unchecked")
		List<String> corpusID = sds.getLrIds(SerialCorpusImpl.class.getName());

		FeatureMap corpFeatures = Factory.newFeatureMap();
		corpFeatures.put(DataStore.LR_ID_FEATURE_NAME, corpusID.get(0));
		corpFeatures.put(DataStore.DATASTORE_FEATURE_NAME, sds);
		
		//tell the factory to load the Serial Corpus with the specified ID from the specified  datastore
		return (Corpus)Factory.createResource(SerialCorpusImpl.class.getName(), corpFeatures);
	}
	
	@BeforeClass
	public static void init() throws GateException, MalformedURLException{
		//		Gate.init();
		System.out.println("RuleBaseAgeDetectionTest.init()");
		Gate.init();
		final File DS_DIR = new File(CORPUS_WITH_ANNOTATION);
		persistCorp = (SerialCorpusImpl) readPresistanceCorpus(DS_DIR);

	}

	@SuppressWarnings("deprecation")
	@Test
	public void precision() throws GateException, IOException{
		AnnotationEvaluation evaluator = new AnnotationEvaluation();
		ResouluteApp app = new ResouluteApp();
		FeatureMap featureMap = Factory.newFeatureMap();
		featureMap.put("Class", "true");
		int totalCorrect = 0, totalGold = 0, totalOutput = 0;
		int idx = 0;
		for (Document doc: persistCorp){
			++idx;
			Document docWithAge = app.annotateAge(doc);
			persistCorp.unloadDocument(doc, false);
			evaluator.evaluate(doc.getAnnotations().get(AgeCandidDetector.AGE_ANNOTATION_TYPE, featureMap), 
					docWithAge.getAnnotations().get(AgeCandidDetector.AGE_ANNOTATION_TYPE, featureMap));
			System.out.println(idx + "-" + doc.getName() + ":\t" + evaluator.getPrecision() + ", " + evaluator.getRecall());
			totalCorrect += evaluator.getCntIntersection();
			totalGold += evaluator.getCntGold();
			totalOutput += evaluator.getCntOut();
			Factory.deleteResource(docWithAge);
			
		}
		
		System.out.println("Total : Precision = [" + (double)totalCorrect / totalOutput + "]\tRecall = [" + (double)totalCorrect / totalGold + "]");
		
	}
	
	@Test
	public void duplicate() throws GateException{
		Document aDoc = Factory.newDocument("it is a test");
		Resource copyDoc = Factory.duplicate(aDoc);
		aDoc.setName("Original");
		copyDoc.setName("Copy");
		Assert.assertFalse(copyDoc.getName().equals(aDoc.getName()));
	}
	
	@Test
	public void evaluator() throws ResourceInstantiationException, PersistenceException, IOException, ExecutionException{
		AnnotationEvaluation tokenEvaluator = new AnnotationEvaluation();
		Document doc = Factory.newDocument("it is a test.");
		
		SerialAnalyserController controller = (SerialAnalyserController) 
				PersistenceManager.loadObjectFromFile(new File(new File( 
						Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR), 
						ANNIEConstants.DEFAULT_FILE));
		
		Corpus corpus = Factory.newCorpus("test");
		corpus.add(doc);
		
		controller.setCorpus(corpus);
		controller.execute();
		
		tokenEvaluator.evaluate(doc.getAnnotations().get("Token"), doc.getAnnotations().get("Token"));

		System.out.println(tokenEvaluator.getPrecision() + ", " + tokenEvaluator.getRecall());
	}

}