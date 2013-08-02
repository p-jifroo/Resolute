package ca.concordia.mjlaali.gate.ml;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;

import java.util.Set;
import java.util.TreeSet;

public class FeatureValue implements AttributeCalculator{
	private static final long serialVersionUID = 1L;
	
	private String annotationSetName, typeName, featureName;
	private String name;
	
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
