package ca.concordia.mjlaali.gate.ml.attributeCalculator;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;

import java.util.LinkedList;
import java.util.List;

import weka.core.Instances;
import ca.concordia.mjlaali.gate.ml.wekaExporter.WekaAttributeExporter;

public class FeatureValue extends CompositionAttribute{
	private String annotationSetName, typeName, featureName;
	private WekaAttributeExporter wekaExporter;
	
	public FeatureValue(String annotationSetName, String typeName, String featureName, AttributeCalculator attributeCalculator){
		super(attributeCalculator);
		this.annotationSetName = annotationSetName;
		this.typeName = typeName;
		this.featureName = featureName;
	}
	
	
	public FeatureValue(String annotationSetName, String typeName, String featureName){
		this(annotationSetName, typeName, featureName, null);
	}
	
	public void setWekaExporter(WekaAttributeExporter wekaExporter) {
		this.wekaExporter = wekaExporter;
	}

	@Override
	public void calMyAttribute(Document doc, Annotation instance) {
		AnnotationSet annotations;
		if (annotationSetName == null)
			annotations = doc.getAnnotations();
		else
			annotations = doc.getAnnotations(annotationSetName);
		
		AnnotationSet tokens = annotations.get(typeName, 
				instance.getStartNode().getOffset(), instance.getEndNode().getOffset());

		List<String> words = new LinkedList<>();
		for (Annotation ann: tokens){
			String strWord = ann.getFeatures().get(featureName).toString();
			words.add(strWord);
		}
	
		wekaExporter.addInstance(words);
	}

	@Override
	public Instances getMyInstances() {
		return wekaExporter.getInstances("BagOfWord");
	}

}
