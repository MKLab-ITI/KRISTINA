package gr.iti.mklab.sm.input;


import java.util.*;

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import gr.iti.mklab.sm.Configuration;
import gr.iti.mklab.sm.feeds.Feed;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.QueryResults;

import com.mongodb.MongoClient;


/**
 * @brief The class responsible for the creation of input feeds from mongodb storage
 * 
 * @author manosetro
 * @email  manosetro@iti.gr
 */
public class FeedsCreator {
	
	protected static final String SINCE = "since";
	
	protected static final String HOST = "host";
	protected static final String DB = "database";
	protected static final String USERNAME = "username";
	protected static final String PASSWORD = "password";
	protected static final String AUTH_METHOD = "authenticationMethod";
	
	Morphia morphia = new Morphia();
	
	private String host = null;
	private String db = null;
	private String username = null;
	private String password = null;
	private String authenticationMethod = null;
	
	private BasicDAO<Feed, String> feedsDao;
	
	public FeedsCreator(Configuration config) throws Exception {
		morphia.map(Feed.class);
		
		this.host = config.getParameter(HOST);
		this.db = config.getParameter(DB);
		this.username = config.getParameter(USERNAME);
		this.password = config.getParameter(PASSWORD);
		this.authenticationMethod = config.getParameter(AUTH_METHOD);

		MongoClient mongoClient = null;

		if(authenticationMethod.equals("SCRAM-SHA-1"))
		{
			MongoCredential credential = MongoCredential.createScramSha1Credential(username, db, password.toCharArray());
			String port = "27017";
			mongoClient = new MongoClient(new ServerAddress(host + ":" + port), Arrays.asList(credential));
		}
		else
		{
			mongoClient = new MongoClient(host);
		}
		feedsDao = new BasicDAO<Feed, String>(Feed.class, mongoClient, morphia, db);
	}
	
	public Map<String, Set<Feed>> createFeedsPerSource() {
	
		Map<String, Set<Feed>> feedsPerSource = new HashMap<String, Set<Feed>>();
		
		Set<Feed> allFeeds = createFeeds();
		for(Feed feed : allFeeds) {
			String source = feed.getSource();
			Set<Feed> feeds = feedsPerSource.get(source);
			if(feeds == null) {
				feeds = new HashSet<Feed>();
				feedsPerSource.put(source, feeds);
			}	
			feeds.add(feed);
		}
		
		return feedsPerSource;
	}

	public Set<Feed> createFeeds() {
		QueryResults<Feed> result = feedsDao.find();
		List<Feed> feeds = result.asList();
		
		return new HashSet<Feed>(feeds);
	}
	
	public static void main(String...args) {
		
	}
	
}