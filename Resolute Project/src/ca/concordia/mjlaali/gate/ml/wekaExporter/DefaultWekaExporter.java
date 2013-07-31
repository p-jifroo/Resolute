package ca.concordia.mjlaali.gate.ml.wekaExporter;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import weka.core.Instance;
import weka.core.Instances;

public class DefaultWekaExporter implements WekaAttributeExporter{
	private NumberRepresenter numberRepresenter;
	private WordIndexer wordIndexr;
	
	class InstanceGenerator implements Iterator<Instance>{
		Iterator<Set<Integer>> wordsIterator;
		public InstanceGenerator() {
			wordsIterator = wordIndexr.iterator();
			
		}

		@Override
		public boolean hasNext() {
			return wordsIterator.hasNext();
		}

		@Override
		public Instance next() {
			return numberRepresenter.convertNumbers(wordsIterator.next());
		}

		@Override
		public void remove() {
			wordsIterator.remove();
		}
		
	}
	
	public DefaultWekaExporter(NumberRepresenter numberRepresenter, WordIndexer wordIndexer) {
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
		return dataRaw;
	}

	@Override
	public Iterator<Instance> iterator() {
		return new InstanceGenerator();
	}

}
