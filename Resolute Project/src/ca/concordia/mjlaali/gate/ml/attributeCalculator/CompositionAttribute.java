package ca.concordia.mjlaali.gate.ml.attributeCalculator;

import gate.Annotation;
import gate.Document;

import java.util.Iterator;

import weka.core.Instance;
import weka.core.Instances;

public abstract class CompositionAttribute implements AttributeCalculator{
	private AttributeCalculator internal;
	private Instances myInstances;
	private Instances internalInstances;
	
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
		myInstances = getMyInstances();
		if (internal != null) {
			internalInstances = internal.getInstances();
			return Instances.mergeInstances(myInstances, internalInstances);
		}
		return myInstances;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Instance> iterator() {
		if (internal != null)
			return new CompositIterator(new Iterator[]{myIterator(), internal.iterator()}, 
					new Instances[]{myInstances, internalInstances});
		else
			return myIterator();
	}
	
	protected abstract Iterator<Instance> myIterator(); 

	protected abstract void calMyAttribute(Document doc, Annotation instance);
	protected abstract Instances getMyInstances();

}
