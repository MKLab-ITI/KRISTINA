package gr.iti.kristina.test.usecase;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import gr.iti.kristina.test.Base;

public class Test18 extends Base {

	@Override
	protected String getFileName() {
		return "dm2ki-output_18.ttl";
	}
	
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Test18 t = new Test18();
		t.call();
	}

}
