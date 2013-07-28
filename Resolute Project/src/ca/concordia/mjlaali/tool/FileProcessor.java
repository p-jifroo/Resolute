package ca.concordia.mjlaali.tool;

import java.io.File;

/**
 * A processor for a file. Any processor for a file should implement this class. 
 * @author Majid
 *
 */
public interface FileProcessor {
	
	/**
	 * process a file
	 * @param file: input file to process.
	 */
	public void process(File file);
}
