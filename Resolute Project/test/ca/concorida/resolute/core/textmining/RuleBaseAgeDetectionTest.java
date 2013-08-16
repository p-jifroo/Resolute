package ca.concorida.resolute.core.textmining;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.DataStore;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.Resource;
import gate.Utils;
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

	@Test
	public void precision() throws GateException, IOException{
		AnnotationEvaluation evaluator = new AnnotationEvaluation();
		ResouluteApp app = new ResouluteApp();
		FeatureMap featureMap = Factory.newFeatureMap();
		featureMap.put("Class", "true");
		int totalCorrect = 0, totalGold = 0, totalOutput = 0;
		int idx = 0;
		int numTest = 10;
		for (Document doc: persistCorp){
			++idx;
			persistCorp.unloadDocument(doc, false);
			Document docWithAge = (Document) Factory.duplicate(doc);
			
			System.out.println(doc.getFeatures().get("gate.SourceURL"));
			
			app.annotateAge(docWithAge);
			evaluator.evaluate(doc.getAnnotations().get(AgeCandidDetector.AGE_ANNOTATION_TYPE, featureMap), 
					docWithAge.getAnnotations().get(AgeCandidDetector.AGE_ANNOTATION_TYPE, featureMap));

			System.out.println("===> Real one");
			nicePrint(doc, doc.getAnnotations().get(AgeCandidDetector.AGE_ANNOTATION_TYPE, featureMap));
			System.out.println("===> Detected one");
			nicePrint(docWithAge, docWithAge.getAnnotations().get(AgeCandidDetector.AGE_ANNOTATION_TYPE, featureMap));
			
			System.out.println(idx + "-" + doc.getName() + ":\t" + evaluator.getPrecision() + ", " + evaluator.getRecall());
			totalCorrect += evaluator.getCntIntersection();
			totalGold += evaluator.getCntGold();
			totalOutput += evaluator.getCntOut();
			Factory.deleteResource(docWithAge);
			Factory.deleteResource(doc);
			if (idx == numTest)
				break;
		}
		
		double precision = (double)totalCorrect / totalOutput;
		double recall = (double)totalCorrect / totalGold;
		System.out.println("Total : Precision = [" + precision + "]\tRecall = [" + recall + "]");
		
		Assert.assertTrue(precision > 0.9);
		Assert.assertTrue(recall > 0.9);
	}
	
	public void nicePrint(Document doc, AnnotationSet toPrint){
		List<Annotation> inDocumentOrder = Utils.inDocumentOrder(toPrint);
		for (Annotation ann: inDocumentOrder){
			System.out.print(Utils.stringFor(doc, ann));
			System.out.println("\t" + ann.getStartNode().getOffset());
		}
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