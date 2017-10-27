package gr.iti.kristina.topicflow.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.LinkedHashMultimap;

import gr.iti.kristina.utils.Utils;

/**
 * Session Bean implementation class WeatherServiceBean
 */

public class WeatherServiceBean {

	/**
	 * Default constructor.
	 */
	public WeatherServiceBean() {
		// TODO Auto-generated constructor stub
	}

	public LinkedHashMultimap<String, String> start() throws ClientProtocolException, IOException, URISyntaxException {

		String start = DateTime.now().toString();
		String end = DateTime.now().plusHours(2).toString();

		CloseableHttpClient httpclient = HttpClients.createDefault();
		URI uri = new URIBuilder().setScheme("http").setHost("160.40.51.32").setPort(9000).setPath("/weather")
				.addParameter("placeCode", "foi_0206").addParameter("startDate", start).addParameter("endDate", end)
				.build();

		HttpGet httpget = new HttpGet(uri);
		System.out.println(httpget);
		CloseableHttpResponse response = httpclient.execute(httpget);
		LinkedHashMultimap<String, String> result = LinkedHashMultimap.create();
		try {
			System.out.println(response.getStatusLine());
			HttpEntity entity1 = response.getEntity();
			String responseJson = EntityUtils.toString(entity1);
			JSONObject o = new JSONObject(responseJson);
			EntityUtils.consume(entity1);
			JSONArray fields = o.getJSONArray("fields");

			// get only the first... maybe needs to be updated
			String valuesJson = (String) o.getJSONArray("values").get(0);
			String[] values = valuesJson.split(";");
			for (int i = 0; i < fields.length(); i++) {
				String field = ((String) fields.get(i)).split("_")[0];
				if (field.equals("duration") || field.equals("feature") || field.equals("Time"))
					continue;
				String value = values[i];
				if(field.equals("windSpeed")){
					value = String.format("%.0f", Double.parseDouble(value)/1000*3600); 
				}
				if(field.equals("temperature")){
					value = String.format("%.0f", Double.parseDouble(value)); 
				}
				if(field.equals("humidity")){
					value = String.format("%.0f", Double.parseDouble(value)); 
				}
				if(field.equals("pressure")){
					value = String.format("%.0f", Double.parseDouble(value)); 
				}
				result.put(field, value);
			}
			Utils.printMap(result);
			// createForecast(result);

			return result;
		} finally {
			response.close();
		}
	}

	// private void createForecast(LinkedHashMultimap<String, String> result) {
	//
	// Set<String> keys = result.keySet();
	// for (String k : keys) {
	// Set<String> set = result.get(k);
	// System.err.println("field: " + k.split("_")[0] + ", " + set);
	// }
	//
	// }

	public static void main(String[] args) throws ClientProtocolException, IOException, URISyntaxException {
		// DateTime now = DateTime.now();
		// String string = now.toString();
		// System.err.println(string);
		WeatherServiceBean b = new WeatherServiceBean();
		b.start();
	}

}
