package Functions;

import CoreNLP.CoreNLPHandler;
import WebKIndex.ServicesController;
import WebKIndex.WebKIndex;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;

/**
 * Created by spyridons on 10/19/2016.
 */
public class ParsingNewsArticleFunctions {

    /**
     * Add a space character after the first dot under specific circumstances
     * @param content
     * @return
     */
    public static String refineNewspaperArticleText(String content){
        String refinedContent=content;
        int firstDotIndex = content.indexOf(".");
        if(firstDotIndex!=-1 && firstDotIndex!=(content.length()-1) && content.charAt(firstDotIndex+1)!=' '){
            String responseUntilFirstDot = content.substring(0,firstDotIndex);
            if(isGeoname(responseUntilFirstDot)){
                refinedContent = refinedContent.replaceFirst("\\.",". ");
            }
        }
        return refinedContent;
    }

    private static boolean isGeoname(String contentUntilFirstDot){
        WebService.setUserName("user1"); // add your username here

        ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
        searchCriteria.setQ(contentUntilFirstDot);
        ToponymSearchResult searchResult = null;
        try {
            WebKIndex.LOGGER.info("Called GeoNames web service with query: " + contentUntilFirstDot);
            searchResult = WebService.search(searchCriteria);
        } catch (Exception e) {
            System.out.println("Error while querying Geonames service!!!");
            return false;
        }
        int numGeonames = searchResult.getTotalResultsCount();
        if(numGeonames == 0)
            return false;
        else
            return true;
    }

    public static String insertSentenceBreaks(String text, String language){
        StringBuilder resultBuilder = new StringBuilder();
        String[] paragraphs = text.split("\n\n");
        String prefix = "";
        for (String paragraph : paragraphs){
            resultBuilder.append(prefix);
            prefix = "\n\n";
            String[] sentences = CoreNLPHandler.splitSentences(paragraph, language);
            for (String sentence : sentences) {
                resultBuilder.append(sentence + " {EOS} ");
            }
        }
        return resultBuilder.toString();
    }

    public static String insertSentenceBreaksToParagraph(String paragraph, String language){
        StringBuilder resultBuilder = new StringBuilder();
        String[] sentences = CoreNLPHandler.splitSentences(paragraph, language);
        for (String sentence : sentences) {
            resultBuilder.append(sentence + " {EOS} ");
        }
        return resultBuilder.toString();
    }
}
