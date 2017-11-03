package gr.iti.mklab.kindex.Indexing.Recipes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by spyridons on 2/24/2017.
 */
public class RecipeContentManager {
    private RecipeDomain domain;
    private String html;
    private String title;
    private String content;

    public RecipeContentManager(RecipeDomain domain, String html) {
        this.domain = domain;
        this.html = html;
        this.title = "";
        this.content = "";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void parseHtml(){
        Document document = Jsoup.parse(this.html);
        Elements titleElements = document.select(this.domain.getTitleTagSelector());
        String prefix = "";
        for (Element titleElement: titleElements) {
            this.title += prefix + titleElement.text().replaceAll("\u00A0", " ");
            prefix = "\n";
        }
        Elements contentElements = document.select(this.domain.getContentTagSelector());
        prefix = "";
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

            this.content += prefix + textToAdd;
            prefix = "\n";
        }
    }
}
