package ca.concordia.mjlaali.gate;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Controller;
import gate.creole.ANNIEConstants;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ControllerAwarePR;
import gate.creole.ExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class FeatureExtractor extends AbstractLanguageAnalyser implements ControllerAwarePR{

	public static final String DEFUALT_OUTFILE = "output/features.arff";

	private BooleanVectorModel booleanVectorModel = new BooleanVectorModel();


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void controllerExecutionStarted(Controller c)
			throws ExecutionException {
		System.out.println("FeatureExtractor.controllerExecutionStarted()");
	}
	@Override
	public void controllerExecutionFinished(Controller c)
			throws ExecutionException {
		booleanVectorModel.compilte();
		Instances dataSet = booleanVectorModel.getInstances("BagOfWord");

		ArffSaver saver = new ArffSaver();
		saver.setInstances(dataSet);
		try {
			saver.setFile(new File(DEFUALT_OUTFILE));
			saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void controllerExecutionAborted(Controller c, Throwable t)
			throws ExecutionException {
	}

	@Override
	public void execute() throws ExecutionException {
		AnnotationSet tokens = getDocument().getAnnotations().get(ANNIEConstants.TOKEN_ANNOTATION_TYPE);

		Set<String> words = new TreeSet<>();
		for (Annotation ann: tokens){
			String strWord = ann.getFeatures().get(TOKEN_STRING_FEATURE_NAME).toString();
			words.add(strWord);
		}

		booleanVectorModel.addInstance(words);
	}

}
