package ca.concordia.resolute.core.textmining.gate;

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

/**
 * A processing resource plugin for the GATE that annotated all the numbers between 9 to 99 in a document as a candidate of age
 *  
 * @author mjlaali
 *
 */ 
@CreoleResource (name = "Age Candid Detector", comment = "This class extract the age of person from chat log")
public class AgeCandidDetector extends AbstractLanguageAnalyser{

	//default serial number
	private static final long serialVersionUID = 1L;
	// the annotation that contain number
	private static final String TARGET_ANNOTATION_TYPE = ANNIEConstants.TOKEN_ANNOTATION_TYPE;
	// the feature name that indicate the if the token is string
	private static final String TARGET_FEATURE_NAME = ANNIEConstants.TOKEN_KIND_FEATURE_NAME;
	// the value of feature that indicate the token is number
	private static final String TARGET_FEATURE_VALUE = "number";

	/// a boolean feature in Token annotation for detection age.
	@Deprecated
	public static final String TOKEN_AGE_FEATURE_NAME = "Age";
	public static final String AGE_ANNOTATION_TYPE = "Age";

	// a general feature that is used for all number that is 
	// detected in the document 
	private FeatureMap numberFeature;

	/**
	 * initialize the class
	 */
	public AgeCandidDetector() {
		numberFeature = Factory.newFeatureMap();
		numberFeature.put(TARGET_FEATURE_NAME, TARGET_FEATURE_VALUE);
	}

	/**
	 * Extract numbers between 9 and 99 and add feature to its Token annotation.
	 * The feature name is {@value #TOKEN_AGE_FEATURE_NAME} and the value for numbers between 
	 * 9 to 99 is true, other wise is false.
	 */
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

	/**
	 * A simple test for checking {@link AgeCandidDetector} class. 
	 * @param args no argument is needed
	 * @throws IOException
	 * @throws GateException
	 * @throws ClassNotFoundException
	 */
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
