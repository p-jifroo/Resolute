package ca.concordia.mjlaali.tool;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.corpora.SerialCorpusImpl;
import gate.creole.ResourceInstantiationException;

public class MyGateTools {

	public static Corpus copyFromCorpus(int start, int cnt, SerialCorpusImpl corpus) throws ResourceInstantiationException{
		Corpus testCorpus = Factory.newCorpus("test corpus");
		for (int i = start; i < start + cnt && i < corpus.size(); ++i){
			Document doc = corpus.get(i);
			corpus.unloadDocument(doc, false);
			Document copyDoc = (Document)Factory.duplicate(doc);
			copyDoc.getAnnotations().addAll(doc.getAnnotations());
			testCorpus.add(copyDoc);
		}
		return testCorpus;
	}

	public static void releaseCorpus(Corpus corpus) throws ResourceInstantiationException{
		for (Document doc: corpus){
			Factory.deleteResource(doc);
		}
		corpus.clear();
		Factory.deleteResource(corpus);
	}

}
