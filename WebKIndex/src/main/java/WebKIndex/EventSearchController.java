package WebKIndex;

import EventSearch.EventSearchHandler;
import TopicDetection.mongo.GettingDataFromMongo;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by spyridons on 3/2/2017.
 */

@CrossOrigin
@EnableScheduling
@RestController
public class EventSearchController {

    // variable to block searches are events are crawled and stored in mongo db
    private static boolean ONGOING_CRAWLING_PROCESS = false;

    @RequestMapping(value = "/getBabyEvent", produces = "text/plain")
    @ResponseBody
    public String getBabyEvent(@RequestParam(value="town") String town){
        System.out.println("Requested baby event in " + town);
        String url = EventSearchHandler.getBabyEventURL(town);
        return url;
    }

    @RequestMapping(value = "/getGenericEvent", produces = "text/plain")
    @ResponseBody
    public String getGenericEvent(@RequestParam(value="town") String town){
        System.out.println("Requested generic event in " + town);
        return EventSearchHandler.getGenericEventUrl(town);
    }

    @RequestMapping("/tubingenEventCrawling")
    //run this function after 10 hours, and re-run every 24 hours
    @Scheduled(initialDelay=36000000, fixedRate=86400000) //in milliseconds
    private void tubingenEventCrawling(){

        // block searches until method ends
        ONGOING_CRAWLING_PROCESS = true;

        try{
            SimpleDateFormat ft = new SimpleDateFormat ("hh:mm:ss a zzz, E, dd.MM.yyyy");
            System.out.println("Scheduled task for tubingen event crawling started at " + ft.format(new Date()));

            boolean success = EventSearchHandler.crawlTubingenGermanEvents();
            if (success)
                System.out.println("Tubingen event crawling finished successfully at " + ft.format(new Date()));
            else
                System.out.println("Tubingen event crawling failed!!!");
        }
        catch(Exception e){
            System.out.println("Exception while crawling tubingen events!!!");
        }

        // unblock searches
        ONGOING_CRAWLING_PROCESS = false;
    }

    @RequestMapping(value = "/tubingenEventSearch", produces = "application/json")
    @ResponseBody
    public String tubingenEventSearch(@RequestParam(value="fieldName", required=false, defaultValue = "") String fieldName,
                                      @RequestParam(value="value", required=false, defaultValue = "") String value,
                                      @RequestParam(value="containsSearch", required=false, defaultValue = "false") boolean containsSearch){
        System.out.println("Requested event search in Tubignen");
        System.out.println("Field name: " + fieldName + "\tValue: " + value + "\tContains search: " + containsSearch);
        if (ONGOING_CRAWLING_PROCESS)
            return "Events are now crawled at this moment. Try again in a few moments.";
        else
            return EventSearchHandler.searchTubingenGermanEvents(fieldName,value,containsSearch);
    }
}
