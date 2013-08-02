package ca.concordia.mjlaali.gate.ml;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import weka.core.Attribute;
import weka.core.FastVector;

public class BooleanVectorEncoder implements WekaEncoder, Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private long dfMin, dfMax;
	private double dfMaxPercentage;
	private Map<String, Integer> word2ID = new TreeMap<>();
	private FastVector booleanValue;

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
