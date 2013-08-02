package ca.concordia.mjlaali.gate.ml;

import java.io.Serializable;

import gate.AnnotationSet;
import gate.Document;

public interface InstanceExtractor extends Serializable {
	public AnnotationSet getInstances(Document doc); 
}
