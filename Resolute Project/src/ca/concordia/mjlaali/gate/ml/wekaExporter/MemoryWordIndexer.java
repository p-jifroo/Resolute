package ca.concordia.mjlaali.gate.ml.wekaExporter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class MemoryWordIndexer implements WordIndexer{
	private Map<String, Integer> bagofwords = new TreeMap<>();
	private List<Set<Integer>> vectorModels = new LinkedList<>();

	@Override
	public Iterator<Set<Integer>> iterator() {
		return vectorModels.iterator();
	}

	@Override
	public void index(List<String> words) {
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
	public List<String> getWords() {
		Map<Integer, String> idToWords = new TreeMap<>();
		
		for (Entry<String, Integer> wordID: bagofwords.entrySet()){
			idToWords.put(wordID.getValue(), wordID.getKey());
		}
				
		return new LinkedList<>(idToWords.values());
	}

	public static void main(String[] args) {
		Map<Integer, Integer> testMap = new TreeMap<>();
		
		testMap.put(10, 10);
		testMap.put(5, 5);
		testMap.put(15, 15);
		testMap.put(1, 1);
		testMap.put(20, 20);
		
		System.out.println(testMap.keySet());
		System.out.println(testMap.values());
	}
}
