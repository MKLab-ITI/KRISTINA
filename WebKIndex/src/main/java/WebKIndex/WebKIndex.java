package WebKIndex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@SpringBootApplication
public class WebKIndex {

	public static final Logger LOGGER= LogManager.getLogger("GLOBAL");
    
	public static void 	main(String[] args) {
        
    	 SpringApplication.run(WebKIndex.class, args);
    }
}