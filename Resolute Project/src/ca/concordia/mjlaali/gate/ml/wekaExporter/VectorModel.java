package ca.concordia.mjlaali.gate.ml.wekaExporter;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;

public class VectorModel implements NumberRepresenter{
	private FastVector booleanValue = new FastVector();
	private FastVector atts;

	public VectorModel() {
		booleanValue.addElement("0");
		booleanValue.addElement("1");
	}
	
	@Override
	public void setWords(Map<String, Integer> wordsToId) {
		atts = new FastVector(wordsToId.size());
		
		for (Entry<String, Integer> wordID: wordsToId.entrySet()){
			atts.addElement(new Attribute(wordID.getKey(), booleanValue));
		}
	}

	@Override
	public Instance convertNumbers(Set<Integer> wordsID) {
		double[] attValuse = new double[atts.size()];
		for (Integer id: wordsID){
			attValuse[id] = 1;
		}
		
		Instance instance = new Instance(1, attValuse);
		return instance;
	}

	@Override
	public FastVector getAttributes() {
		return atts;
	}

}
