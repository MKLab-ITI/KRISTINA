package gr.iti.mklab.kindex.Scrapping;

/**
 * This class is to keep domain's structure that is needed for forum posts extraction
 *
 * Created by Thodoris Tsompanidis on 19/1/2016.
 */
public class ForumDomain {

	public String url; //dimains url
	//in fora, pages that contain topic and replies, have a url template
	public String url_post_template;
	public String title_element;
	public String reply_element;
	public String quote_element;

	public ForumDomain(String url, String url_post_template, String title_element, String reply_element, String quote_element) {
		this.url = url;
		this.url_post_template = url_post_template;
		this.title_element = title_element;
		this.reply_element = reply_element;
		this.quote_element = quote_element;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl_post_template() {
		return url_post_template;
	}

	public void setUrl_post_template(String url_post_template) {
		this.url_post_template = url_post_template;
	}

	public String getTitle_element() {
		return title_element;
	}

	public void setTitle_element(String title_element) {
		this.title_element = title_element;
	}

	public String getReply_element() {
		return reply_element;
	}

	public void setReply_element(String reply_element) {
		this.reply_element = reply_element;
	}

	public String getQuote_element() {
		return quote_element;
	}

	public void setQuote_element(String quote_element) {
		this.quote_element = quote_element;
	}
}
