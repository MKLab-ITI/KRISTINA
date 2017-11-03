package gr.iti.mklab.kindex.Indexing.ContentManagers.spanish;

import gr.iti.mklab.kindex.Indexing.ContentManagers.ContentManager;
import gr.iti.mklab.kindex.Indexing.ContentManagers.ContentManagerConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that handles the html content and defines the paragraphs
 * Created by spyridons on 2/13/2017.
 */
public class ContentManagerES extends ContentManager {

    // parameters toggled depending on url
    private String paragraphTag;
    private boolean cutIfDate; // cut text if it contains a date
    private static final String DATE_REGEX =
            "(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/((19|20)\\d\\d)";

    public ContentManagerES(String url, String html){
        super(url, html);
        initializeUrlSpecificVariables();
        this.title="";
    }

    private void initializeUrlSpecificVariables(){
        if(this.url.contains("who.int")){
            this.paragraphTag = "span";
            this.defaultElements.set(this.defaultElements.indexOf("p"),"span");
        }
        else
            this.paragraphTag = ContentManager.defaultParagraphTag;
        if(this.url.contains("www.guiainfantil.com"))
            this.cutIfDate = true;
        else
            this.cutIfDate = false;
    }

    public void parseHtml(){
        // get html element and iterate through them
        this.paragraphs = new ArrayList<>();
        Document document = Jsoup.parse(this.html);
        Elements elements = document.getAllElements();

        // retrieve the document title and remove it from the html
        String titleSelector = findTitleSelector();
        Elements titleElements = elements.select(titleSelector);
        String prefix = "";
        for (Element titleElement: titleElements) {
            this.title += prefix + titleElement.text();
            prefix = "\n";
        }
        elements.select(titleSelector).remove();

        StringBuilder textBuilder = new StringBuilder();
        StringBuilder paragraphBuilder = new StringBuilder();
        boolean cutNextParagraph = false;
        for (int i = 0; i < elements.size() ; i++) {
            Element element = elements.get(i);
            String tag = element.tagName().toLowerCase();
            if(!this.defaultElements.contains(tag))
                continue;
            String text = element.text();
            if(!(element.ownText().equals(""))){
                // h1 to h6 handle, add it only if a <p> or <li> is next
                if(ContentManager.defaultHeaderTags.contains(tag)){

                    if (endOfWebpage(tag, text))
                        break;

                    String nextElementTag = ContentManager.getNextElementWithTextTag(elements, i);
                    if(nextElementTag.equals(this.paragraphTag) || nextElementTag.equals(ContentManager.defaultListTag)){

                        if(text.trim().replaceAll("\\u00A0", "").length()!=0){
                            if(!text.trim().matches(".*\\p{Punct}"))
                                text = text + ".";
                            // add tags to recognize titles
                            text = "{s_o_t} " + text + " {e_o_t}";
                        }

                        textBuilder.append(text + "\n");
                        paragraphBuilder.append(text + "\n");
                    }
                }
                else if(tag.equals(this.paragraphTag))
                {
                    if (this.cutIfDate){
                        if (cutNextParagraph){
                            cutNextParagraph = false;
                            continue;
                        }
                        Pattern p = Pattern.compile(this.DATE_REGEX);
                        Matcher m = p.matcher(text);
                        if (m.find()){
                            // cut this and the next paragraph
                            cutNextParagraph = true;
                        }
                        else{
                            textBuilder.append(text + "\n");
                            paragraphBuilder.append(text + "\n");

                            String nextElementTag = ContentManager.getNextElementWithTextTag(elements, i);
                            if(!(nextElementTag.equals(ContentManager.defaultListTag))){
                                // paragraph changes
                                this.paragraphs.add(paragraphBuilder.toString());
                                paragraphBuilder = new StringBuilder();
                            }
                        }
                    }
                    else{
                        textBuilder.append(text + "\n");
                        paragraphBuilder.append(text + "\n");

                        String nextElementTag = ContentManager.getNextElementWithTextTag(elements, i);
                        if(!(nextElementTag.equals(ContentManager.defaultListTag))){
                            // paragraph changes
                            this.paragraphs.add(paragraphBuilder.toString());
                            paragraphBuilder = new StringBuilder();
                        }
                    }
                }
                else if(tag.equals(ContentManager.defaultListTag)){

                    // .replaceAll("\\u00A0", "") is used because this type of space is not trimmed
                    if(!text.trim().matches(".*\\p{Punct}")
                            && text.trim().replaceAll("\\u00A0", "").length()!=0)
                        text = text + ".";

                    textBuilder.append(text + "\n");
                    paragraphBuilder.append(text + "\n");
                    String nextElementTag = ContentManager.getNextElementWithTextTag(elements, i);
                    if(!(nextElementTag.equals(ContentManager.defaultListTag))){
                        // paragraph changes
                        this.paragraphs.add(paragraphBuilder.toString());
                        paragraphBuilder = new StringBuilder();
                    }
                }
            }
        }

        // for the end of the html
        if (paragraphBuilder.length() != 0)
            this.paragraphs.add(paragraphBuilder.toString());
        this.text = textBuilder.toString();
    }

    /**
     * check for a specific case in which webpage ends (for medlineplus domain)
     * @return
     */
    private boolean endOfWebpage(String tag, String text){
        if (tag.equals("h2"))
            if(text.equals("Nombres alternativos") || text.equals("Referencias"))
                return true;

        return false;
    }

    private String findTitleSelector(){
        for (Map.Entry<String,String> entry: ContentManagerConstants.documentTitleMap.entrySet()) {
            String key = entry.getKey();
            if(this.url.contains(key)){
                String selector = entry.getValue();
                return selector;
            }
        }
        return "h1";
    }

}
