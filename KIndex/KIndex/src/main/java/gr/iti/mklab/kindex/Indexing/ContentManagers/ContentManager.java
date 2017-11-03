package gr.iti.mklab.kindex.Indexing.ContentManagers;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.List;

/**
 * Abstract class used as base for inherited classes that handle the html content and define the paragraphs
 * Created by spyridons on 3/30/2017.
 */
public abstract class ContentManager {
    protected String url;
    protected String html;
    protected String text;
    protected String title;
    protected List<String> paragraphs;

    public ContentManager(String url, String html){
        this.url = url;
        this.html = html;
    }

    public String getHtml() {
        return html;
    }

    public String getText() {
        return text;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getParagraphs() {
        return paragraphs;
    }

    /**
     * a separate parser must be implemented per language
     */
    public abstract void parseHtml();


    // STATIC VARIABLES AND METHODS
    // ----------------------------
    protected static final List<String> defaultElements = Arrays.asList(new String[]{"h1","h2","h3","h4","h5","h6","p","li"});
    protected static final List<String> defaultHeaderTags = Arrays.asList(new String[]{"h1","h2","h3","h4","h5","h6"});
    protected static final String defaultParagraphTag = "p";
    protected static final String defaultListTag = "li";

    protected static String getNextElementWithTextTag(Elements elements, int index){
        Element element = elements.get(index);
        for (int i = index + 1; i < elements.size() ; i++) {
            Element next = elements.get(i);
            String nextText = next.ownText();
            if(!(nextText.equals(""))){
                // if its not a child element
                if(!(element.getElementsContainingText(nextText).contains(next)))
                    return next.tagName();
            }
        }
        return "";
    }
}
