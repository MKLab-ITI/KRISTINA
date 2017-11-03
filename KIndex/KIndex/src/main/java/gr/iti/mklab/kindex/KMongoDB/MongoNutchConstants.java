package gr.iti.mklab.kindex.KMongoDB;

/**
 * Class for storing LexParserConstants like Database Name, Collection Name etc.
 * LexParserConstants here are for Source Database, the DB KIndex.Scraper is reading from.
 */
public final class MongoNutchConstants {

	public static final String DB_NAME="NutchDevelopmentDatabase";
	public static final String COLLECTION_NAME="crawlingUniqueUrls";
	public static final String CRAWLING_ID_FIELD="crawlingId";
}
