package gr.iti.mklab.kindex.Indexing.ContentManagers.german;

import gr.iti.mklab.kindex.Indexing.ContentManagers.ContentPDFManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created by spyridons on 4/3/2017.
 */
public class ContentPDFManagerDE extends ContentPDFManager {

    public ContentPDFManagerDE(String url, String html) {
        super(url, html);
    }

    public void parseHtmlFromPdf(){
        this.paragraphs = new ArrayList<>();
        boolean appendNext = false;
        Document document = Jsoup.parse(this.html);
        Elements elements = document.select("p");
        StringBuilder textBuilder = new StringBuilder();
        StringBuilder paragraphBuilder = new StringBuilder();
        for (int i = 0; i < elements.size() ; i++) {
            Element element = elements.get(i);
            String text = element.text();
            // remove control characters
            text = text.replaceAll("[\\p{Cntrl}&&[^\n\t\r]]", "").trim();
            //"..................." is a string matched in the table of contents, which we do not want to add in the index
            if (text.length()>1 && !(text.contains("..................."))){

                if(paragraphBuilder.length() != 0 && changeParagraph(text)){
                    this.paragraphs.add(paragraphBuilder.toString());
                    paragraphBuilder = new StringBuilder();
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

    private boolean changeParagraph(String text){
        boolean changeParagraph = false;
        // if first character is a lower case (except for the pattern 'q' followed by ' ') or '•', do NOT change paragraphs
        if(!(Character.isLowerCase(text.charAt(0))) && !(text.charAt(0) == '•'))
            changeParagraph = true;
        if(text.startsWith("q "))
            changeParagraph = true;
        return changeParagraph;
    }
}
