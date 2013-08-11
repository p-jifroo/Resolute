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
import java.util.LinkedList;
import java.util.List;


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
	private int prevContexWindowSize = 10;
	private int futureContexWindowSize = 5;

	@Override
	public void execute() throws ExecutionException {
		AnnotationSet docAnnSet = getDocument().getAnnotations();
		@SuppressWarnings("deprecation")
		AnnotationSet ageCandid = docAnnSet.get(AgeCandidDetector.AGE_ANNOTATION_TYPE);
		List<Annotation> tokens = Utils.inDocumentOrder(docAnnSet.get(ANNIEConstants.TOKEN_ANNOTATION_TYPE));
		
		FeatureMap docFeatuers = getDocument().getFeatures();
		LinkedList<String> prevContext = new LinkedList<>(), futureContex = new LinkedList<>();
		for (Annotation ann: ageCandid){
			prevContext.clear();
			
			int idxToken = -1;
			for (int i = 0; i < tokens.size(); ++i){
				if (tokens.get(i).getStartNode().getOffset() == ann.getStartNode().getOffset())
					idxToken = i;
			}

			int stWindows = idxToken - prevContexWindowSize;
			stWindows = stWindows < 0 ? 0 : stWindows;
			int enWindows = idxToken + futureContexWindowSize;
			enWindows = enWindows > tokens.size() ? tokens.size() : enWindows;
			
			for (int i = stWindows; i < idxToken; ++i){
				prevContext.add(tokens.get(i).getFeatures().get(ANNIEConstants.TOKEN_STRING_FEATURE_NAME).toString().toLowerCase());
			}
			for (int i = idxToken; i < enWindows; ++i){
				futureContex.add(tokens.get(i).getFeatures().get(ANNIEConstants.TOKEN_STRING_FEATURE_NAME).toString().toLowerCase());
			}
			//some rules for age detection
			if ((prevContext.contains("asl") ||  
					(futureContex.get(0).equals("m") || futureContex.get(0).equals("f"))) ||
					(prevContext.getLast().equals("i'm"))
					){
				Object oldAge = docFeatuers.get(AGE_DOC_FEATURE);
				String newAge = Utils.stringFor(getDocument(), ann);
				String age = oldAge == null ? newAge : oldAge + ", " + newAge;
				docFeatuers.put(AGE_DOC_FEATURE, age);
				ann.getFeatures().put("Class", "true");
			}
		}
		
		if (docFeatuers.get(AGE_DOC_FEATURE) == null){
			docFeatuers.put(AGE_DOC_FEATURE, "?");
		}
	}
	
	
	public static void main(String[] args) throws IOException, GateException {
		String testFile = "/Volumes/Data/Users/Majid/Documents/Course/Concordia/SOEN6951/data-set/PervertedJustice/xml/batch 2/ekoplaya20.xml";
		
		Gate.init();
		ResouluteApp app = new ResouluteApp();
		Document doc = Factory.newDocument(new File(testFile).toURI().toURL());
		
		Document annotateAge = app.annotateAge(doc);
		System.out.println(annotateAge.getFeatures().get(AGE_DOC_FEATURE));
	}
}
