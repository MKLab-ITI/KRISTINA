package gr.iti.kristina.topicflow;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Url {

	String domain;
	String text;
	String acronym;

	static String get(String urlString) {
		Gson gson = new Gson();
		ClassLoader classLoader = Url.class.getClassLoader();
		try {
			Url[] data = gson.fromJson(FileUtils.readFileToString(
					new File(classLoader.getResource("urls.json").getFile())), Url[].class);
			Optional<String> exists = Arrays.asList(data).stream()
					.filter(url -> urlString.contains(url.domain)).map(url -> url.text).findFirst();
			if (exists.isPresent()) {
				return exists.get();
			} else {
				return null;
			}
		} catch (JsonSyntaxException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		System.err.println(Url.get("www.aeped.es"));
	}

}
