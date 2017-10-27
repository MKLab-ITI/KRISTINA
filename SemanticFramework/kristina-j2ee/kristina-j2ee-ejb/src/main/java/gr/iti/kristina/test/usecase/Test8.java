package gr.iti.kristina.test.usecase;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import gr.iti.kristina.test.Base;

public class Test8 extends Base {

	@Override
	protected String getFileName() {
		return "dm2ki-output_8.ttl";
	}
	
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Test8 t = new Test8();
		t.call();
	}

}
