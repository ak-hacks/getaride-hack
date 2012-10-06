/**
 * 
 */
package com.anuragkapur.hth.dao;

import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

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

			insertTweet(tweetToInsert);
		}
	}
	
	public void processRideRequests() throws UnknownHostException {
		DBObject request = getFirstUnprocessedTweet();
		
		if (request != null) {
			BasicDBObject query = new BasicDBObject();
			query.put("from", request.get("from"));
			query.put("to", request.get("to"));
			query.put("time", request.get("time"));
			query.put("_id", new BasicDBObject("$ne", request.get("_id")));
			
			DBCursor cursor = DBConnectionManager.getCollection(COLLECTION_NAME).find(query);
			cursor.limit(1);
			DBObject match = null;
			
			if (cursor.hasNext()) {
				match = cursor.next();
				markRequestsAsServiced(request, match);
			}else {
				LOGGER.debug("No match found for :: " + request.get("twitterHandle"));
			}
		}
	}
	
	private void markRequestsAsServiced(DBObject request, DBObject match) throws UnknownHostException {
		request.put("serviced", "true");
		match.put("serviced", "true");
		
		DBConnectionManager.getCollection(COLLECTION_NAME).update(new BasicDBObject("_id",request.get("_id")), request);
		DBConnectionManager.getCollection(COLLECTION_NAME).update(new BasicDBObject("_id",match.get("_id")), match);
		
		LOGGER.info("Match found and served :: " + request.get("twitterHandle") + " and " + match.get("twitterHandle"));
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
	
	private DBObject getFirstUnprocessedTweet() throws UnknownHostException {
		BasicDBObject query = new BasicDBObject();
		query.put("serviced", "false");
		DBCursor cursor = DBConnectionManager.getCollection(COLLECTION_NAME).find(query);
		cursor.limit(1);
		DBObject tweetObject = null;
		if (cursor.hasNext()) {
			tweetObject = cursor.next();
			LOGGER.debug("Will look for a match for :: " + tweetObject.get("twitterHandle"));
		}else {
			LOGGER.debug("No unprocessed requests found");
		}
		
		return tweetObject;
	}

	private void insertTweet(DBObject tweetObj) throws UnknownHostException {
		LOGGER.debug("Inserting tweet into DB");
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
