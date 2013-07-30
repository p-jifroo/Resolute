package ca.concordia.mjlaali.gate.ml.wekaExporter;

import java.util.List;

import weka.core.Instances;

public interface WekaAttributeExporter {
	public void addInstance(List<String> words);
	public Instances getInstances(String name);
}
