package ca.concordia.mjlaali.gate.ml.attributeCalculator;

import java.util.Iterator;

import weka.core.Instance;
import weka.core.Instances;

class CompositIterator implements Iterator<Instance>{
	private Iterator<Instance>[] iters;
	private Instances[] dummies;
	public CompositIterator(Iterator<Instance>[] iters, Instances[] structures) {
		this.iters = iters;
		dummies = new Instances[iters.length];
		for (int i = 0; i < structures.length; ++i)
			dummies[i] = new Instances(structures[i], 0, 0);
			
	}

	@Override
	public boolean hasNext() {
		for (Iterator<Instance> iter: iters)
			if (!iter.hasNext())
				return false;
		return true;
	}

	@Override
	public Instance next() {
		for (int i = 0; i < dummies.length; ++i){
			dummies[i].delete();
			dummies[i].add(iters[i].next());
		}
		
		Instances merged = null;
		for (Instances dummy: dummies){
			if (merged == null)
				merged = dummy;
			else
				merged = Instances.mergeInstances(merged, dummy);
		}
		
		return merged.firstInstance();
	}

	@Override
	public void remove() {
		for (Iterator<Instance> iter: iters){
			iter.remove();
		}
	}
	
}