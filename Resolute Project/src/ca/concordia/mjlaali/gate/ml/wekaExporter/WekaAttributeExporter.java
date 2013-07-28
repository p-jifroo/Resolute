package ca.concordia.mjlaali.gate.ml.wekaExporter;

import java.util.Set;

import weka.core.Instances;

public interface WekaAttributeExporter {
	public void addInstance(Set<String> words);
	public Instances getInstances(String name);
}
