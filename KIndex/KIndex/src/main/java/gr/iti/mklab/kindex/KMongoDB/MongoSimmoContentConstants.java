package gr.iti.mklab.kindex.KMongoDB;

/**
 * Class for storing LexParserConstants like Database Name, Collection Name etc.
 * LexParserConstants here are for Target Database, the DB KIndex.Scraper is storing output and KIndex.Indexer reads from.
 * Target DB is a Simmo Based Structure DB.
 * Find More for Simmo here: <a href="http://github.com/MKLab-ITI/simmo">http://github.com/MKLab-ITI/simmo</a>
 */
public final class MongoSimmoContentConstants {

	public static final String DB_NAME="SimmoContentDatabaseHtml";
	public static final String RECIPE_DB_NAME="SimmoRecipesHtml";
	public static final String NEWSPAPER_DB_NAME="SimmoNewspaperHtml";
	public static final String WEBPAGE_COLLECTIO_NAME="Webpage";
	public static final String PDF_COLLECTIO_NAME="PDF";
	public static final String POST_COLLECTIO_NAME="Post";
	public static final String TEXT_COLLECTION_NAME="Text";
	public static final String VIDEO_COLLECTION_NAME="Video";
	public static final String IMAGE_COLLECTION_NAME="Image";
	public static final String POST_COLLECTION_NAME="Post";
	public static final String URL_FIELD="url";
	public static final String POST_LABEL="label";


}
