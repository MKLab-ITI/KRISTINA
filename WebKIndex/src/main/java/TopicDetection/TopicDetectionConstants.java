package TopicDetection;

public class TopicDetectionConstants {
	
	//paths
	public static String exePath="C:/R/R-3.1.3/bin/x64/Rscript";
	public static String filesPath="topicDetectionFiles";

	public static String mongoUsername = "user";
	public static String mongoPassword = "pass";
	public static String host = "160.40.X.Y";
	public static int port = 27017;
	public static String authenticationUserMechamism = "SCRAM-SHA-1";

	public static final int NUM_DAYS = 2;

	public static final String TWITTER_BASE_URL = "https://api.twitter.com/1.1/statuses/oembed.json?hide_media=true&id=";
	public static final int NUM_TWEETS_PER_TOPIC = 2; //tweets to show in the HTML response
	
}
