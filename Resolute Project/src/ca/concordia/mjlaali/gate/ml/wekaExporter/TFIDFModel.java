package ca.concordia.mjlaali.gate.ml.wekaExporter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class TFIDFModel implements WordIndexer{
	enum IndexMode{
		append, create, open
	}
	static final String IDX = "idx";
	static final String WORDS = "words";
	private int instanceCount;
	private int dfMin;
	private double dfMaxPercentage;
	private Directory wordsdir, documentDir;
	private IndexReader reader;
	private Analyzer analyzer;
	private int create;

	public TFIDFModel(String indexPath, int create, int dfMin, double dfMaxPercentage) throws IOException {
		this.dfMin = dfMin;
		this.create = create;
		this.dfMaxPercentage = dfMaxPercentage;
		
		wordsdir = FSDirectory.open(new File(indexPath));
		analyzer = new WhitespaceAnalyzer(Version.LUCENE_35);
		
		if (create == 0) {
			// Create a new index in the directory, removing any
			// previously indexed documents:
			documentDir = wordsdir;
		} else {
			// Add new documents to an existing index:
			documentDir = new RAMDirectory();
		}
		
		// Optional: for better indexing performance, if you
		// are indexing many documents, increase the RAM
		// buffer.  But if you do this, increase the max heap
		// size to the JVM (eg add -Xmx512m or -Xmx1g):
		//
	}

	@Override
	public Iterator<Set<Integer>> iterator() {
		try {
			return new InstanceIterator(getWords(), documentDir);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void index(List<String> words) {
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35, analyzer);
		if (create >= 0){
			iwc.setOpenMode(OpenMode.CREATE);
			instanceCount = 0;
		}
		else 
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		
		if (create == 0)
			create = -1;
		IndexWriter writer = null;
		try {
			writer = new IndexWriter(documentDir, iwc);
			StringBuilder strWords = new StringBuilder();
			for (String word: words){
				strWords.append(word);
				strWords.append(" ");
			}
			Document doc = new Document();
			doc.add(new Field(WORDS, strWords.toString(), Field.Store.YES, Field.Index.ANALYZED, TermVector.YES));
			doc.add(new Field(IDX, "" + instanceCount++, Field.Store.YES, Field.Index.NO));

			writer.addDocument(doc);
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally{
			if (writer != null){
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public List<String> getWords() {
		try {
			List<String> words = new LinkedList<>();
			reader = IndexReader.open(wordsdir);
			long dfMax = Math.round(dfMaxPercentage * reader.numDocs());
			TermEnum termEnum = reader.terms();
			while (termEnum.next()) {
				int docFreq = termEnum.docFreq();
				if (dfMin <= docFreq && docFreq <= dfMax){
					Term term = termEnum.term();
					words.add(term.text()); 
				}
			}
			termEnum.close(); 
			reader.close();
			return words;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
	}

	public static void main(String[] args) throws IOException {
		TFIDFModel tfidfModel = new TFIDFModel("output/luceneidx", 0, 1, 0.5);
		tfidfModel.index(Arrays.asList(new String[]{"it", "is", "test"}));
		tfidfModel.index(Arrays.asList(new String[]{"another", "lucene", "test"}));
		System.out.println(tfidfModel.getWords());
		Iterator<Set<Integer>> iterator = tfidfModel.iterator();
		while(iterator.hasNext())
			System.out.println(iterator.next());
		
		tfidfModel = new TFIDFModel("output/luceneidx", 1, 1, 0.5);
		tfidfModel.index(Arrays.asList(new String[]{"it", "is1", "test"}));
		System.out.println(tfidfModel.getWords());
		iterator = tfidfModel.iterator();
		while(iterator.hasNext())
			System.out.println(iterator.next());
		tfidfModel.index(Arrays.asList(new String[]{"another", "lucene", "test"}));
		iterator = tfidfModel.iterator();
		while(iterator.hasNext())
			System.out.println(iterator.next());

	}
}
