package WebKIndex;

import Indexing.PassageIndexHandler;
import RelExtFusion.RelExtPipeline;
import RelExtFusion.demo.DemoResult;
import Version.PassageRetrievalVersion;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by Thodoris Tsompanidis on 4/12/2015.
 */

@Controller
public class DemosController {

	static Logger log = LogManager.getLogger("Global");


	@RequestMapping("/relationExtractionDemo")
	public String relationExtractionDemo(Model model){
		log.info("RelExtDemo.html request");
		System.out.println("RelExtDemo.html request");

		return "demos/relationExtraction/RelExtDemo";
	}

	@RequestMapping("/relationExtraction")
	public String relationExtraction(@RequestParam(value="sentence", required=true, defaultValue = "") String sentence,
										 Model model){
		RelExtPipeline pipeline = new RelExtPipeline();
		DemoResult result = pipeline.executeDemoPipeline(sentence, false);
		model.addAttribute("result",result);
		return "demos/relationExtraction/RelationsResult";
	}

}
