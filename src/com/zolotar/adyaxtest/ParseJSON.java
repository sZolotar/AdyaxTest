package com.zolotar.adyaxtest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses http link to the object to extract data
 * @author Zolotar Sergey
 *
 */
public class ParseJSON {
	
	private static InputStream is;
	private static String result;
	private static JSONObject jsonObj;
	
	
	/**
	 * Returns an object extracted from the url
	 */
	public JSONObject getJson(String url) {


		// Create an HTTP request
		try {

			
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(url);
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

		} catch (Exception e) {
			return null;
		}

		// transmits the result to a string
		try {

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "UTF-8"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result = sb.toString();

		} catch (Exception e) {
			return null;
			
		}

		// Converts a string to an object

		try {

			jsonObj = new JSONObject(result);

		} catch (JSONException je) {

			return null;
		}
		
		return jsonObj;

	}
}
