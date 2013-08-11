package ca.concordia.mjlaali.gate.ml;

import java.io.Serializable;

import gate.AnnotationSet;
import gate.Document;

/**
 * Create instances from a document. Instance has same role as in Weka platform. 
 * @author mjlaali
 *
 */
public interface InstanceExtractor extends Serializable {
	/**
	 * Get all instances from a document. Each instance is represent as a GATE annotation
	 * @param doc the document 
	 * @return Instances of the document
	 */
	public AnnotationSet getInstances(Document doc); 
}
