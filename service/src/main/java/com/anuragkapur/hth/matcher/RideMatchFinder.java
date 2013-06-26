/**
 * 
 */
package com.anuragkapur.hth.matcher;

import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.anuragkapur.hth.dao.TweetsDAO;

/**
 * @author anurag.kapur
 * 
 */
public class RideMatchFinder {

	private static final Logger LOGGER = Logger.getLogger(RideMatchFinder.class);
	
	private static void doPeriodicWork() {
		TweetsDAO tweetsDao = new TweetsDAO();
		try {
			LOGGER.debug("Will process ride requests");
			tweetsDao.processRideRequests();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
