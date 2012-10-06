package com.anuragkapur.hth.twitterclient;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

/**
 * @author anurag.kapur
 * 
 */
public class TweetRideMatches {

	private static final String PROTECTED_RESOURCE_URL = "https://api.twitter.com/1/statuses/update.json";

	public static void postMatch(String tweet) {

		OAuthService service = new ServiceBuilder().provider(TwitterApi.class)
				.apiKey("SWWEJXrHKryBK5cjqGsQ")
				.apiSecret("NMiJ0tPZ8BZs0Lw22T0vHLOoOvu9FfYNq7iGIFMw3E")
				.build();

		Token accessToken = new Token(
				"863690246-awS7kLgs47XSWHB5WY4tTpXATONX3AKxI6cZsaHZ",
				"KE5DhnfG55eRK1RbtLEFt8zVt0RL1u9c9XvSBkkSPU");

		OAuthRequest request = new OAuthRequest(Verb.POST, PROTECTED_RESOURCE_URL);
	    request.addBodyParameter("status", tweet);
	    service.signRequest(accessToken, request);
	    Response response = request.send();
	    System.out.println(response.getBody());
	}
	
	public static void main (String args[]) {
		postMatch("This is sparta! And we will give you a ride");
	}
}
