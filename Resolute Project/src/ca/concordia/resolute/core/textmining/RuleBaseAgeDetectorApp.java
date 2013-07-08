package ca.concordia.resolute.core.textmining;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.ProcessingResource;
import gate.creole.ANNIEConstants;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;

import ca.concordia.resolute.datamining.AgeCandidDetector;

public class RuleBaseAgeDetectorApp {

	private SerialAnalyserController controller;
	private Corpus corpus;
	
	public RuleBaseAgeDetectorApp() throws GateException, IOException {
		Gate.getCreoleRegister().registerComponent(AgeCandidDetector.class);
		Gate.getCreoleRegister().registerComponent(RuleBaseAgeDetection.class);
		controller = (SerialAnalyserController) 
				PersistenceManager.loadObjectFromFile(new File(new File( 
						Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR), 
						ANNIEConstants.DEFAULT_FILE));


		controller.add((ProcessingResource)Factory.createResource(AgeCandidDetector.class.getName()));
		controller.add((ProcessingResource)Factory.createResource(RuleBaseAgeDetection.class.getName()));

		corpus = Factory.newCorpus("Corpus");
	}
	
	public Document annotateAge(Document aDoc) throws ResourceInstantiationException, ExecutionException{
		Document annotatedDoc = (Document) Factory.duplicate(aDoc);

		corpus.add(annotatedDoc);
		controller.setCorpus(corpus);
		controller.execute();
		corpus.clear();

		return annotatedDoc;
	}
}
