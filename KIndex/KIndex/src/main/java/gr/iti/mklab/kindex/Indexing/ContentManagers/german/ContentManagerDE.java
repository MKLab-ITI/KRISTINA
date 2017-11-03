package gr.iti.mklab.kindex.Indexing.ContentManagers.german;

import gr.iti.mklab.kindex.Indexing.ContentManagers.ContentManager;
import gr.iti.mklab.kindex.Indexing.ContentManagers.ContentManagerConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class that handles the html content and defines the paragraphs for german resources
 * Created by spyridons on 3/30/2017.
 */
public class ContentManagerDE extends ContentManager {

    private List<String> listTags;

    public ContentManagerDE(String url, String html){
        super(url, html);
        initializeUrlSpecificVariables();
        this.title = "";
    }

    private void initializeUrlSpecificVariables(){
        listTags = new ArrayList<>();
        listTags.add(ContentManager.defaultListTag);

        // for this domain, more tags are considered as list ones, that meaning paragraphs do not change
        if(this.url.contains("alzheimer-bw")){
            listTags.add("h4");
            listTags.add("h5");
            listTags.add("h6");
            html = html.replaceAll("<LI><H4>","<H4>").replaceAll("</H4></LI>","</H4>");
        }
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

        String contentSelector = findContentSelector();
        if(contentSelector.equals(""))
        {
            this.text = "";
            return;
        }
        Elements contentElements = elements.select(contentSelector);
        for (int i = 0; i < contentElements.size() ; i++) {
            Elements validElements = contentElements.get(i).select(String.join(",", ContentManager.defaultElements));
            for (int j = 0; j < validElements.size(); j++) {
                Element element = validElements.get(j);
                // if element is not child of another valid element
                if(isValidElement(element)){
                    String tag = element.tagName().toLowerCase();
                    String elementText = element.text();
                    // if title element
                    if(ContentManager.defaultHeaderTags.contains(tag)){

                        // get next valid element
                        int nextIndex = j + 1;
                        Element next = null;
                        if(nextIndex < validElements.size())
                            next = validElements.get(nextIndex);
                        while(next!=null && !isValidElement(next)){
                            nextIndex++;
                            if(nextIndex < validElements.size())
                                next = validElements.get(nextIndex);
                            else
                                next = null;
                        }

                        // check next element, if p or li, add it
                        if(next!=null){
                            String nextElementTag = next.tagName();
                            if(nextElementTag.equals(ContentManager.defaultParagraphTag)
                                    || this.listTags.contains(nextElementTag)){

                                if(elementText.trim().replaceAll("\\u00A0", "").length()!=0){
                                    if(!elementText.trim().matches(".*\\p{Punct}"))
                                        elementText = elementText + ".";
                                    // add tags to recognize titles
                                    elementText = "{s_o_t} " + elementText + " {e_o_t}";
                                }

                                textBuilder.append(elementText + "\n");
                                paragraphBuilder.append(elementText + "\n");
                            }
                        }

                    }
                    else if (tag.equals(ContentManager.defaultParagraphTag) || this.listTags.contains(tag)){

                        // .replaceAll("\\u00A0", "") is used because this type of space is not trimmed
                        if(!elementText.trim().matches(".*\\p{Punct}") && elementText.trim().replaceAll("\\u00A0", "").length()!=0)
                            elementText = elementText + ".";

                        textBuilder.append(elementText + "\n");
                        paragraphBuilder.append(elementText + "\n");

                        // get next valid element
                        int nextIndex = j + 1;
                        Element next = null;
                        if(nextIndex < validElements.size())
                            next = validElements.get(nextIndex);
                        while(next!=null && !isValidElement(next)){
                            nextIndex++;
                            if(nextIndex < validElements.size())
                                next = validElements.get(nextIndex);
                            else
                                next = null;
                        }

                        // check next element, if not li, change paragraph
                        if(next!=null){
                            String nextElementTag = next.tagName();
                            if(!this.listTags.contains(nextElementTag)){
                                this.paragraphs.add(paragraphBuilder.toString());
                                paragraphBuilder = new StringBuilder();
                            }
                        }
                    }
                }
            }
        }

        // for the end of the html
        if (paragraphBuilder.length() != 0)
            this.paragraphs.add(paragraphBuilder.toString());
        this.text = textBuilder.toString();
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

    private String findContentSelector(){
        String domain = getHostName();
        if(ContentManagerConstants.contentElementMapDE.containsKey(domain))
            return ContentManagerConstants.contentElementMapDE.get(domain);
        else{
            System.out.println("did not find content element");
            return "";
        }
    }

    public String getHostName() {
        URI uri = null;
        try {
            uri = new URI(this.url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String hostname = uri.getHost();
        // to provide faultproof result, check if not null then return only hostname, without www.
        if (hostname != null) {
            return hostname.startsWith("www.") ? hostname.substring(4) : hostname;
        }
        return hostname;
    }

    /**
     * Checks the rule that an element should not be a child of a set of specified elements
     * @param element
     * @return
     */
    public boolean isValidElement(Element element){

        if(element.text().trim().replaceAll("\\u00A0", "").length() == 0)
            return false;

        Elements parents = element.parents();
        for (Element parent: parents){
            String parentTag = parent.tagName();
            if(ContentManager.defaultElements.contains(parentTag))
                return false;
        }
        return true;
    }



}
