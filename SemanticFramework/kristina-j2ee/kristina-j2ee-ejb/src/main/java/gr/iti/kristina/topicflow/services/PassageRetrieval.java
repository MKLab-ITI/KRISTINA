package gr.iti.kristina.topicflow.services;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class PassageRetrieval {

	private static final Logger LOG = LoggerFactory.getLogger(PassageRetrieval.class);

	String getIntro(String lang, String domain) {
		switch (lang) {
		case "de":
			return "";
		case "tr":
			return "";
		case "pl":
			return "";
		case "es":
			return "";
		default:
			return "";
		}
	}

	public IRResponse start(String userInput, String lang) throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		URI uri = new URIBuilder().setScheme("http").setHost("160.40.51.32").setPort(9000).setPath("/getpassage")
				.setParameter("query", userInput).setParameter("language", lang.toLowerCase()).build();
		HttpGet httpget = new HttpGet(uri);
		LOG.debug(httpget.toString());
		CloseableHttpResponse response = httpclient.execute(httpget);

		try {
			LOG.debug(response.getStatusLine().toString());
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new Exception("IR service is not reachable: " + response.getStatusLine().getStatusCode());
			}

			HttpEntity entity1 = response.getEntity();
			String responseJson = EntityUtils.toString(entity1);
			LOG.debug("responseJson" + responseJson);
			// JSONObject o = new JSONObject(responseJson);
			// EntityUtils.consume(entity1);
			// String string = o.getString("content");

			if (responseJson != null && !responseJson.isEmpty()) {
				JSONObject obj = null;
				try {
					obj = new JSONObject(responseJson);
				} catch (org.json.JSONException ex) {
					if (response != null)
						response.close();
					return new IRResponse("", "",
							java.net.URLEncoder.encode(responseJson, "UTF-8"));
				}
				String url = obj.getString("url");
				String domain = obj.getString("domain");
				String content = obj.getString("content");
				if (!Strings.isNullOrEmpty(content))
					return new IRResponse(domain, url,
							java.net.URLEncoder.encode(getIntro(lang, domain) + content, "UTF-8"));
				else
					return null;
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

	// public static void main(String[] args) throws Exception {
	// PassageRetrievalBean PassageRetrievalBean = new PassageRetrievalBean();
	// System.out.println(PassageRetrievalBean
	// .start("Fabian Bähr freut sich auf den 2. Horber Neckar-Balloncup-Start
	// am Mittwoch", "de"));
	// }

}
