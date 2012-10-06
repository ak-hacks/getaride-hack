/**
 * 
 */
package com.anuragkapur.hth;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author anurag.kapur
 * 
 */
public class RideMatchFinder {

	private static void doPeriodicWork() {
		System.out.println("Hello World");
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ScheduledExecutorService executor = Executors
				.newSingleThreadScheduledExecutor();

		Runnable periodicTask = new Runnable() {
			public void run() {
				// Invoke method(s) to do the work
				doPeriodicWork();
			}
		};
		
		executor.scheduleWithFixedDelay(periodicTask, 0, 3, TimeUnit.SECONDS);
	}

}
