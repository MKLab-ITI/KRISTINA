package LexParser;

import java.util.Arrays;
import java.util.List;

/**
 * Created by spyridons on 8/26/2016.
 */
public class LexParserConstants {
    public static List<String> verbs = Arrays.asList("VBN","VBD","VBP","VBG","VB","VBZ","MD");
    public static List<String> subjects = Arrays.asList("subj","nsubj","nsubjpass","csubj","csubjpass");
    public static int wordsBeforeVerb = 2;
}
