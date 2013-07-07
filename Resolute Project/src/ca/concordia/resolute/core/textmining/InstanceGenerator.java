package ca.concordia.resolute.core.textmining;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.Utils;
import gate.creole.ANNIEConstants;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.SerialAnalyserController;
import gate.creole.metadata.CreoleResource;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ca.concordia.resolute.datamining.AgeCandidDetector;

@CreoleResource (name = "InstanceGenerator")
public class InstanceGenerator extends AbstractLanguageAnalyser{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void execute() throws ExecutionException {
		AnnotationSet docAnnSet = getDocument().getAnnotations();
		AnnotationSet ageCandid = docAnnSet.get(AgeCandidDetector.AGE_ANNOTATION_TYPE);
		List<Annotation> tokens = Utils.inDocumentOrder(docAnnSet.get(ANNIEConstants.TOKEN_ANNOTATION_TYPE));
		
		FeatureMap docFeatuers = getDocument().getFeatures();
		Set<String> contextWord = new TreeSet<>();
		for (Annotation ann: ageCandid){
			contextWord.clear();
			
			int idxToken = -1;
			for (int i = 0; i < tokens.size(); ++i){
				if (tokens.get(i).getStartNode().getOffset() == ann.getStartNode().getOffset())
					idxToken = i;
			}

			int stWindows = idxToken - 5;
			stWindows = stWindows < 0 ? 0 : stWindows;
			int enWindows = idxToken + 5;
			enWindows = enWindows > tokens.size() ? tokens.size() : enWindows;
			
			for (int i = stWindows; i < enWindows; ++i){
				contextWord.add(tokens.get(i).getFeatures().get(ANNIEConstants.TOKEN_STRING_FEATURE_NAME).toString().toLowerCase());
			}
			if (contextWord.contains("m") || contextWord.contains("asl")){
				Object oldAge = docFeatuers.get("Age");
				String newAge = Utils.stringFor(getDocument(), ann);
				String age = oldAge == null ? newAge : oldAge + ", " + newAge;
				docFeatuers.put("Age", age);
			}
		}
		
		if (docFeatuers.get("Age") == null){
			docFeatuers.put("Age", "-1");
		}
	}
	
	public static void main(String[] args) throws IOException, GateException {
		String testFile = "/Volumes/Data/Users/Majid/Documents/Course/Concordia/SOEN6951/data-set/PervertedJustice/xml/batch 2/ekoplaya20.xml";
		Gate.init();
		Gate.getCreoleRegister().registerComponent(AgeCandidDetector.class);
		Gate.getCreoleRegister().registerComponent(InstanceGenerator.class);

		SerialAnalyserController controller = (SerialAnalyserController) 
				PersistenceManager.loadObjectFromFile(new File(new File( 
						Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR), 
						ANNIEConstants.DEFAULT_FILE));


		controller.add((ProcessingResource)Factory.createResource(AgeCandidDetector.class.getName()));
		controller.add((ProcessingResource)Factory.createResource(InstanceGenerator.class.getName()));

		Document doc = Factory.newDocument(new File(testFile).toURI().toURL());
		Corpus corpus = Factory.newCorpus("Corpus");
		corpus.add(doc);

		controller.setCorpus(corpus);
		controller.execute();

		System.out.println(doc.getFeatures().get("Age"));
	}
}
