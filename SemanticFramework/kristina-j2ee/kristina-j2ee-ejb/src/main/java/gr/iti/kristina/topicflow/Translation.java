package gr.iti.kristina.topicflow;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Translation {
	public String word;
	public String es, tr, de;

	static String get(Set<String> _words, String lng) {
		String result = "";
		Gson gson = new Gson();
		ClassLoader classLoader = Translation.class.getClassLoader();
		Class<?> c = Translation.class;
		Field lang;
		try {
			lang = c.getDeclaredField(lng);

			Translation[] data = gson.fromJson(FileUtils.readFileToString(
					new File(classLoader.getResource("translations.json").getFile())), Translation[].class);
			for (String _w : _words) {

				for (Translation translation : data) {
					if (_w.contains(translation.word)) {
						result += " " + (String) lang.get(translation);
					}
				}
			}
			return result.trim();

		} catch (JsonSyntaxException | IOException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		System.err.println(Translation.get(Sets.newHashSet("pregnancy"), "de"));
	}
}
