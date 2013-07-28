package ca.concordia.mjlaali.gate.ml.wekaExporter;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;

public class NumeratorRepresenter implements NumberRepresenter{
	private FastVector att = new FastVector();
	
	@Override
	public void setWords(Map<String, Integer> wordsToId) {
		for (Entry<String, Integer> wordID: wordsToId.entrySet()){
			att.addElement(wordID.getKey());
		}
	}

	@Override
	public Instance convertNumbers(Set<Integer> wordsID) {
		if (wordsID.size() != 1)
			throw new RuntimeException("Numerator can not support more than one value");
		
		return new Instance(1, new double[]{wordsID.iterator().next()});
	}

	@Override
	public FastVector getAttributes() {
		FastVector atts = new FastVector();
		atts.addElement(new Attribute("numerator", att));
		return atts;
	}

}
