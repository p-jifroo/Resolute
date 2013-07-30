package ca.concordia.mjlaali.gate.ml.wekaExporter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class TFIDFModel implements WordIndexer{
	public static final String NOT_SEEN = "NOT_SEEN";
	private static final String IDX = "idx";
	private static final String WORDS = "words";
	private IndexWriter writer;
	private int instanceCount;
	private int dfMin;
	private double dfMaxPercentage;
	private Directory dir;
	private IndexReader reader;

	class InstanceIterator implements Iterator<Set<Integer>>{
		private int currentDocIndex = 0;
		private IndexReader reader;
		private Map<String, Integer> wordsToId = new TreeMap<>();
		
		public InstanceIterator(Directory dir) throws CorruptIndexException, IOException {
			reader = IndexReader.open(dir);
			List<String> words = getWords();
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
				Document doc = reader.document(currentDocIndex);
				Fieldable fieldable = doc.getFieldable(IDX);
				if (!fieldable.stringValue().equals("" + currentDocIndex))
					throw new RuntimeException("Index of document and iteration are not the same");
				
				TermFreqVector termFreqVector = reader.getTermFreqVector(currentDocIndex, WORDS);
				Set<Integer> wordsId = new TreeSet<>(); 
				for (String word: termFreqVector.getTerms()){
					Integer idx = wordsToId.get(word);
					if (idx != null)
						wordsId.add(idx);
					else
						wordsId.add(wordsToId.get(NOT_SEEN));
				}
				++currentDocIndex;
				return wordsId;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	public TFIDFModel(String indexPath, boolean create, int dfMin, double dfMaxPercentage) throws IOException {
		this.dfMin = dfMin;
		this.dfMaxPercentage = dfMaxPercentage;
		dir = FSDirectory.open(new File(indexPath));
		Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_35);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35, analyzer);
		if (create) {
			// Create a new index in the directory, removing any
			// previously indexed documents:
			iwc.setOpenMode(OpenMode.CREATE);
		} else {
			// Add new documents to an existing index:
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		}
		
		// Optional: for better indexing performance, if you
		// are indexing many documents, increase the RAM
		// buffer.  But if you do this, increase the max heap
		// size to the JVM (eg add -Xmx512m or -Xmx1g):
		//
		iwc.setRAMBufferSizeMB(256.0);
		writer = new IndexWriter(dir, iwc);
	}

	@Override
	public Iterator<Set<Integer>> iterator() {
		try {
			return new InstanceIterator(dir);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void index(List<String> words) {
		StringBuilder strWords = new StringBuilder();
		for (String word: words){
			strWords.append(word);
			strWords.append(" ");
		}
		Document doc = new Document();
		doc.add(new Field(WORDS, strWords.toString(), Field.Store.YES, Field.Index.ANALYZED, TermVector.YES));
		doc.add(new Field(IDX, "" + instanceCount++, Field.Store.YES, Field.Index.NO));
		try {
			writer.addDocument(doc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public List<String> getWords() {
		try {
			writer.close();
			List<String> words = new LinkedList<>();
			reader = IndexReader.open(dir);
			long dfMax = Math.round(dfMaxPercentage * reader.numDocs());
			TermEnum termEnum = reader.terms();
			while (termEnum.next()) {
				int docFreq = termEnum.docFreq();
				if (dfMin <= docFreq && docFreq <= dfMax){
					Term term = termEnum.term();
					words.add(term.text()); 
				}
			}
			words.add(NOT_SEEN);
			termEnum.close(); 
			reader.close();
			return words;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
	}

	public static void main(String[] args) throws IOException {
		TFIDFModel tfidfModel = new TFIDFModel("output/luceneidx", true, 1, 0.5);
		tfidfModel.index(Arrays.asList(new String[]{"it", "is", "test"}));
		tfidfModel.index(Arrays.asList(new String[]{"another", "lucene", "test"}));
		System.out.println(tfidfModel.getWords());
		Iterator<Set<Integer>> iterator = tfidfModel.iterator();
		while(iterator.hasNext())
			System.out.println(iterator.next());
	}
}
