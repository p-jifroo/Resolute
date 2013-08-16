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
	
	private LinkedList<String> prevContext = new LinkedList<>(), futureContex = new LinkedList<>();
	private String prevString, futureString;

	@Override
	public void execute() throws ExecutionException {
		AnnotationSet docAnnSet = getDocument().getAnnotations();
		AnnotationSet ageCandid = docAnnSet.get(AgeCandidDetector.AGE_ANNOTATION_TYPE);
		List<Annotation> tokens = Utils.inDocumentOrder(docAnnSet.get(ANNIEConstants.TOKEN_ANNOTATION_TYPE));
		
		FeatureMap docFeatuers = getDocument().getFeatures();
		for (Annotation ann: ageCandid){
			prevContext.clear();
			futureContex.clear();
			
			int idxToken = findPosition(tokens, ann);

			createContextWindows(tokens, idxToken);
			System.out.println("\nRuleBaseAgeDetection.execute()\t/*******");
			System.out.print("RuleBaseAgeDetection.createContextWindows(): " + tokens.get(idxToken));
			System.out.println("RuleBaseAgeDetection.createContextWindows(): " + prevContext + prevString);
			System.out.println("RuleBaseAgeDetection.createContextWindows(): " + futureContex + futureString);
			System.out.println("RuleBaseAgeDetection.execute()\t*******/");
			
			applyRules(docFeatuers, ann);
		}
		
		if (docFeatuers.get(AGE_DOC_FEATURE) == null){
			docFeatuers.put(AGE_DOC_FEATURE, "?");
		}
	}

	/**
	 * Find position of a token in the input annotations list.
	 * @param tokens list of all annotations
	 * @param ann the annotation to lookup
	 * @return the position of annotation
	 */
	private int findPosition(List<Annotation> tokens, Annotation ann) {
		int idxToken = -1;
		for (int i = 0; i < tokens.size(); ++i){
			if (tokens.get(i).getStartNode().getOffset() == ann.getStartNode().getOffset())
				idxToken = i;
		}
		return idxToken;
	}


	/**
	 * extract context words from document
	 * @param tokens
	 * @param prevContext
	 * @param futureContex
	 * @param idxToken
	 */
	private void createContextWindows(List<Annotation> tokens, int idxToken) {
		prevContext.clear();
		futureContex.clear();
		
		
		int stWindows = idxToken - prevContexWindowSize;
		stWindows = stWindows < 0 ? 0 : stWindows;
		int enWindows = idxToken + futureContexWindowSize;
		enWindows = enWindows > tokens.size() ? tokens.size() : enWindows;
		
		StringBuffer sb = new StringBuffer();
		for (int i = stWindows; i < idxToken; ++i){
			String strWord = tokens.get(i).getFeatures().get(ANNIEConstants.TOKEN_STRING_FEATURE_NAME).toString().toLowerCase();
			prevContext.add(strWord);
			sb.append(strWord);
			sb.append(" ");
		}
		prevString = sb.toString().trim();

		sb.setLength(0);
		for (int i = idxToken + 1; i < enWindows; ++i){
			String strWord = tokens.get(i).getFeatures().get(ANNIEConstants.TOKEN_STRING_FEATURE_NAME).toString().toLowerCase();
			futureContex.add(strWord);
			sb.append(strWord);
			sb.append(" ");
		}
		futureString = sb.toString().trim();
	}
	

	/**
	 * apply manual rules to the documents and store the ouput to the featuer of document
	 * @param docFeatuers
	 * @param prevContext
	 * @param futureContex
	 * @param ann
	 */
	private void applyRules(FeatureMap docFeatuers,	Annotation ann) {
		//some rules for age detection
		if (       prevContext.contains("asl")   
				|| prevString.contains("age ?")
				|| prevString.contains("a / s")  
				|| (futureContex.get(0).equals("m") || futureContex.get(0).equals("f"))
				|| (futureString.startsWith("/ m") || futureString.startsWith("/ f"))
				|| (prevString.endsWith("im") || prevString.endsWith("i am") || prevContext.getLast().equals("'m"))
				|| (prevString.contains("how old") && (prevString.contains("ru") || (prevString.contains("r u")))) 
				|| (prevContext.contains("okay") && prevContext.contains("bein"))
				){
			Object oldAge = docFeatuers.get(AGE_DOC_FEATURE);
			String newAge = Utils.stringFor(getDocument(), ann);
			String age = oldAge == null ? newAge : oldAge + ", " + newAge;
			docFeatuers.put(AGE_DOC_FEATURE, age);
			ann.getFeatures().put("Class", "true");
		}
	}
	
	/**
	 * A simple program that extract age from a chat conversation 
	 * @param args
	 * @throws IOException
	 * @throws GateException
	 */
	public static void main(String[] args) throws IOException, GateException {
		String testFile = "/Volumes/Data/Users/Majid/Documents/Course/Concordia/SOEN6951/data-set/PervertedJustice/xml/batch 2/ekoplaya20.xml";
		
		Gate.init();
		ResouluteApp app = new ResouluteApp();
		Document doc = Factory.newDocument(new File(testFile).toURI().toURL());
		
		app.annotateAge(doc);
		System.out.println(doc.getFeatures().get(AGE_DOC_FEATURE));
	}
}
