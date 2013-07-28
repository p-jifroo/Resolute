package ca.concordia.mjlaali.gate.ml.wekaExporter;

import java.util.Map;
import java.util.Set;

import weka.core.FastVector;
import weka.core.Instance;

public interface NumberRepresenter {
	public void setWords(Map<String, Integer> wordsToId);
	public Instance convertNumbers(Set<Integer> wordsID);
	public FastVector getAttributes();
}
