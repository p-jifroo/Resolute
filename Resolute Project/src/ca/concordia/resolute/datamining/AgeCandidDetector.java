package ca.concordia.resolute.datamining;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.creole.ANNIEConstants;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.SerialAnalyserController;
import gate.creole.metadata.CreoleResource;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;

@CreoleResource (name = "Age Candid Detector", comment = "This class extract the age of person from chat log")
public class AgeCandidDetector extends AbstractLanguageAnalyser{

	private static final long serialVersionUID = 1L;
	private final String TARGET_ANNOTATION_TYPE = ANNIEConstants.TOKEN_ANNOTATION_TYPE;
	private final String TARGET_FEATURE_NAME = ANNIEConstants.TOKEN_KIND_FEATURE_NAME;
	private final String TARGET_FEATURE_VALUE = "number";

	public static final String TOKEN_AGE_FEATURE_NAME = "Age";
	public static final String AGE_ANNOTATION_TYPE = "Age";

	private FeatureMap numberFeature;

	public AgeCandidDetector() {
		numberFeature = Factory.newFeatureMap();
		numberFeature.put(TARGET_FEATURE_NAME, TARGET_FEATURE_VALUE);
	}

	@Override
	public void execute() throws ExecutionException {
		AnnotationSet annotations = getDocument().getAnnotations();
		AnnotationSet oldAgeAnnotation = annotations.get(AGE_ANNOTATION_TYPE);
		for (Annotation ann: oldAgeAnnotation){
			annotations.remove(ann);
		}
		AnnotationSet annSetNumbers = annotations.get(TARGET_ANNOTATION_TYPE, numberFeature);

		for (Annotation ann: annSetNumbers){
			FeatureMap tokenFeatures = ann.getFeatures();

			int num;
			try {
				num = Integer.parseInt(tokenFeatures.get(TOKEN_STRING_FEATURE_NAME).toString());
				if (num > 9 && num < 99){ 
					FeatureMap newFeatureMap = Factory.newFeatureMap();

					Object isAge = tokenFeatures.get(TOKEN_AGE_FEATURE_NAME);
					if (isAge != null && isAge.toString().equals("true")){
						newFeatureMap.put("Class", "true");
					} else
						newFeatureMap.put("Class", "false");

					annotations.add(ann.getStartNode(), ann.getEndNode(), AGE_ANNOTATION_TYPE, newFeatureMap);
				}
			} catch (NumberFormatException e) {
			}

		}
	}

	public static void main(String[] args) throws IOException, GateException, ClassNotFoundException {
		Gate.init();
		Gate.getCreoleRegister().registerComponent(AgeCandidDetector.class);

		SerialAnalyserController controller = (SerialAnalyserController) 
				PersistenceManager.loadObjectFromFile(new File(new File( 
						Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR), 
						ANNIEConstants.DEFAULT_FILE));


		controller.add((ProcessingResource)Factory.createResource(AgeCandidDetector.class.getName()));

		Document doc = Factory.newDocument("it is test 325");
		Corpus corpus = Factory.newCorpus("Corpus");
		corpus.add(doc);

		controller.setCorpus(corpus);
		controller.execute();

	}
}
