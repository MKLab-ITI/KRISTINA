package gr.iti.kristina.errors;

import com.google.gson.Gson;

public class ErrorHandling {

	public static String error(String custom, Exception e) {

		ErrorModel m = new ErrorModel();
		m.setMessage(custom);
		if (e != null)
			m.setDetails(e.getMessage());
		else 
			m.setDetails("");
		Gson gson = new Gson();
		String json = gson.toJson(new Error(m));
		System.err.println(json);
		return json;

	}

	public static void main(String[] args) {
		String error = ErrorHandling.error("custom message", new Exception("Hello"));
		System.out.println(error);
	}

}
