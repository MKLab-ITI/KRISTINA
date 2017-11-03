package gr.iti.mklab.kindex.Indexing.ContentManagers.polish;

import gr.iti.mklab.kindex.Indexing.ContentManagers.ContentPDFManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by spyridons on 2/15/2017.
 */
public class ContentPDFManagerPL extends ContentPDFManager{

    public ContentPDFManagerPL(String url, String html){
        super(url,html);
    }

    public void parseHtmlFromPdf(){
        this.paragraphs = new ArrayList<>();
        Document document = Jsoup.parse(this.html);
        Elements elements = document.select(ContentPDFManager.defaultParagraphTag);
        StringBuilder textBuilder = new StringBuilder();
        StringBuilder paragraphBuilder = new StringBuilder();
        for (int i = 0; i < elements.size() ; i++) {
            Element element = elements.get(i);
            String text = element.text();
            // remove control characters
            text = text.replaceAll("[\\p{Cntrl}&&[^\n\t\r]]", "");
            if (text.trim().length()>1){
                // if start with a capital or a '-' or a number that does not belong to a numbered list, change paragraph
                if(Character.isUpperCase(text.charAt(0)) || text.charAt(0) == '-'
//                            || text.charAt(0) == '•'
                        || (Character.isDigit(text.charAt(0)) && !isNumberedList(text))){
                    if(paragraphBuilder.length() != 0){
                        this.paragraphs.add(paragraphBuilder.toString());
                        paragraphBuilder = new StringBuilder();
                    }
                }
                textBuilder.append(text + "\n");
                paragraphBuilder.append(text + "\n");
            }
        }

        // for the end of the html
        if (paragraphBuilder.length() != 0)
            this.paragraphs.add(paragraphBuilder.toString());
        this.text = textBuilder.toString();
    }

    private boolean isNumberedList(String text){

        // numbered list regex
        String regex = "(?s)^" // start of input
                + "\\d+" // followed by a number
                + "\\. " // followed by a dot and a space character
                + ".*$"; // followed by any character and the end of the sequence
        Pattern pattern = Pattern.compile( regex );

        // Now create matcher object.
        Matcher m = pattern.matcher(text);
        if (m.find())
            return true;
        else
            return false;
    }

}
