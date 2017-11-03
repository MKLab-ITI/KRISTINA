package gr.iti.mklab.kindex.Indexing.Recipes;

/**
 * Created by spyridons on 2/23/2017.
 */
public class RecipeDomain {
    private String domainUrl;
    private String titleTagSelector;
    private String contentTagSelector;

    public RecipeDomain(String domainUrl, String titleTagSelector, String contentTagSelector) {
        this.domainUrl = domainUrl;
        this.titleTagSelector = titleTagSelector;
        this.contentTagSelector = contentTagSelector;
    }

    public String getDomainUrl() {
        return domainUrl;
    }

    public void setDomainUrl(String domainUrl) {
        this.domainUrl = domainUrl;
    }

    public String getTitleTagSelector() {
        return titleTagSelector;
    }

    public void setTitleTagSelector(String titleTagSelector) {
        this.titleTagSelector = titleTagSelector;
    }

    public String getContentTagSelector() {
        return contentTagSelector;
    }

    public void setContentTagSelector(String contentTagSelector) {
        this.contentTagSelector = contentTagSelector;
    }
}
