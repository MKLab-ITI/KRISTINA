package gr.iti.kristina.test.usecase;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import gr.iti.kristina.test.Base;

public class Test11 extends Base {

	@Override
	protected String getFileName() {
		return "dm2ki-output_11.ttl";
	}
	
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Test11 t = new Test11();
		t.call();
	}

}
