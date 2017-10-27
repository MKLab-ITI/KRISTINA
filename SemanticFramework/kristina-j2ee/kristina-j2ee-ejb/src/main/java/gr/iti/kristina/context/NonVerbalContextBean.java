package gr.iti.kristina.context;

import java.util.LinkedList;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Strings;

@Singleton
@LocalBean
public class NonVerbalContextBean {
	LinkedList<NonVerbalContextItem> nonVerbalContextItems;

	public NonVerbalContextBean() {
		if (nonVerbalContextItems == null)
			nonVerbalContextItems = new LinkedList<>();
	}

	@PostConstruct
	public void initialisation() {
		System.out.println("---> NonVerbalContext bean initialisation ...");
		nonVerbalContextItems = new LinkedList<>();
	}

	// {valence:0.25,arousal:0.0}
	public void update(String input) throws JSONException {
		try {
			if (!Strings.isNullOrEmpty(input)) {
				JSONObject obj = new JSONObject(input);
				NonVerbalContextItem i = new NonVerbalContextItem(obj.getDouble("valence"), obj.getDouble("arousal"));
				this.nonVerbalContextItems.add(i);
				System.out.println("added (" + nonVerbalContextItems.size() + "):" + obj.toString());
			}
		} catch (JSONException e) {
			throw new JSONException(e);
		}

	}

	/**
	 * inline class
	 * 
	 * @author gmeditsk
	 *
	 */
	public class NonVerbalContextItem {
		double valence, arousal;
		DateTime timestamp;

		public NonVerbalContextItem() {
			timestamp = new DateTime();
		}

		public NonVerbalContextItem(Double valence, Double arousal) {
			timestamp = new DateTime();
			this.valence = valence;
			this.arousal = arousal;
		}

		public double getValence() {
			return valence;
		}

		public double getArousal() {
			return arousal;
		}

		public void setValence(double valence) {
			this.valence = valence;
		}

		public void setArousal(double arousal) {
			this.arousal = arousal;
		}
	}

	public static void main(String[] args) {
		JSONObject obj = new JSONObject("{ssss:0, dddd:34}");
		System.out.println(obj.getDouble("dddd"));
	}
}
