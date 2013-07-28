package ca.concordia.mjlaali.tool;

import java.util.Timer;
import java.util.TimerTask;


public class ConsolProgressBar extends TimerTask{
	private double chunkSize;
	private double processed = 0;
	private int precision;
	private long numProcessedChunk = 0;
	private long remained;
	private Timer timer;
	
	private double speed;
	private double avgSpeed = -1;
	private String name;
	
	public static final double ALPHA = 0.95;

	public ConsolProgressBar(long totalSize, int precision) {
		this("", totalSize, precision);
	}
	
	public ConsolProgressBar(String name, long totalSize, int precision) {
		this.precision = precision;
		this.remained = totalSize;
		chunkSize = (double) totalSize / precision;
		
		timer = new Timer(true);
		timer.schedule(this, 0, 1000);
		this.name = name;
	}
	
	public synchronized void progress(long progressSize){
		processed += progressSize;
		remained -= progressSize;
		speed += progressSize;
		
		if (processed > chunkSize){
			int chunkCount = (int)(processed / chunkSize);
			numProcessedChunk += chunkCount;
			processed -= chunkCount * chunkSize;
		}
		
		if (remained == 0)
			timer.cancel();
		
	}

	@Override
	public synchronized boolean cancel() {
		timer.cancel();
		return super.cancel();
	}
	
	private int time = 0;
	private int count = 0;
	
	@Override
	public synchronized void run() {
		if (avgSpeed < 0)
			avgSpeed = speed;
		else
			avgSpeed = ALPHA * avgSpeed + (1 - ALPHA) * speed;
		speed = 0;
		++count;
		++time;
		System.err.print(".");
		
		if (time == 10){
			time = 0;
			System.err.print("(" + numProcessedChunk + "/" + precision + ")");
			if (avgSpeed != 0) {
				int sec = (int)(remained / avgSpeed);
				int hrs = sec / 3600;
				sec = sec % 3600;
				int min = sec / 60;
				sec = sec % 60;
				if (count != 0)
					System.err.print("{" + hrs + ":" + min + ":" + sec + "remained, speed = " + avgSpeed + "}");
				else
					System.err.print("{ No Progress }");
				count = 0;
			} 
			if (avgSpeed == 0)
				System.err.print("{ INF remained}");
			System.err.println(name);
		}
	}

	public static void main(String[] args) {
		ConsolProgressBar progressBar = new ConsolProgressBar((long)1000 * 1000 * 10000000, 100);
		long count = 0;
		for (int i = 0; i < 1000; ++i)
			for (int j = 0; j < 1000; ++j){
				for (int k = 0; k < 10000000; ++k){
					++count;
					progressBar.progress(1);
				}
			}
		System.out.println(count);
					
	}

}
