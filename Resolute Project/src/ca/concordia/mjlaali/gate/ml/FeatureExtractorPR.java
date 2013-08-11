package ca.concordia.mjlaali.gate.ml;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;


/**
 * This class extracts features from a document. All extracted features are stored in a {@link Directory}. For each 
 * Instance (refer to {@link InstanceExtractor}), a {@link org.apache.lucene.document.Document} is created and all features
 * are stored as a {@link Fieldable} in the document.
 * @author mjlaali
 *
 */
public class FeatureExtractorPR extends AbstractLanguageAnalyser{
	private InstanceExtractor instanceExtractor;
	private transient IndexWriter indexer;
	private List<AttributeCalculator> attributes = new LinkedList<>();
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public void execute() throws ExecutionException {
		Document doc = getDocument();
		AnnotationSet instances = instanceExtractor.getInstances(doc);
		for (Annotation instance: instances){
			org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document(); 
			for (AttributeCalculator att: attributes){
				Set<String> val = att.getAttributeValue(doc, instance);
				if (val != null && val.size() > 0){
					Fieldable fieldable = getFieldable(att.getName(), val);
					luceneDoc.add(fieldable);
				}
			}
			try {
				indexer.addDocument(luceneDoc);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Fieldable getFieldable(String name, Set<String> words) {
		StringBuilder strWords = new StringBuilder();
		for (String word: words){
			strWords.append(word);
			strWords.append(" ");
		}
		return new Field(name, strWords.toString(), Field.Store.YES, Field.Index.ANALYZED, TermVector.YES);
	}
	
	/**
	 * @param indexer Indexer that is responsible to index features.
	 */
	public void setIndexer(IndexWriter indexer) {
		this.indexer = indexer;
	}

	/**
	 * @param instanceExtractor Instance extractor for a document
	 */
	public void setInstanceExtractor(InstanceExtractor instanceExtractor) {
		this.instanceExtractor = instanceExtractor;
	}
	
	/**
	 * Add an attribute calculator for document.
	 * @param attributeCalculator
	 */
	public void addAttributeCalculator(AttributeCalculator attributeCalculator){
		attributes.add(attributeCalculator);
	}

}
