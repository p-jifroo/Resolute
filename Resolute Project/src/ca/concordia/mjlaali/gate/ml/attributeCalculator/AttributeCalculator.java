package ca.concordia.mjlaali.gate.ml.attributeCalculator;

import gate.Annotation;
import gate.Document;
import weka.core.Instances;

public interface AttributeCalculator {
	public void calAttribute(Document doc, Annotation instance);
	public Instances getInstances();
}
