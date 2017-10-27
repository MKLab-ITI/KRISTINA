package gr.iti.kristina.test.usecase;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import gr.iti.kristina.test.Base;

public class Test7 extends Base {

	@Override
	protected String getFileName() {
		return "dm2ki-output_7.ttl";
	}
	
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Test7 t = new Test7();
		t.call();
	}

}
