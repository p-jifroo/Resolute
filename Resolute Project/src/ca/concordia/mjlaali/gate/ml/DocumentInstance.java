package ca.concordia.mjlaali.gate.ml;

import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.util.InvalidOffsetException;

public class DocumentInstance implements InstanceExtractor{
	
	private static final long serialVersionUID = 1L;
	
	private static final String DOCUMENT_INSTANCE = "DocumentInstance";

	@Override
	public AnnotationSet getInstances(Document doc) {
		AnnotationSet annotations = doc.getAnnotations("ML_Temp");
		annotations.clear();
		try {
			annotations.add(0L, doc.getContent().size(), DOCUMENT_INSTANCE, Factory.newFeatureMap());
		} catch (InvalidOffsetException e) {
			e.printStackTrace();
		}
		return annotations.get(DOCUMENT_INSTANCE);
	}

}
