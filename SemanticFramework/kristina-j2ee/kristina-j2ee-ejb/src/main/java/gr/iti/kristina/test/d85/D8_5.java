package gr.iti.kristina.test.d85;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import gr.iti.kristina.test.Base;

public class D8_5 extends Base {
	static String path = "C:/Users/gmeditsk/Google Drive/KRISTINA_prototype1/sleep/D8.5_evaluation/la_with_multiple_responses/";

	@Override
	protected String getFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) throws ClientProtocolException, IOException {

		File f = new File(path);
		File[] listFiles = f.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.getName().endsWith(".owl");
			}
		});

		for (File file : listFiles) {
			System.out.println(file.getName());
			
			D8_5 d = new D8_5();
			d.call(path, file.getName());
		}

	}

}
