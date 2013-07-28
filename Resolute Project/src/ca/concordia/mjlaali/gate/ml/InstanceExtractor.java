package ca.concordia.mjlaali.gate.ml;

import gate.AnnotationSet;
import gate.Document;

public interface InstanceExtractor {
	public AnnotationSet getInstances(Document doc); 
}
