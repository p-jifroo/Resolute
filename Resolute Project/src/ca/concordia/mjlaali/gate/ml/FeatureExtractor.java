package ca.concordia.mjlaali.gate.ml;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Controller;
import gate.Document;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ControllerAwarePR;
import gate.creole.ExecutionException;

import java.io.File;
import java.util.Iterator;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;
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
	private boolean manual;

	private static final long serialVersionUID = 1L;
	

	@Override
	public void controllerExecutionStarted(Controller c)
			throws ExecutionException {
	}
	
	@Override
	public void controllerExecutionFinished(Controller c)
			throws ExecutionException {
		if (!manual)
			exportFeature();
	}

	public void exportFeature() {
		Instances structure = attributeCalculator.getInstances();
		
		try {
			ArffSaver saver = new ArffSaver();
			saver.setRetrieval(Saver.INCREMENTAL);
			saver.setStructure(structure);
			saver.setFile(new File(exportFileName));
			for (Instance ins: attributeCalculator)
				saver.writeIncremental(ins);
			saver.writeIncremental(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Iterator<Instance> iterInstance = null;
	public Instance getInstance(){
		if (iterInstance == null)
			iterInstance = attributeCalculator.iterator();
		if (iterInstance.hasNext())
			return iterInstance.next();
		return null;
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
	
	public void setManual(boolean manual) {
		this.manual = manual;
	}
	
	public static void main(String[] args) {
		SparseInstance a = new SparseInstance(4, new double[]{0, 1, 1, 0});
		SparseInstance b = new SparseInstance(4, new double[]{1, 1, 0, 1});
		System.out.println(a.mergeInstance(b));
	}
}
