package ca.concordia.mjlaali.gate.ml.wekaExporter;

import java.util.List;
import java.util.Set;

import weka.core.FastVector;
import weka.core.Instance;

public interface NumberRepresenter {
	public void setWords(List<String> words);
	public Instance convertNumbers(Set<Integer> wordsID);
	public FastVector getAttributes();
}
