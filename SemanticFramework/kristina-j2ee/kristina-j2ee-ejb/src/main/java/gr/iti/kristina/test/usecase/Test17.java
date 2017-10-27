package gr.iti.kristina.test.usecase;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import gr.iti.kristina.test.Base;

public class Test17 extends Base {

	@Override
	protected String getFileName() {
		return "dm2ki-output_17.ttl";
	}
	
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Test17 t = new Test17();
		t.call();
	}

}
