package ca.concordia.resolute.core.textmining.gate;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.Utils;
import gate.creole.ANNIEConstants;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleResource;
import gate.util.GateException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * A rule base processing resource that extract the age of a person from his/her chat message.
 * @author mjlaali
 *
 */
@CreoleResource (name = "RuleBaseAgeDetection")
public class RuleBaseAgeDetection extends AbstractLanguageAnalyser{

	public static final String AGE_DOC_FEATURE = "Age";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void execute() throws ExecutionException {
		AnnotationSet docAnnSet = getDocument().getAnnotations();
		@SuppressWarnings("deprecation")
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
			if (contextWord.contains("m") || contextWord.contains("asl") 
					|| contextWord.contains("f") || contextWord.contains("im") ){
				Object oldAge = docFeatuers.get(AGE_DOC_FEATURE);
				String newAge = Utils.stringFor(getDocument(), ann);
				String age = oldAge == null ? newAge : oldAge + ", " + newAge;
				docFeatuers.put(AGE_DOC_FEATURE, age);
				ann.getFeatures().put("Class", "true");
			}
		}
		
		if (docFeatuers.get(AGE_DOC_FEATURE) == null){
			docFeatuers.put(AGE_DOC_FEATURE, "-1");
		}
	}
	
	
	public static void main(String[] args) throws IOException, GateException {
		String testFile = "/Volumes/Data/Users/Majid/Documents/Course/Concordia/SOEN6951/data-set/PervertedJustice/xml/batch 2/ekoplaya20.xml";
		
		Gate.init();
		RuleBaseAgeDetectorApp app = new RuleBaseAgeDetectorApp();
		Document doc = Factory.newDocument(new File(testFile).toURI().toURL());
		
		Document annotateAge = app.annotateAge(doc);
		System.out.println(annotateAge.getFeatures().get(AGE_DOC_FEATURE));
	}
}
