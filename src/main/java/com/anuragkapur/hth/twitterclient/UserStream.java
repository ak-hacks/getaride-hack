package com.anuragkapur.hth.twitterclient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

/**
 * @author anurag.kapur
 */
public class UserStream {

	private static final String PROTECTED_RESOURCE_URL = "https://userstream.twitter.com/1.1/user.json";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OAuthService service = new ServiceBuilder().provider(TwitterApi.class)
				.apiKey("jQb2il02mimOvGiqlAGZQ")
				.apiSecret("kZcqPfLVgOubAjn1F4oWtRu4dvVgE6dXuzB4qcO4G74")
				.build();

		Token accessToken = new Token(
				"863690246-Hf0HoNcC9xhqwGAgl3ct9nsmSHqKA5gtOJmfsZc",
				"r06h0zetDvaVQtKo5AdHdHc3BXF1ZyAfEFy6pWraSg");
		
		OAuthRequest request = new OAuthRequest(Verb.GET,
				PROTECTED_RESOURCE_URL);
		service.signRequest(accessToken, request);
		
		Response response = request.send();
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(response.getStream(), "UTF-8"));
			while (true) {
				System.out.println(br.readLine());
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(response.getBody());

	}

}
