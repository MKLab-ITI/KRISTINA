package gr.iti.mklab.kindex.RelExt;

import gr.iti.mklab.kindex.Babelfy.BabelfyHandler;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Class represent a term in KB response
 *
 * Created by Thodoris Tsompanidis on 7/1/2016.
 */
public class KBItem {
	String term;
	String concept;
	String BabelNet;
	String DBPedia;

	public KBItem(String term, String concept) {
		this.term = term;
		this.concept = concept;
		this.BabelNet = "";
		this.DBPedia = "";
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getConcept() {
		return concept;
	}

	public void setConcept(String concept) {
		this.concept = concept;
	}

	public String getBabelNet() {
		return BabelNet;
	}

	public void setBabelNet(String babelNet) {
		BabelNet = babelNet;
	}

	public String getDBPedia() {
		return DBPedia;
	}

	public void setDBPedia(String DBPedia) {
		this.DBPedia = DBPedia;
	}

	public void babelfyMe() {
		String babelJson = BabelfyHandler.annotateText(this.term);
		JSONArray json = new JSONArray(babelJson);

		//babelfy may return more than one response, if this.term consists of more than 1 term
		//the better response will be the one nontaining more terms

		int max = -1;
		JSONObject max_obj= null;
		for (int i = 0; i < json.length(); i++) {

			JSONObject obj =json.getJSONObject(i);

			//if the number of containing terms is larger than max, proceed
			int diff = obj.getJSONObject("tokenFragment").getInt("end") - obj.getJSONObject("tokenFragment").getInt("start");
			if ( diff > max){
				max = diff;
				max_obj = obj;
			}
		}

		if (max > -1) {
			if (max_obj.has("DBpediaURL")) {
				this.DBPedia = max_obj.getString("DBpediaURL");
			}
			if (max_obj.has("BabelNetURL")) {
				this.BabelNet = max_obj.getString("BabelNetURL");
			}
		}
	}
}























