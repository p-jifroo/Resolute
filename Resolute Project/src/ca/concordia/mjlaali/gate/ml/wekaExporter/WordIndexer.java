package ca.concordia.mjlaali.gate.ml.wekaExporter;

import java.util.List;
import java.util.Set;

public interface WordIndexer extends Iterable<Set<Integer>>{

	public void index(List<String> words);
	public List<String> getWords();

}
