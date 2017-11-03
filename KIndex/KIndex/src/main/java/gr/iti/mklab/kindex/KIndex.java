package gr.iti.mklab.kindex;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 *  KIndex is an Application developed as part 
 *  of KRISTINA Horizon2020 Project.
 *  
 *  Main functionalities of KIndex:
 *  - Scraping specified urls (output of Nutch Crawling)
 *  - Indexing
 *
 * 	 KIndex has a simple command line user interface, but gets calling parameters as input as well.
 * 	 If no parameters are provided, KIndex opens in console mode.
 * 	 In console mode, user can type "help" anytime, to see all options available.
 * 	 Calling parameters have to be like: "java -jar KIndex.jar indexer.start indexer.query scaper.start.12231233" etc.
 * 	 To see current version of KIndex, open in console and type "version"
 * 	 See more in documentation
 *
 *	@since 2015-11-20
 */
public class KIndex 
{


	/**
	 *
	 * @param args String. Commands for KIndexer (HANDLER.COMMAND) . Ex: "java -jar KIndex.jar indexer.start indexer.query scaper.start.12231233"
	 */
    public static void main( String[] args )
    {
		ArrayList<String> arguments = new ArrayList<String>();
		Collections.addAll(arguments, args);

		System.out.println("=================================================");
		System.out.println("                 KIndex Started");
		System.out.println("=================================================");

		//If there are not args in main, then go to console mode. In console mode user can set KIndex input from command line
		//If there are args in main, go to Arguments mode. In Arguments mode functionality comes from arguments

		if (!arguments.isEmpty()){
			InputHandler ih= new InputHandler("arguments", arguments);
		}
		else {
			InputHandler ih = new InputHandler("console");
		}
		System.out.println("=================================================");
		System.out.println("                  KIndex Finished");
		System.out.println("=================================================");
	}
}




























