package gr.iti.mklab.kindex.Indexing.Newspapers;

import gr.iti.mklab.kindex.Indexing.Recipes.RecipeDomain;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by spyridons on 2/24/2017.
 */
public class NewspaperContentManager {
    private String html;
    private String title;
    private String subtitle;
    private String content;

    public NewspaperContentManager(String html) {
        this.html = html;
        this.title = "";
        this.subtitle = "";
        this.content = "";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void parseHtml(){
        Document document = Jsoup.parse(this.html);
        Element titleElement = document.select("h1").first();
        if(titleElement!=null)
            this.title = titleElement.text().replaceAll("\u00A0", " ");

        Element subtitleElement = document.select("h2").first();
        if(subtitleElement!=null)
            this.subtitle = subtitleElement.text().replaceAll("\u00A0", " ");

        Elements contentElements = document.select("div.devami_haber");
        for (Element contentElement: contentElements) {

            // add missing line breaks and tabs
            contentElement.select("br").append("<pre>\n</pre>");
            contentElement.select("h1").prepend("<pre>\n\n</pre>");
            contentElement.select("h2").prepend("<pre>\n\n</pre>");
            contentElement.select("h3").prepend("<pre>\n\n</pre>");
            contentElement.select("h4").prepend("<pre>\n\n</pre>");
            contentElement.select("h5").prepend("<pre>\n\n</pre>");
            contentElement.select("h6").prepend("<pre>\n\n</pre>");
            contentElement.select("li").prepend("<pre>\n</pre>");
            contentElement.select("p").prepend("<pre>\n\n</pre>");
            contentElement.select("tr").prepend("<pre>\n</pre>");
            contentElement.select("td").prepend("<pre>\t</pre>");

            // clear unwanted non-breaking spaces and multiple line breaks (more than 2 consecutive)
            String textToAdd = contentElement.text().replaceAll("\u00A0", " ")
                    .replaceAll("\n\\s+\n", "\n\n").replaceAll("\n{3,}", "\n\n");

            this.content += textToAdd;
        }
    }
}
