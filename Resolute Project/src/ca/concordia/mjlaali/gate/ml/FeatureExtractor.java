package ca.concordia.mjlaali.gate.ml;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Controller;
import gate.Document;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ControllerAwarePR;
import gate.creole.ExecutionException;

import java.io.File;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.NonSparseToSparse;
import ca.concordia.mjlaali.gate.ml.attributeCalculator.AttributeCalculator;

/**
 * This class extracts boolean vector feature from a document.
 * @author mjlaali
 *
 */
public class FeatureExtractor extends AbstractLanguageAnalyser implements ControllerAwarePR{

	private InstanceExtractor instanceExtractor;
	private AttributeCalculator attributeCalculator;
	private String exportFileName;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	@Override
	public void controllerExecutionStarted(Controller c)
			throws ExecutionException {
		//TODO: load or reset model here
	}
	@Override
	public void controllerExecutionFinished(Controller c)
			throws ExecutionException {
		Instances dataSet = attributeCalculator.getInstances();
		
		NonSparseToSparse nonSparseToSparse = new NonSparseToSparse();
		try {
			nonSparseToSparse.setInputFormat(dataSet);
			Instances sparseDataset = Filter.useFilter(dataSet, nonSparseToSparse);
			ArffSaver saver = new ArffSaver();
			saver.setInstances(sparseDataset);
			saver.setFile(new File(exportFileName));
			saver.writeBatch();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public void controllerExecutionAborted(Controller c, Throwable t)
			throws ExecutionException {
	}

	@Override
	public void execute() throws ExecutionException {

		Document doc = getDocument();
		AnnotationSet instances = instanceExtractor.getInstances(doc);
		for (Annotation instance: instances){
			attributeCalculator.calAttribute(doc, instance);
		}
		
	}
	
	public void setAttributeCalculator(AttributeCalculator attributeCalculator) {
		this.attributeCalculator = attributeCalculator;
	}
	
	public void setExportFileName(String exportFileName) {
		this.exportFileName = exportFileName;
	}
	
	public void setInstanceExtractor(InstanceExtractor instanceExtractor) {
		this.instanceExtractor = instanceExtractor;
	}
}
