package ca.concordia.mjlaali.gate.ml.ecoder;

import weka.core.FastVector;

/**
 * Converter that convert a Lucene directory to the Weka convention. 
 * @author mjlaali
 *
 */
public interface WekaEncoder {

	/**
	 * @param numDoc set the number of the document in Lucene directory
	 */
	public void setNumDoc(int numDoc);
	/**
	 * Add all the word for specific attribute. This will be used for filtering words
	 * @param text a word that is represented in the attribute
	 * @param docFreq Document frequency of the word
	 */
	public void addWord(String text, int docFreq);
	/**
	 * Get the structure of an attribute represented by this encoder 
	 * @param attName the name of attribute
	 * @return the structure of attribute 
	 */
	public FastVector getAttribute(String attName);
	/**
	 * Convert words of the an instance to its Weka value
	 * @param terms the words that are represented by an instance 
	 * @return the value with respect to words in the attribute
	 */
	public double[] getValues(String[] terms);
}
