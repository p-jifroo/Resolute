package ca.concordia.resolute.core.textmining.gate;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.ProcessingResource;
import gate.creole.ANNIEConstants;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.persist.PersistenceException;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;


/**
 * A GATE application that handles text analysises for a chat message.
 * @author mjlaali
 *
 */
public class ResouluteApp {
	private Corpus corpus;
	private SerialAnalyserController completeController;
	private SerialAnalyserController ageController;


	public ResouluteApp() throws GateException, IOException {
		Gate.getCreoleRegister().registerComponent(AgeCandidDetector.class);
		Gate.getCreoleRegister().registerComponent(RuleBaseAgeDetection.class);
		Gate.getCreoleRegister().registerComponent(PredatorDetector.class);

		corpus = Factory.newCorpus("Corpus");

	}


	private SerialAnalyserController createController(boolean includePredator) throws PersistenceException, IOException,
			ResourceInstantiationException {
		SerialAnalyserController controller = (SerialAnalyserController) 
				PersistenceManager.loadObjectFromFile(new File(new File( 
						Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR), 
						ANNIEConstants.DEFAULT_FILE));

		controller.add((ProcessingResource)Factory.createResource(AgeCandidDetector.class.getName()));
		controller.add((ProcessingResource)Factory.createResource(RuleBaseAgeDetection.class.getName()));
		if (includePredator)
			controller.add((ProcessingResource)Factory.createResource(PredatorDetector.class.getName()));
		return controller;
	}
	
	public Document annotateAge(Document aDoc) throws ResourceInstantiationException, ExecutionException, PersistenceException, IOException{
		if (ageController == null)
			ageController = createController(false);

		corpus.add(aDoc);
		ageController.setCorpus(corpus);
		ageController.execute();
		corpus.clear();

		return aDoc;
	}
	
	/**
	 * @return GATE pipeline that contains all the necessary tools for analyzing a chat conversation
	 * @throws IOException 
	 * @throws ResourceInstantiationException 
	 * @throws PersistenceException 
	 */
	public SerialAnalyserController getController() throws PersistenceException, ResourceInstantiationException, IOException {
		if (completeController == null)
			completeController = createController(true);
		return completeController;
	}
}
