package ca.concordia.mjlaali.gate.ml.attributeCalculator;

import gate.Annotation;
import gate.Document;
import weka.core.Instance;
import weka.core.Instances;

public interface AttributeCalculator extends Iterable<Instance>{
	public void calAttribute(Document doc, Annotation instance);
	public Instances getInstances();
}
