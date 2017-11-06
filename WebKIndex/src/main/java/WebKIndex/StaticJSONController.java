package WebKIndex;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Thodoris Tsompanidis on 13/1/2016.
 */

@Controller
public class StaticJSONController {

	static Logger log = LogManager.getLogger("Global");


	@RequestMapping("/webapquery")
	public String WebAPQueryResponse(Model model){

		log.info("webapqueryRequest");
		System.out.println("webapquery Request");


		return "WebAPQuery";
	}
}
