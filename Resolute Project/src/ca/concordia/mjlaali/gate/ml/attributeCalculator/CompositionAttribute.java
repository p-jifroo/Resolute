package ca.concordia.mjlaali.gate.ml.attributeCalculator;

import gate.Annotation;
import gate.Document;
import weka.core.Instances;

public abstract class CompositionAttribute implements AttributeCalculator{
	private AttributeCalculator internal;

	public CompositionAttribute(AttributeCalculator internal) {
		this.internal = internal;
	}

	@Override
	public void calAttribute(Document doc, Annotation instance) {
		calMyAttribute(doc, instance);
		if (internal != null)
			internal.calAttribute(doc, instance);
	}

	@Override
	public Instances getInstances() {
		Instances myInstances = getMyInstances();
		if (internal != null)
			return Instances.mergeInstances(myInstances, internal.getInstances());
		return myInstances;
	}
	
	protected abstract void calMyAttribute(Document doc, Annotation instance);
	protected abstract Instances getMyInstances();

}
