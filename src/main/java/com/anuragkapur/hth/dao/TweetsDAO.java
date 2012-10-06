/**
 * 
 */
package com.anuragkapur.hth.dao;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.anuragkapur.hth.proximity.ProximityCalculator;
import com.anuragkapur.hth.twitterclient.TweetRideMatches;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * @author anurag.kapur
 * 
 */
public class TweetsDAO {

	private static final Logger LOGGER = Logger.getLogger(TweetsDAO.class);
	private static final String COLLECTION_NAME = "tweets";

	public void processTweet(String tweet) throws UnknownHostException {
		DBObject tweetObj = (DBObject) JSON.parse(tweet);
		LOGGER.debug(tweetObj.toString());

		String tweetType = getTweetType(tweetObj);
		LOGGER.debug("Tweet Action of TYPE :: " + tweetType);

		if (tweetType.equalsIgnoreCase("text")) {

			BasicDBObject tweetToInsert = new BasicDBObject();
			tweetToInsert.put("_id", tweetObj.get("id_str"));
			tweetToInsert.put("coordinates", tweetObj.get("coordinates"));
			tweetToInsert.put("time", tweetObj.get("created_at"));

			// Process hashtags
			DBObject entities = (DBObject) tweetObj.get("entities");
			LOGGER.debug("Tweet Hashtags :: " + entities.get("hashtags"));
			List hashTags = (List) entities.get("hashtags");
			if (hashTags.size() < 3) {
				// Not a valid get a ride request
				return;
			}

			Iterator iterator = hashTags.iterator();
			int hashtagCounter = 0;
			while (iterator.hasNext()) {
				hashtagCounter++;
				DBObject hashtag = (DBObject) iterator.next();
				if (hashtagCounter == 1) {
					tweetToInsert.put("from", hashtag.get("text"));
				} else if (hashtagCounter == 2) {
					tweetToInsert.put("to", hashtag.get("text"));
				} else if (hashtagCounter == 3) {
					tweetToInsert.put("time", hashtag.get("text").toString()
							.substring(1));
				}
			}

			// Process user info
			DBObject userData = (DBObject) tweetObj.get("user");
			tweetToInsert.put("twitterHandle", userData.get("screen_name"));
			
			// Initialise and unserviced
			tweetToInsert.put("serviced", "false");

			insertTweet(tweetToInsert);
		}
	}
	
	public void processRideRequests() throws UnknownHostException {
		BasicDBObject query = new BasicDBObject();
		query.put("serviced", "false");
		
		DBCursor cursor = DBConnectionManager.getCollection(COLLECTION_NAME).find(query);
		DBObject tweetObject = null;
		if (cursor.size() <= 0) {
			LOGGER.debug("No unprocessed requests");
		}
		while (cursor.hasNext()) {
			tweetObject = cursor.next();
			LOGGER.debug("Will look for a match for :: " + tweetObject.get("twitterHandle"));
			if (findMatch(tweetObject)) {
				break;
			}else {
				LOGGER.debug("No match found for :: " + tweetObject.get("twitterHandle"));
			}
		}
	}
	
	public boolean findMatch(DBObject request) throws UnknownHostException {
		ProximityCalculator proximityCalc = new ProximityCalculator();
		
		boolean matchFound = false;
		BasicDBObject query = new BasicDBObject();
//		query.put("from", request.get("from"));
//		query.put("to", request.get("to"));
		query.put("time", request.get("time"));
		query.put("_id", new BasicDBObject("$ne", request.get("_id")));
		query.put("twitterHandle", new BasicDBObject("$ne", request.get("twitterHandle")));
		
		DBCursor cursor = DBConnectionManager.getCollection(COLLECTION_NAME).find(query);
		while(cursor.hasNext()) {
			DBObject prospectiveMatch = cursor.next();
			String prospectiveFrom = prospectiveMatch.get("from").toString();
			String requestFrom = request.get("from").toString();
			String prospectiveTo = prospectiveMatch.get("to").toString();
			String requestTo = request.get("to").toString();
			
			float sourceDistances = proximityCalc.Calcproximit(prospectiveFrom, requestFrom);
			float destinationDistances = proximityCalc.Calcproximit(prospectiveTo, requestTo);
			
			LOGGER.debug("Distance :: " + sourceDistances + " and " + destinationDistances);
			
			if (sourceDistances < 2 && destinationDistances < 2) {
				matchFound = true;
				markRequestsAsServiced(request, prospectiveMatch);
			}
		}
		
		if (!matchFound) {
			LOGGER.debug("No matched found for :: " + request.get("twitterHandle"));
		}
		
		return matchFound;
	}
	
	private void markRequestsAsServiced(DBObject request, DBObject match) throws UnknownHostException {
		request.put("serviced", "true");
		match.put("serviced", "true");
		
		DBConnectionManager.getCollection(COLLECTION_NAME).update(new BasicDBObject("_id",request.get("_id")), request);
		DBConnectionManager.getCollection(COLLECTION_NAME).update(new BasicDBObject("_id",match.get("_id")), match);
		Date date = new Date(System.currentTimeMillis());
		TweetRideMatches.postMatch("@GetARideHack found a ride match for @" + request.get("twitterHandle") + " @" + match.get("twitterHandle") + " at " + date.toString());
		
		LOGGER.info("Matches found and served :: " + request.get("twitterHandle") + " and " + match.get("twitterHandle"));
	}

	private String getTweetType(DBObject tweetObj) {
		Map tweetMap = tweetObj.toMap();
		Set tweetMapKeys = tweetMap.keySet();
		Iterator keysIterator = tweetMapKeys.iterator();
		String key = null;

		if (keysIterator.hasNext()) {
			key = (String) keysIterator.next();
		}

		return key;
	}

	private void insertTweet(DBObject tweetObj) throws UnknownHostException {
		LOGGER.info("Inserting tweet into DB :: " + tweetObj.toString());
		DBConnectionManager.getCollection(COLLECTION_NAME).insert(tweetObj);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TweetsDAO tDao = new TweetsDAO();
	}

}
