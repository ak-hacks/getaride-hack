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
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * @author anurag.kapur
 * 
 */
public class TweetsDAO {

	private static final Logger LOGGER = Logger.getLogger(TweetsDAO.class);

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
			DBObject entities = (DBObject)tweetObj.get("entities");
			LOGGER.debug("Tweet Hashtags :: " + entities.get("hashtags"));
			List hashTags = (List)entities.get("hashtags");
			if(hashTags.size() < 3) {
				// Not a valid get a ride request
				return;
			}
			
			Iterator iterator = hashTags.iterator();
			int hashtagCounter = 0;
			while(iterator.hasNext()) {
				hashtagCounter ++;
				DBObject hashtag = (DBObject)iterator.next();
				if (hashtagCounter == 1) {
					tweetToInsert.put("from", hashtag.get("text"));
				}else if (hashtagCounter == 2) {
					tweetToInsert.put("to", hashtag.get("text"));
				}else if (hashtagCounter == 3) {
					tweetToInsert.put("time", hashtag.get("text").toString().substring(1));
				}
			}
			
			// Process user info
			DBObject userData = (DBObject)tweetObj.get("user");
			tweetToInsert.put("twitterHandle",userData.get("screen_name"));
			
			insertTweet(tweetToInsert );
		}
	}
	
	private String getTweetType(DBObject tweetObj) {
		Map tweetMap = tweetObj.toMap();
		Set tweetMapKeys = tweetMap.keySet();
		Iterator keysIterator = tweetMapKeys.iterator();
		String key = null;
		
		if(keysIterator.hasNext()) {
			key = (String)keysIterator.next();
		}
		
		return key;
	}
	
	private void insertTweet(DBObject tweetObj) throws UnknownHostException {
		LOGGER.debug("Inserting tweet into DB");
		DBConnectionManager.getCollection("tweets").insert(tweetObj);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TweetsDAO tDao = new TweetsDAO();
	}

}
