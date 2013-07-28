package ca.concordia.mjlaali.gate.ml.wekaExporter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import weka.core.Instance;
import weka.core.Instances;

public class BooleanVectorModel implements WekaAttributeExporter{
	private Map<String, Integer> bagofwords = new TreeMap<>();
	private List<Set<Integer>> vectorModels = new LinkedList<>();
	private NumberRepresenter numberRepresenter;
	
	public BooleanVectorModel(NumberRepresenter numberRepresenter) {
		this.numberRepresenter = numberRepresenter;
	}

	@Override
	public void addInstance(Set<String> words){
		if (words.size() == 0)
			System.err.println("Zero word for attribute");
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
	
	@Override
	public Instances getInstances(String name){
		numberRepresenter.setWords(bagofwords);
		Instances dataRaw = new Instances(name, numberRepresenter.getAttributes(), 0);
		for (Set<Integer> wordsID: vectorModels){
			Instance instance = numberRepresenter.convertNumbers(wordsID);
			dataRaw.add(instance);
		}
		return dataRaw;
	}

}
