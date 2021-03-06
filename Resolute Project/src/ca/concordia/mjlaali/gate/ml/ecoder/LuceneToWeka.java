package ca.concordia.mjlaali.gate.ml.ecoder;


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.Directory;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;

/**
 * Convert the {@link Directory} to Weka convention. The main responsibility of this class is converting {@link Document}
 * to the {@link Instance}. It also able to do this in batch format. In other word, this feature converts {@link Directory}
 * to the ARFF file. 
 * @author mjlaali
 *
 */
public class LuceneToWeka {
	private Map<String, WekaEncoder> name2Encoder;
	private IndexReader reader;
	private Instances structure;
	
	/**
	 * Initial the class
	 * @param dirDocs Directory contains extracted features 
	 * @param name2Encoder Different encoder that converts specific field of document to {@link Attribute} 
	 * @throws CorruptIndexException 
	 * @throws IOException
	 */
	public LuceneToWeka(Directory dirDocs, Map<String, WekaEncoder> name2Encoder) throws CorruptIndexException, IOException {
		reader = IndexReader.open(dirDocs);
		this.name2Encoder = name2Encoder;
	}

	/**
	 * Create structure of ARFF file. As in testing, the class needs to do same approach that used in training, 
	 * this method needs to get {@link Directory} in the training section.
	 * @param dirTrain Directory that indicate the previous training data words.
	 * @param name The name of structures. This is the same as in relation in Weka
	 * @return The structure of instances
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public Instances buildStructure(Directory dirTrain, String name) throws CorruptIndexException, IOException{
		IndexReader reader = IndexReader.open(dirTrain);

		for (Entry<String, WekaEncoder> nameEncoder: name2Encoder.entrySet()){
			nameEncoder.getValue().setNumDoc(reader.numDocs());
		}
		
		TermEnum terms = reader.terms();
		while (terms.next()){
			Term term = terms.term();
			WekaEncoder encoder = name2Encoder.get(term.field());
			if (encoder != null)
				encoder.addWord(term.text(), terms.docFreq());
		}

		reader.close();
		FastVector atts = new FastVector();
		for (Entry<String, WekaEncoder> nameEncoder: name2Encoder.entrySet()){
			atts.appendElements(nameEncoder.getValue().getAttribute(nameEncoder.getKey()));
		}
		
		structure = new Instances(name, atts, 0);
		return structure;
	}
	
	/**
	 * Get an instance for specific document in {@link Directory}
	 * @param idxDoc index of document
	 * @return the instance for input document.
	 * @throws IOException
	 */
	public Instance getInstance(int idxDoc) throws IOException{
		double[] values = new double[structure.numAttributes()];
		int lastValue = 0;
		for (Entry<String, WekaEncoder> nameEncoder: name2Encoder.entrySet()){
			TermFreqVector termFreqVector = reader.getTermFreqVector(idxDoc, nameEncoder.getKey());
			if (termFreqVector == null){	//if this document miss the feature values
				System.err.println("LuceneToWeka.getInstance(): the document miss the value of " + nameEncoder.getKey());
				continue;
			}
			String[] terms = termFreqVector.getTerms();
			double[] termValue = nameEncoder.getValue().getValues(terms);
			for (int i = 0; i < termValue.length; ++i){
				values[lastValue + i] = termValue[i]; 
			}
			lastValue += termValue.length;
		}
		return new SparseInstance(1.0, values);
	}
	
	/**
	 * @return Get the number of documents in initialize {@link Directory}.
	 */
	public int getNumDoc(){
		return reader.numDocs();
	}
	
	/**
	 * close the encoder and release all resources
	 * @throws IOException
	 */
	public void close() throws IOException{
		reader.close();
	}

	/**
	 * Do batch processing and convert all {@link Document} to ARFF file 
	 * @param file the name ARFF file
	 * @throws IOException
	 */
	public void saveAsArff(File file) throws IOException {
		ArffSaver arffSaver = new ArffSaver();
		arffSaver.setFile(file);
		arffSaver.setRetrieval(Saver.INCREMENTAL);
		arffSaver.setStructure(structure);
		
		for (int i = 0; i < getNumDoc(); ++i){
			Instance instance = getInstance(i);
			instance.setDataset(structure);
			arffSaver.writeIncremental(instance);
		}
		arffSaver.writeIncremental(null);
	}
	
}
