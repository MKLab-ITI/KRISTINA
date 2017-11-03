package gr.iti.mklab.kindex.ConceptExtraction.DBpediaSpotlight;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by spyridons on 5/3/2017.
 */
public class DBpediaSpotlightMain {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        ArrayList<String> selectTypes = new ArrayList<>(Arrays.asList());
        DBpediaSpotlightHandler dbpedia = new DBpediaSpotlightHandler();
        String text = "First documented in the 13th century, Berlin was the capital of the Kingdom of Prussia " +
                "(1701–1918), the German Empire (1871–1918), the Weimar Republic (1919–33) and the Third Reich (1933–45)." +
                " Berlin in the 1920s was the third largest municipality in the world. After World War II," +
                " the city became divided into East Berlin -- the capital of East Germany -- and West Berlin, a West" +
                " German exclave surrounded by the Berlin Wall from 1961–89. Following German reunification in 1990," +
                " the city regained its status as the capital of Germany, hosting 147 foreign embassies.";
        System.out.println(dbpedia.getAnnotations(text,"en","0.5"));
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Elapsed time: " + elapsed);
    }
}
