package gr.iti.kristina.test.usecase;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import gr.iti.kristina.test.Base;

public class Test4 extends Base {

	@Override
	protected String getFileName() {
		return "dm2ki-output_4.ttl";
	}
	
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Test4 t = new Test4();
		t.call();
	}

}
