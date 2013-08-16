package ca.concordia.resolute.core.evaluation;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Utils;
import gate.util.GateException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import ca.concorida.resolute.core.textmining.RuleBaseAgeDetectionTest;

/**
 * A class that is used for evaluation of annotated document in GATE with the Gold annotation. 
 * @author mjlaali
 *
 */
public class AnnotationEvaluation {
	private int cntIntersection, cntGold, cntOut;
	private Annotation annGold = null, annOut = null;
	private Iterator<Annotation> iterGold, iterOut;
	
	/**
	 * Compute the number of correct and incorrect annotation for a GATE document. 
	 * @param gold gold annotations
	 * @param out output annotations for testing purpose 
	 */
	public void evaluate(AnnotationSet gold, AnnotationSet out){
		//get all annotations of documents in the document order
		List<Annotation> annListGold = Utils.inDocumentOrder(gold);
		List<Annotation> annListOut = Utils.inDocumentOrder(out);

		iterGold = annListGold.iterator();
		iterOut = annListOut.iterator();
		
		//the number correct annotation, number of gold annotation, number of output annotation
		cntIntersection = 0; cntGold = annListGold.size(); cntOut = annListOut.size();
		
		//iter over both annotations
		while (true){
			try {
				goNext();
			} catch (NoSuchElementException e) {
				break;
			}

			long annGoldStart = annGold.getStartNode().getOffset().longValue();
			long annOutStart = annOut.getStartNode().getOffset().longValue();
			long annGoldEnd = annGold.getEndNode().getOffset().longValue();
			long annOutEnd = annOut.getEndNode().getOffset().longValue();

			if ( (annGoldStart == annOutStart) && (annGoldEnd == annOutEnd))
				++cntIntersection;

		}
	}

	private void goNext() {
		
		if (annGold == null && annOut == null){	//it is first time
			annGold = iterGold.next();
			annOut = iterOut.next();
		} else {	//detect which annotation is below the other and update only the below one
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
				else if (annGoldEnd > annGoldEnd){
					annOut = iterOut.next();
				} else {	//both annotations should be updated
					annGold = iterGold.next();
					annOut = iterOut.next();
				}
			}
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
