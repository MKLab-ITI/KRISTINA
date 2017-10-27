package gr.iti.kristina.topicflow;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import gr.iti.kristina.errors.UsernameException;
import gr.iti.kristina.model.Namespaces;

public class Console {

	private static final Logger LOG = LoggerFactory.getLogger(Console.class);

	public static String name, scenario, lang;

	static boolean LG = false;

	public static Set<String> setUp(String scenario) {
		if (scenario.equals("uc1_na_he")) {
			Console.name = "claudia";
			Console.scenario = "eat_sleep";
			Console.lang = "de";
		} else if (scenario.equals("uc2_he")) {
			Console.name = "carmen";
			Console.scenario = "uc2_he";
			Console.lang = "es";
		} else if (scenario.equals("uc1_sc")) {
			Console.name = "carmen";
			Console.scenario = "uc1_sc";
			Console.lang = "de";
		}
		// Console.scenario = scenario;
		return Sets.newHashSet(Arrays.asList(scenario));
	}

	public static void main(String[] args) {

		Set<String> themeModels = setUp("uc2_he");

		Workflow w = new Workflow(themeModels, false, LG);
		Vocabulary v = new Vocabulary();
		Scanner reader = new Scanner(System.in);
		while (true) {
			System.out.println("\n\n> Enter ontos: ");
			String line = reader.nextLine();

			Set<String> ontos = Arrays.asList(line.split(",")).stream()
					.map(x -> Namespaces.LA_ONTO + x.trim()).collect(Collectors.toSet());
			if (ontos.stream().anyMatch(x -> {
				x = x.toLowerCase();
				return x.endsWith("exit");
			})) {
				reader.close();
				break;
			}

			if (ontos.stream().anyMatch(x -> {
				x = x.toLowerCase();
				return x.contains("reload");
			})) {
				String command = ontos.stream().findFirst().get();
				Set<String> models = Arrays.stream(command.split(" "))
						.skip(1).collect(Collectors.toSet());
				// LOG.debug("models: " + models);
				if (models.isEmpty()) {
					models.addAll(themeModels);
				}
				w = new Workflow(models, false, LG);
				continue;
			}

			try {
				w.start(ontos, name, scenario, lang, "no user text");
			} catch (RepositoryException | RepositoryConfigException | UnsupportedEncodingException
					| NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | UsernameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				reader.nextLine();
				continue;
			}
			// System.err.println(Utils.modelToString(w.g.flashModel(),
			// "TURTLE"));
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Goodbye!");

	}

}
