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

/**
 * Session Bean implementation class NewspaperBean
 */
@Stateless
@LocalBean
public class NewspaperBean {

	// public final String NEWPAPER_URL = "http://160.40.51.32:9000/newspaper";

	// http://localhost:9000/newspaperTR?query=<input_query>

	public NewspaperBean() {
		// TODO Auto-generated constructor stub
	}

	public String start(String title, String language) throws ClientProtocolException, IOException, URISyntaxException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		if (language == null || language.isEmpty()) {
			language = "de";
		}

		URI uri = null;

		if (language.toLowerCase().equals("de")) {
			uri = new URIBuilder().setScheme("http").setHost("160.40.51.32").setPort(9000).setPath("/newspaper")
					.setParameter("title", title).setParameter("onlyFirstParagraph", "true").build();
			// System.out.println("Newspaper title:" +
			// java.net.URLEncoder.encode(title.toString(), "UTF-8"));
		} else {
			uri = new URIBuilder().setScheme("http").setHost("160.40.51.32").setPort(9000).setPath("/newspaperTR")
					.setParameter("query", title).setParameter("onlyFirstParagraph", "true").build();
		}
		HttpGet httpget = new HttpGet(uri);
		// System.out.println(java.net.URLEncoder.encode(httpget.getURI().toString(),
		// "UTF-8"));
		CloseableHttpResponse response = httpclient.execute(httpget);

		try {
			System.out.println(response.getStatusLine());
			HttpEntity entity1 = response.getEntity();
			String responseJson = EntityUtils.toString(entity1);
			// JSONObject o = new JSONObject(responseJson);
			// EntityUtils.consume(entity1);
			// return o.getString("content");
			// String newStringUtf8 =
			// StringUtils.newStringUtf8(responseJson.getBytes());
			// System.out.println(responseJson);
			return java.net.URLEncoder.encode(responseJson, "UTF-8");
		} finally {
			response.close();
		}
	}

	public static void main(String[] args) throws ClientProtocolException, IOException, URISyntaxException {
		NewspaperBean newspaperBean = new NewspaperBean();
		System.out.println(newspaperBean.start("Bessere Orientierung für Radfahrer an der Baustelle in Fischingen", null));
	}
}
