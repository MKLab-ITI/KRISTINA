package Indexing.Queries;

import Functions.FileFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by spyridons on 11/9/2017.
 */
public class QueryTopicMapper {

    private static final String baseTopicFilesPath = "input/topics/";
    private Map<String,ArrayList<String>> topicMap;

    public QueryTopicMapper() {
        this.topicMap = new LinkedHashMap<>();
    }

    public void formTopicMap(String language){
        String topicFileName = "topics_" + language + ".txt";
        String topicFileContents = FileFunctions.readInputFile(baseTopicFilesPath + topicFileName);
        String[] topicLines = topicFileContents.split("\r\n\r\n");
        for(String topicLine: topicLines){
            ArrayList<String> topicQuestionsList = new ArrayList<>();
            String[] questions = topicLine.split("\r\n");
            String topicBaseQuestion = questions[0].trim();
            for (int i = 1; i < questions.length; i++) {
                topicQuestionsList.add(questions[i].trim());
            }
            this.topicMap.put(topicBaseQuestion, topicQuestionsList);
        }
    }

    public Map<String, ArrayList<String>> getTopicMap() {
        return topicMap;
    }
}
