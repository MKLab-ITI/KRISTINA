package gr.iti.mklab.kindex.Indexing.ContentManagers.spanish;

import gr.iti.mklab.kindex.Indexing.ContentManagers.ContentPDFManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by spyridons on 2/15/2017.
 */
public class ContentPDFManagerES extends ContentPDFManager{

    // parameters toggled depending on url
    private boolean guiapracticaRules;

    public ContentPDFManagerES(String url, String html){
        super(url,html);
        initializeUrlSpecificVariables();
    }

    private void initializeUrlSpecificVariables(){
        if(this.url.contains("guiapractica.semfyc.info"))
            this.guiapracticaRules = true;
        else
            this.guiapracticaRules = false;
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
                if(this.guiapracticaRules){
                    // if start with a capital or a '¿' or a number that does not belong to a numbered list
                    if(text.charAt(0) =='¿' || (Character.isUpperCase(text.charAt(0)) && Character.isUpperCase(text.charAt(1)))){
                        if(paragraphBuilder.length() != 0){
                            this.paragraphs.add(paragraphBuilder.toString());
                            paragraphBuilder = new StringBuilder();
                        }
                    }

                    String bulletRegex = "(?<=(^| ))" // start of input or a space character (just lookbehind)
                            +  "O "; // followerd by an 'O' and a space character
                    text = text.replaceAll(bulletRegex,"");
                }
                else{
                    // if start with a capital or a '¿','¡','-','•' or a number that does not belong to a numbered list
                    if(Character.isUpperCase(text.charAt(0)) || text.charAt(0) =='¿' || text.charAt(0) == '¡'
                            || text.charAt(0) == '-' || text.charAt(0) == '•'
                            || (Character.isDigit(text.charAt(0)) && !isNumberedList(text))){
                        if(paragraphBuilder.length() != 0){
                            this.paragraphs.add(paragraphBuilder.toString());
                            paragraphBuilder = new StringBuilder();
                        }
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
