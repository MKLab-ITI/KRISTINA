package gr.iti.mklab.kindex.Babelfy;

/**
 * Created by Thodoris Tsompanidis on 21/3/2016.
 */
public class BabelfyResponse {

	String DBPediaURL;
	String BabelNetURL;

	public BabelfyResponse() {}

	public BabelfyResponse(String DBPediaURL, String babelNetURL) {
		this.DBPediaURL = DBPediaURL;
		BabelNetURL = babelNetURL;
	}

	public String getDBPediaURL() {
		return DBPediaURL;
	}

	public void setDBPediaURL(String DBPediaURL) {
		this.DBPediaURL = DBPediaURL;
	}

	public String getBabelNetURL() {
		return BabelNetURL;
	}

	public void setBabelNetURL(String babelNetURL) {
		BabelNetURL = babelNetURL;
	}
}
