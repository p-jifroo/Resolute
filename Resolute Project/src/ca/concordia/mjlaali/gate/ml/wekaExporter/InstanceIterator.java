package ca.concordia.mjlaali.gate.ml.wekaExporter;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.Directory;

class InstanceIterator implements Iterator<Set<Integer>>{
	/**
	 * 
	 */
	private int currentDocIndex = 0;
	private IndexReader reader;
	private Map<String, Integer> wordsToId = new TreeMap<>();
	
	public InstanceIterator(List<String> words, Directory dir) throws CorruptIndexException, IOException {
		reader = IndexReader.open(dir);
		
		int idx = 0;
		for (String word: words){
			wordsToId.put(word, idx);
			idx++;
		}
		
	}
	

	@Override
	public boolean hasNext() {
		if (currentDocIndex < reader.numDocs())
			return true;

		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Set<Integer> next() {
		try {
//			Document doc = reader.document(currentDocIndex);
//			Fieldable fieldable = doc.getFieldable(TFIDFModel.IDX);
//			if (!fieldable.stringValue().equals("" + currentDocIndex))
//				throw new RuntimeException("Index of document and iteration are not the same");
			
			TermFreqVector termFreqVector = reader.getTermFreqVector(currentDocIndex, TFIDFModel.WORDS);
			Set<Integer> wordsId = new TreeSet<>(); 
			for (String word: termFreqVector.getTerms()){
				Integer idx = wordsToId.get(word);
				if (idx != null)
					wordsId.add(idx);
			}
			++currentDocIndex;
			return wordsId;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}