package ca.concordia.mjlaali.gate.ml.wekaExporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.SparseInstance;

public class NumeratorRepresenter implements NumberRepresenter{
	private FastVector att = new FastVector();
	private List<String> words;
	private String attName;
	
	public NumeratorRepresenter(String attName) {
		this.attName = attName;
	}
	
	@Override
	public void setWords(List<String> words) {
		att.addElement("dummy");
		for (String word: words){
			att.addElement(word);
		}
		this.words = new ArrayList<>(words);
	}

	@Override
	public Instance convertNumbers(Set<Integer> wordsID) {
		if (wordsID.size() != 1)
			throw new RuntimeException("Numerator can not support more than one value");
		
		return new SparseInstance(1, new double[]{att.indexOf(words.get(wordsID.iterator().next()))});
	}

	@Override
	public FastVector getAttributes() {
		FastVector atts = new FastVector();
		atts.addElement(new Attribute(attName, att));
		return atts;
	}
	
	public static void main(String[] args) {
		NumeratorRepresenter test = new NumeratorRepresenter("test");
		test.setWords(new LinkedList<String>(Arrays.asList(new String[]{"test1", "test2"})));
		
		
		Instance convertNumbers = test.convertNumbers(new TreeSet<Integer>(Arrays.asList(new Integer[]{1})));
		System.out.println(convertNumbers);
		
	}

}
