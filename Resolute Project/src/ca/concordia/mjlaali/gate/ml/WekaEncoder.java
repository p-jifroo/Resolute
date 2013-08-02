package ca.concordia.mjlaali.gate.ml;

import weka.core.FastVector;

public interface WekaEncoder {

	public void setNumDoc(int numDoc);
	public void addWord(String text, int docFreq);
	public FastVector getAttribute(String attName);
	public double[] getValues(String[] terms);
}
