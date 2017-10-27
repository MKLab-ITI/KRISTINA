package gr.iti.kristina.test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public abstract class Base {

	protected static String path = "C:/Users/gmeditsk/Google Drive/KRISTINA_prototype1/sleep/Dm2KI/";
	final String url = "http://localhost:8080/kristina-j2ee-web/api/context/update";

	protected String filename;

	protected String getFileName(){
		return this.filename;
	};

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
	protected void setFileName(String filename){
		this.filename = filename;
	}

	public void call() throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("frames", readFile(path + getFileName(), Charset.forName("utf-8"))));
		nvps.add(new BasicNameValuePair("scenario", "sleep"));
		nvps.add(new BasicNameValuePair("mode", "batch"));
		nvps.add(new BasicNameValuePair("file", getFileName()));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response2 = httpclient.execute(httpPost);

		try {
			System.out.println(response2.getStatusLine());
			HttpEntity entity2 = response2.getEntity();
//			EntityUtils.consume(entity2);
			String response = EntityUtils.toString(entity2);
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			System.out.println(response);
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		} finally {
			response2.close();
		}

	}
	
	public void call(String filePath, String fileName) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("frames", readFile(filePath + fileName, Charset.forName("utf-8"))));
		nvps.add(new BasicNameValuePair("scenario", "sleep"));
		nvps.add(new BasicNameValuePair("mode", "batch"));
		nvps.add(new BasicNameValuePair("file", fileName + ".log"));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response2 = httpclient.execute(httpPost);

		try {
			System.out.println(response2.getStatusLine());
			HttpEntity entity2 = response2.getEntity();
//			EntityUtils.consume(entity2);
			String response = EntityUtils.toString(entity2);
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			System.out.println(response);
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		} finally {
			response2.close();
		}

	}

}
