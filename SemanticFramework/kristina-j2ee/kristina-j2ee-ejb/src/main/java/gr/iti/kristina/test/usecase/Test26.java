package gr.iti.kristina.test.usecase;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import gr.iti.kristina.test.Base;

public class Test26 extends Base {

	@Override
	protected String getFileName() {
		return "dm2ki-output_26.ttl";
	}
	
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Test26 t = new Test26();
		t.call();
	}

}
