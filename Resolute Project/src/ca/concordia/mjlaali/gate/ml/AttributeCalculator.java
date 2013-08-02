package ca.concordia.mjlaali.gate.ml;

import gate.Annotation;
import gate.Document;

import java.io.Serializable;
import java.util.Set;

public interface AttributeCalculator extends Serializable{
	public Set<String> getAttributeValue(Document doc, Annotation instance);
	public String getName();
}
