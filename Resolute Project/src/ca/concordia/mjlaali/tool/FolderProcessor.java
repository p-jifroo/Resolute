package ca.concordia.mjlaali.tool;

import java.io.File;

/**
 * Process all file in a folder. This class needs {@link FileProcessor} to define a process
 * need to run for each file  
 *  
 * @author Majid
 *
 */
public class FolderProcessor {
	private boolean recrusive;
	
	public void setRecrusive(boolean recrusive) {
		this.recrusive = recrusive;
	}
	
	public void process(File folder, FileProcessor processor){
		File[] files = getListOfFile(folder);
		
		for (File f: files){
			if (f.isFile())
				processor.process(f);
		}
	}
	
	private File[] getListOfFile(File folder){
		if (recrusive)
			System.err.println("FolderProcessor.getListOfFile(): recursive functionallity is not implemented!");
		return folder.listFiles();
	}
}
