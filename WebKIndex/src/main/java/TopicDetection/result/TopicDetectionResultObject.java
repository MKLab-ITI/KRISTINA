package TopicDetection.result;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by spyridons on 4/10/2017.
 */
public class TopicDetectionResultObject {

    private String id;
    private String keywords;
    private List<String> topPostsIds;
    private List<String> topPostsHTML;

    public  TopicDetectionResultObject(){
        topPostsIds = new ArrayList<>();
        topPostsHTML = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public List<String> getTopPostsIds() {
        return topPostsIds;
    }

    public void setTopPostsIds(List<String> topPostsIds) {
        this.topPostsIds = topPostsIds;
    }

    public void addTopPost(String postId){
        this.topPostsIds.add(postId);
    }

    public List<String> getTopPostsHTML() {
        return topPostsHTML;
    }

    public void setTopPostsHTML(List<String> topPostsHTML) {
        this.topPostsHTML = topPostsHTML;
    }

    public void addTopPostHTML(String postHTML){
        this.topPostsHTML.add(postHTML);
    }
}
