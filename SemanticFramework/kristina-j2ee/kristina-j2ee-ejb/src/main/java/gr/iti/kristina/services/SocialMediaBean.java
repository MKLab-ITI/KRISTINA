package gr.iti.kristina.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.print.attribute.HashAttributeSet;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Session Bean implementation class PassageRetrievalBean
 */
@Stateless
@LocalBean
public class SocialMediaBean {

	// public final String PASSAGE_URL =
	// "http://160.40.51.32:9000/topicDetection";
	//http://mklab-services.iti.gr/KRISTINA_topic_detection

	public SocialMediaBean() {
		// TODO Auto-generated constructor stub
	}

	public String start() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		URI uri = new URIBuilder().setScheme("http").setHost("160.40.51.32").setPort(9000).setPath("/topicDetection")
				.setParameter("days", "2").build();
		HttpGet httpget = new HttpGet(uri);
		System.out.println(httpget);
		CloseableHttpResponse response = httpclient.execute(httpget);

		try {
			System.out.println(response.getStatusLine());
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new Exception(
						"SocialMedia service is not reachable: " + response.getStatusLine().getStatusCode());
			}

			HttpEntity entity1 = response.getEntity();
			String responseJson = EntityUtils.toString(entity1);
			System.err.println("responseJson: " + responseJson);
			// JSONObject o = new JSONObject(responseJson);
			// EntityUtils.consume(entity1);
			// String string = o.getString("content");

			if (responseJson != null && !responseJson.isEmpty()
					&& !org.apache.commons.lang3.StringUtils.containsIgnoreCase(responseJson, "no results")) {
				HashSet<String> topics = new HashSet<String>();
				JSONObject o = new JSONObject(responseJson);
				EntityUtils.consume(entity1);
				Set<String> attributes = o.keySet();
				for (String att : attributes) {
					String labels = o.getJSONObject(att).getString("labels");
					if (!labels.equals("noise")) {
						topics.addAll(Arrays.asList(labels.split(" ")));
					}
				}

				//return java.net.URLEncoder.encode(String.join(", ", topics), "UTF-8");
				return String.join(", ", topics);
			} else {
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
		SocialMediaBean PassageRetrievalBean = new SocialMediaBean();
		System.out.println(PassageRetrievalBean.start());
	}

}
