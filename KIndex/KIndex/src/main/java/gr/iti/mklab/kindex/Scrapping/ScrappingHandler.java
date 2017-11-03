package gr.iti.mklab.kindex.Scrapping;

import java.net.MalformedURLException;
import java.net.URL;


public class ScrappingHandler {


	/**
	 * Returns String with the type, provided url corresponds to
	 * @param url
	 * @return one of {@value ScrappingConstants#FORUM}/{@value ScrappingConstants#WEBSITE}/{@value ScrappingConstants#TWEET}/
	 */
	public static String whatTypeIsIt(URL url){

		if (url.toString().endsWith("pdf")){
			return ScrappingConstants.PDF;
		}
		String host = url.getHost();
		host = host.replace("www.","");

		if (ScrappingConstants.FORA.get(host) != null)
			return ScrappingConstants.FORUM;
		return ScrappingConstants.WEBSITE;
	}

	/**
	 * Returns String with the type, provided url corresponds to
	 * @param url_string
	 * @return one of {@value ScrappingConstants#FORUM}/{@value ScrappingConstants#WEBSITE}/{@value ScrappingConstants#TWEET}/
	 */
	public static String whatTypeIsIt(String url_string){
		try {
			URL new_url = new URL(url_string);
			String host = new_url.getHost();
			if (ScrappingConstants.FORA.get(host) != null)
				return ScrappingConstants.FORUM;
			return ScrappingConstants.WEBSITE;
		} catch (MalformedURLException e) {
			System.out.println("KIndex :: ScrappingConstants.whatTypeIsIt(String url_string) Could NOT form new url");
			System.out.println("url: " + url_string);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * returns true if url host is stored as forum and url is forum's thread url template
	 * @param url the url to check for
	 * @return boolean
	 */
	public static boolean isForumExtractable(URL url) {
		String host = url.getHost();
		host=host.replace("www.","");
		ForumDomain forum = ScrappingConstants.FORA.get(host);
		if (forum == null)
			return false;
		String url_string = url.toString();
		if (url_string.contains(forum.getUrl_post_template()))
			return true;
		return false;
	}

	/**
	 * returns the ForumDomain corresponding to url provided
	 * Provide complete url
	 * @param url
	 * @return
	 */
	public static ForumDomain getForum(URL url){
		String host = url.getHost();
		host=host.replace("www.","");
		return ScrappingConstants.FORA.get(host);
	}

	/**
	 * returns the ForumDomain corresponding to host provided
	 * host format "domain.com", without "www."
	 * @param host
	 * @return
	 */
	public static ForumDomain getForum(String host){
		return ScrappingConstants.FORA.get(host);
	}
}
