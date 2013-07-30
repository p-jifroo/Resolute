package ca.concordia.resolute.core.evaluation;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Utils;
import gate.util.GateException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import ca.concorida.resolute.core.textmining.RuleBaseAgeDetectionTest;

/**
 * A class that is used for evaluation of annotated document in GATE with the Gold annotation. 
 * @author mjlaali
 *
 */
public class AnnotationEvaluation {
	int cntIntersection, cntGold, cntOut;
	
	/**
	 * Compute the number of correct and incorrect annotation for a GATE document. 
	 * @param gold gold annotations
	 * @param out output annotations for testing purpose 
	 */
	public void evaluate(AnnotationSet gold, AnnotationSet out){
		List<Annotation> annListGold = Utils.inDocumentOrder(gold);
		List<Annotation> annListOut = Utils.inDocumentOrder(out);

		Iterator<Annotation> iterGold = annListGold.iterator();
		Iterator<Annotation> iterOut = annListOut.iterator();
		
		cntIntersection = 0; cntGold = annListGold.size(); cntOut = annListOut.size();
		
		Annotation annGold = null, annOut = null;
		while (iterGold.hasNext() && iterOut.hasNext()){
			//Go next
			if (annGold == null && annOut == null){
				annGold = iterGold.next();
				annOut = iterOut.next();
			} else {
				long annGoldStart = annGold.getStartNode().getOffset().longValue();
				long annOutStart = annOut.getStartNode().getOffset().longValue();
				long annGoldEnd = annGold.getEndNode().getOffset().longValue();
				long annOutEnd = annOut.getEndNode().getOffset().longValue();

				if (annGoldStart < annOutStart){
					annGold = iterGold.next();
				} else if (annGoldStart > annOutStart){
					annOut = iterOut.next();
				} else {
					if (annGoldEnd < annOutEnd)
						annGold = iterGold.next();
					else if (annGoldEnd > annGoldStart){
						annOut = iterOut.next();
					} else {
						annGold = iterGold.next();
						annOut = iterOut.next();
					}
				}
			}

			long annGoldStart = annGold.getStartNode().getOffset().longValue();
			long annOutStart = annOut.getStartNode().getOffset().longValue();
			long annGoldEnd = annGold.getEndNode().getOffset().longValue();
			long annOutEnd = annOut.getEndNode().getOffset().longValue();

			if ( (annGoldStart == annOutStart) && (annGoldEnd == annOutEnd))
				++cntIntersection;

		}
	}
	
	/**
	 * computer precision of new annotation. run {@link #evaluate(AnnotationSet, AnnotationSet)} before running this method 
	 * @return the precision 
	 */
	public double getPrecision(){
		if (cntOut != 0)
			return ((double)cntIntersection) / cntOut;
		return 1.0; 
			
	}

	/**
	 * computer recall of new annotation. run {@link #evaluate(AnnotationSet, AnnotationSet)} before running this method 
	 * @return the recall
	 */
	public double getRecall(){
		if (cntGold != 0)
			return ((double)cntIntersection) / cntGold;
		return 1.0;
	}
	
	public int getCntGold() {
		return cntGold;
	}
	
	public int getCntIntersection() {
		return cntIntersection;
	}
	
	public int getCntOut() {
		return cntOut;
	}
	
	public static void main(String[] args) throws GateException, IOException {
		RuleBaseAgeDetectionTest.init();
		RuleBaseAgeDetectionTest ruleBaseAgeDetectionTest = new RuleBaseAgeDetectionTest();
		ruleBaseAgeDetectionTest.precision();
	}

}
