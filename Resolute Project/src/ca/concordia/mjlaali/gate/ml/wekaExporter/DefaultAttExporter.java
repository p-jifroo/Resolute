package ca.concordia.mjlaali.gate.ml.wekaExporter;

import java.util.List;
import java.util.Set;

import weka.core.Instance;
import weka.core.Instances;

public class DefaultAttExporter implements WekaAttributeExporter{
	private NumberRepresenter numberRepresenter;
	private WordIndexer wordIndexr;
	
	public DefaultAttExporter(NumberRepresenter numberRepresenter, WordIndexer wordIndexer) {
		this.numberRepresenter = numberRepresenter;
		this.wordIndexr = wordIndexer;
	}

	@Override
	public void addInstance(List<String> words){
		wordIndexr.index(words);
	}
	
	@Override
	public Instances getInstances(String name){
		numberRepresenter.setWords(wordIndexr.getWords());
		Instances dataRaw = new Instances(name, numberRepresenter.getAttributes(), 0);
		for (Set<Integer> wordsID: wordIndexr){
			Instance instance = numberRepresenter.convertNumbers(wordsID);
			dataRaw.add(instance);
		}
		return dataRaw;
	}

}
