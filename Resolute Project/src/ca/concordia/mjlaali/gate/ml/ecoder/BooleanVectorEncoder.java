package ca.concordia.mjlaali.gate.ml.ecoder;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import ca.concordia.mjlaali.gate.ml.AttributeCalculator;

import weka.core.Attribute;
import weka.core.FastVector;

/**
 * Encode the input feature value as a boolean vector. Therefore, the output of {@link AttributeCalculator}, which is a Set 
 * String, is store in the vector. In this regard each String has a position in the vector and if it is represented
 * that position is set as One, otherwise is set as Zero.
 * This class able to filter some values in Set of String base on the Document Frequency (DF).  
 * @author mjlaali
 *
 */
public class BooleanVectorEncoder implements WekaEncoder, Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private long dfMin, dfMax;
	private double dfMaxPercentage;
	private Map<String, Integer> word2ID = new TreeMap<>();
	private FastVector booleanValue;

	/**
	 * Initialize the class
	 * @param dfMin the minimum DF for a value. It is absolute value 
	 * @param dfMaxPercentage the maximum DF for a value. It is percentage and its absolute value is calculated base of number of document
	 */
	public BooleanVectorEncoder(int dfMin, double dfMaxPercentage) {
		this.dfMin = dfMin;
		this.dfMaxPercentage = dfMaxPercentage;
		booleanValue = new FastVector();
		booleanValue.addElement("0");
		booleanValue.addElement("1");

	}
	
	@Override
	public void setNumDoc(int numDoc) {
		dfMax = Math.round(dfMaxPercentage * numDoc);
	}

	@Override
	public void addWord(String text, int docFreq) {
		if (dfMin <= docFreq && docFreq <= dfMax){
			if (!word2ID.containsKey(text))
				word2ID.put(text, word2ID.size());
		}
	}

	@Override
	public FastVector getAttribute(String prefixName) {
		
		FastVector atts = new FastVector();
		for (Entry<String, Integer> wordID: word2ID.entrySet()){
			atts.addElement(new Attribute(prefixName + wordID.getKey(), booleanValue));
		}

		return atts;
	}

	@Override
	public double[] getValues(String[] terms) {
		int one = booleanValue.indexOf("1");
		
		double[] values = new double[word2ID.size()];
		for (String term: terms){
			Integer id = word2ID.get(term);
			if (id != null){
				values[id] = one;
			}
		}
		return values;
	}

}
