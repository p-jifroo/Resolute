package ca.concordia.mjlaali.gate.ml.wekaExporter;

import java.util.List;
import java.util.Set;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.SparseInstance;

public class VectorNumbersRepresenter implements NumberRepresenter{
	private FastVector booleanValue = new FastVector();
	private FastVector atts;
	double[] attValuse;

	public VectorNumbersRepresenter() {
		booleanValue.addElement("0");
		booleanValue.addElement("1");
	}
	
	@Override
	public void setWords(List<String> words) {
		atts = new FastVector(words.size());
		
		for (String word: words){
			atts.addElement(new Attribute(word, booleanValue));
		}
		
		attValuse = new double[atts.size()];
	}

	@Override
	public Instance convertNumbers(Set<Integer> wordsID) {
		for (int i = 0; i < attValuse.length; ++i)
			attValuse[i] = 0;
		
		for (Integer id: wordsID){
			attValuse[id] = 1;
		}
		
		Instance instance = new SparseInstance(1, attValuse);
		return instance;
	}

	@Override
	public FastVector getAttributes() {
		return atts;
	}

}
