package gr.iti.kristina.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.google.common.base.Strings;

/**
 * Session Bean implementation class PassageRetrievalBean
 */
@Stateless
@LocalBean
public class BabyEventBean {

	// public final String PASSAGE_URL =
	// "http://160.40.51.32:9000/getGenericEvent?town=<Madrid_or_Barcelona_or_
	// Tübingen> ";

	/*
	 * 
	 * German event retrieval: URL:
	 * http://160.40.51.32:9000/getBabyEvent?town=<Madrid_or_Barcelona> Response
	 * type: JSON
	 * 
	 */

	public BabyEventBean() {
		// TODO Auto-generated constructor stub
	}

	public String start(String location, String lang) throws Exception {

		if (org.apache.commons.lang3.StringUtils.endsWith(location, "id")) {
			location = "Madrid";
		} 
//		else if (org.apache.commons.lang3.StringUtils.endsWith(location, "en")) {
//			location = "Tübingen";
//		} 
		else if (org.apache.commons.lang3.StringUtils.endsWith(location, "na")) {
			location = "Barcelona";
		} else {
			location = "Madrid";
		}

		// if(lang!= null && lang.toLowerCase().equals("tr") &&
		// location.equals("Tübingen")) {
		// return "http://160.40.51.32:9000/getBabyEvent?town=" + location;
		// }
		
		//http://160.40.51.32:9000/getBabyEvent?town=Barcelona

		CloseableHttpClient httpclient = HttpClients.createDefault();
		URI uri = new URIBuilder().setScheme("http").setHost("160.40.51.32").setPort(9000).setPath("/getBabyEvent")
				.setParameter("town", location).build();
		HttpGet httpget = new HttpGet(uri);
		System.out.println(httpget);
		CloseableHttpResponse response = httpclient.execute(httpget);

		try {
			System.out.println(response.getStatusLine());
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new Exception(
						"Local Baby Event service is not reachable: " + response.getStatusLine().getStatusCode());
			}

			HttpEntity entity1 = response.getEntity();
			String responseJson = EntityUtils.toString(entity1);
			System.err.println("responseJson: " + responseJson);
			// JSONObject o = new JSONObject(responseJson);
			// EntityUtils.consume(entity1);
			// String string = o.getString("content");

			if (responseJson != null && !responseJson.isEmpty()
					&& !org.apache.commons.lang3.StringUtils.containsIgnoreCase(responseJson, "no results"))
				// return java.net.URLEncoder.encode(responseJson, "UTF-8");
				return responseJson;
			else {
				return null;
			}
		} catch (Exception e) {
			throw e;
		}

		finally {
			if (response != null)
				response.close();
		}
	}

	public static void main(String[] args) throws Exception {
		BabyEventBean PassageRetrievalBean = new BabyEventBean();
		System.out.println(PassageRetrievalBean.start("Madrid", "es"));
	}

}
