package ca.concordia.mjlaali.gate;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.SparseInstance;

public class BooleanVectorModel {
	private Map<String, Integer> bagofwords = new TreeMap<>();
	private List<Set<Integer>> vectorModels = new LinkedList<>();
	private FastVector booleanValue = new FastVector();
	
	public BooleanVectorModel() {
		booleanValue.addElement("0");
		booleanValue.addElement("1");
	}

	public void addInstance(Set<String> words){
		Set<Integer> vectorMode = new TreeSet<>();
		for (String word: words){
			Integer idx = bagofwords.get(word);
			if (idx == null){
				idx = new Integer(bagofwords.size());
				bagofwords.put(word, idx);
			}
			vectorMode.add(idx);
		}
		vectorModels.add(vectorMode);
	}
	
	public FastVector compilte(){
		FastVector atts = new FastVector(bagofwords.size());
		
		for (Entry<String, Integer> wordID: bagofwords.entrySet()){
			atts.addElement(new Attribute(wordID.getKey(), booleanValue));
		}
		
		return atts;
	}
	
	public Instances getInstances(String name){
		FastVector atts = compilte();
		Instances dataRaw = new Instances(name, atts, 0);
		for (Set<Integer> wordsID: vectorModels){
			double[] attValuse = new double[bagofwords.size()];
			for (Integer id: wordsID){
				attValuse[id] = 1;
			}
			
			SparseInstance instance = new SparseInstance(1, attValuse);
			dataRaw.add(instance);
		}
		return dataRaw;
	}
	
}
