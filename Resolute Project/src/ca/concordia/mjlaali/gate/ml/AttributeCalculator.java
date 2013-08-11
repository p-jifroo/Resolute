package ca.concordia.mjlaali.gate.ml;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;

import java.io.Serializable;
import java.util.Set;

/**
 * This class responsible to calculate a feature from a {@link Document} with respect to its {@link AnnotationSet}
 * Each attribute is represented by {@link Set} of String. In this regard a n-gram can be supported.
 * @author mjlaali
 *
 */
public interface AttributeCalculator extends Serializable{
	/**
	 * Extract features of an instance from a {@link Document}
	 * @param doc the whole document that contains the instance
	 * @param instance an {@link Annotation} that all features are calculated for it
	 * @return a set of string represent a feature.
	 */
	public Set<String> getAttributeValue(Document doc, Annotation instance);
	
	/**
	 * @return name of the feature
	 */
	public String getName();
}
