package gr.iti.kristina.test.usecase;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import gr.iti.kristina.test.Base;

public class Test20 extends Base {

	@Override
	protected String getFileName() {
		return "dm2ki-output_20.ttl";
	}
	
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Test20 t = new Test20();
		t.call();
	}

}
