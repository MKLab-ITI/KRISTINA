package KnowledgeBase;

/**
 * Created by spyridons on 10/12/2016.
 */
public class DocumentResult {

    private String pageID;
    private String textID;
    private String url;
    private String text;

    // can only store score from one query
    private double score;

    public String getPageID() {
        return pageID;
    }

    public void setPageID(String pageID) {
        this.pageID = pageID;
    }

    public String getTextID() {
        return textID;
    }

    public void setTextID(String textID) {
        this.textID = textID;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentResult that = (DocumentResult) o;

        return url != null ? url.equals(that.url) : that.url == null;
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }
}
