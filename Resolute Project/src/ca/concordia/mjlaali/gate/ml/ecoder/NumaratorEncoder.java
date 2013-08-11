package ca.concordia.mjlaali.gate.ml.ecoder;

import ca.concordia.mjlaali.gate.ml.AttributeCalculator;
import weka.core.Attribute;
import weka.core.FastVector;

/**
 * Encode the input feature value as a numerator. Therefore, the output of {@link AttributeCalculator}, which is a Set
 * String, is represented as numerator. This type of encoding is useful for attribute such as class which there all 
 * multiple value for it and only one of them is represented in the attribute
 * @author mjlaali
 *
 */
public class NumaratorEncoder implements WekaEncoder{
	private FastVector numaratorValues = new FastVector();
	
	@Override
	public void setNumDoc(int numDoc) {
	}

	@Override
	public void addWord(String text, int docFreq) {
		if (numaratorValues.indexOf(text) == -1)
			numaratorValues.addElement(text);
	}

	@Override
	public FastVector getAttribute(String attName) {
		FastVector atts = new FastVector();
		atts.addElement(new Attribute(attName, numaratorValues));
		return atts;
	}

	@Override
	public double[] getValues(String[] terms) {
		if (terms.length > 1)
			throw new UnsupportedOperationException("More than one word is not supported in NumaratorEncoder");
		else if (terms.length == 0)
			return new double[0];
		double[] values = new double[1];
		values[0] = numaratorValues.indexOf(terms[0]);
		return values;
	}

}
