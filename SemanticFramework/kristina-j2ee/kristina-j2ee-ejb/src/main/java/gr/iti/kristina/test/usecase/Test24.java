package gr.iti.kristina.test.usecase;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import gr.iti.kristina.test.Base;

public class Test24 extends Base {

	@Override
	protected String getFileName() {
		return "dm2ki-output_24.ttl";
	}
	
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Test24 t = new Test24();
		t.call();
	}

}
