package gr.iti.mklab.kindex.Scrapping;


import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;

import java.net.URL;
import java.util.HashMap;

/**
 * A class to handle all scraping conctants.
 * There are specific types of content in KIndex to be stored
 */
public class ScrappingConstants{

	public static final String FORUM = "Forum";
	public static final String WEBSITE = "Website";
	public static final String TWEET= "Tweet";
	public static final String PDF= "pdf";
	public static final String[] AVAILABLE_TYPES= { FORUM, WEBSITE, TWEET};

	public static final String URL_LIST_FILE="input\\ScraperList.txt";

	public static final HashMap<String, ForumDomain> FORA = new HashMap<String, ForumDomain>(){{
		put("alzconnected.org", new ForumDomain("alzconnected.org", "alzconnected.org/discussion.aspx", ".ekHeader1Reply table tr td:first",".post .message",".innerquote"));
		put("forum.alzheimers.org.uk", new ForumDomain("forum.alzheimers.org.uk", "forum.alzheimers.org.uk/showthread.php", ".threadtitle",".postcontainer .postbody .postrow .content", ".bbcode_quote"));
		put("parkinsons.org.uk", new ForumDomain("parkinsons.org.uk", "parkinsons.org.uk/forum/thread", ".cufon.grid_3_w.sideNavSpacer",".forum-post .forum-post-content", "blockquote"));
		put("dementiacarecentral.com", new ForumDomain("dementiacarecentral.com", "dementiacarecentral.com/node", "#content-header .title","#comments .content", "there_are_no_quotes_in_this_forum"));
		put("caregiveraction.org", new ForumDomain("caregiveraction.org", "caregiveraction.org", ".page-header",".forum-post .forum-post-content","there_are_no_quotes_in_this_forum"));

	}};

}
