package gr.iti.mklab.kindex.MetaMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Main class for standalone execution of MetaMapHandler
 * Input for getting concepts is {@linkplain MetaMapHandler#OUTPUT_FILE}
 *
 * Created by Thodoris Tsompanidis on 11/12/2015.
 */
public class MetaMapMain {


	public static void main( String[] args ){
		String text = "Yellow fever begins after an incubation period of three to six days. Most cases only cause a mild infection with fever, headache, chills, back pain, fatigue, loss of appetite, muscle pain, nausea, and vomiting. In these cases, the infection lasts only three to four days.";
		MetaMapHandler mmh= new MetaMapHandler();
		mmh.extractFromTextToWords(text);

	}
}
