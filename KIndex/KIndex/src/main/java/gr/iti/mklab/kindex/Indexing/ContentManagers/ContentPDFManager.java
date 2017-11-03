package gr.iti.mklab.kindex.Indexing.ContentManagers;

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
public abstract class ContentPDFManager {
    protected String url;
    protected String html;
    protected String text;
    protected List<String> paragraphs;
    protected static final String defaultParagraphTag = "p";

    public ContentPDFManager(String url, String html){
        this.url = url;
        this.html = html;
    }

    public String getHtml() {
        return html;
    }

    public String getText() {
        return text;
    }

    public List<String> getParagraphs() {
        return paragraphs;
    }

    public abstract void parseHtmlFromPdf();
}
