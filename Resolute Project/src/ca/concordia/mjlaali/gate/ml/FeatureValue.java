package ca.concordia.mjlaali.gate.ml;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.ProcessingResource;

import java.util.Set;
import java.util.TreeSet;

/**
 * Extract a feature from a document. This class use a value of a {@link FeatureMap} that is assigned to a {@link Annotation}
 * in the document as a feature. Therefore, any output of {@link ProcessingResource} which store in {@link AnnotationSet}
 * in the document can be used as a feature. 
 * @author mjlaali
 *
 */
public class FeatureValue implements AttributeCalculator{
	private static final long serialVersionUID = 1L;
	
	private String annotationSetName, typeName, featureName;
	private String name;
	
	/**
	 * Initialized the class
	 * @param name the name of the feature
	 * @param annotationSetName The name of the {@link AnnotationSet}
	 * @param typeName the name of {@link Annotation}
	 * @param featureName the name of feature
	 */
	public FeatureValue(String name, String annotationSetName, String typeName, String featureName){
		this.annotationSetName = annotationSetName;
		this.typeName = typeName;
		this.featureName = featureName;
		this.name = name;
	}
	
	@Override
	public Set<String> getAttributeValue(Document doc, Annotation instance) {
		AnnotationSet annotations;
		if (annotationSetName == null)
			annotations = doc.getAnnotations();
		else
			annotations = doc.getAnnotations(annotationSetName);
		
		AnnotationSet tokens = annotations.get(typeName, 
				instance.getStartNode().getOffset(), instance.getEndNode().getOffset());

		Set<String> words = new TreeSet<>();
		for (Annotation ann: tokens){
			Object objFeature = ann.getFeatures().get(featureName);
			String strWord;
			if (objFeature != null){
				strWord = objFeature.toString();
				words.add(strWord);
			}
		}
		return words;
	}

	@Override
	public String getName() {
		return name;
	}
}
