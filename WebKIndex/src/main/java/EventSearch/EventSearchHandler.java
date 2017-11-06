package EventSearch;

import MongoDB.MongoDBHandler;
import com.kohlschutter.boilerpipe.BoilerpipeExtractor;
import com.kohlschutter.boilerpipe.extractors.CommonExtractors;
import com.kohlschutter.boilerpipe.sax.HTMLHighlighter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by spyridons on 3/2/2017.
 */
public class EventSearchHandler {

    public static final String BARCELONA_BABY_EVENT_URL = "http://www.petitexplorador.com/familias/";
    public static final String MADRID_BABY_EVENT_URL = "https://www.esmadrid.com/agenda-infantil";
    public static final String BARCELONA_GENERIC_EVENT_URL = "http://ajuntament.barcelona.cat/centrescivics/es/centros-civicos";
    public static final String MADRID_GENERIC_EVENT_URL = "http://www.madrid.es/portales/munimadrid/es/Inicio/Cultura-ocio-y-deporte/Cultura-y-ocio/Actividades-y-eventos?vgnextfmt=default&vgnextchannel=15d3efff228fe410VgnVCM2000000c205a0aRCRD&vgnextoid=15d3efff228fe410VgnVCM2000000c205a0aRCRD";
    public static final String TUBINGEN_GENERIC_EVENT_URL = "https://de-de.facebook.com/stuttgartetkinlikpanosu/";
    public static final String TUBINGEN_GERMAN_EVENT_URL_PATTERN = "http://www.tagblatt.de/Veranstaltungen/%s?Zeitraum=Heute&kPageId=331";

    public static String getBabyEvent(String town){
        if(town.equals("Barcelona"))
            return getFirstBarcelonaBabyEvent();
        if(town.equals("Madrid"))
            return getFirstMadridBabyEvent();
        else{
            System.out.println("Town not available for baby events!!!");
            return "";
        }
    }

    private static String getFirstBarcelonaBabyEvent(){
        String content = "";
        final BoilerpipeExtractor extractor = CommonExtractors.KEEP_EVERYTHING_EXTRACTOR;
        final HTMLHighlighter hh = HTMLHighlighter.newExtractingInstance();
        URL url = null;
        try {
            url = new URL(BARCELONA_BABY_EVENT_URL);
            content = hh.process(url, extractor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Document document = Jsoup.parse(content);
        Elements elements = document.select("div.divCategoriesContingut.row.rounded-corners.amagarovf.centrarContingutRes div.padzero");

        StringBuilder firstEventBuilder = new StringBuilder();
        Element firstElement = elements.first();
        for(Element child: firstElement.select("div.row"))
           firstEventBuilder.append(child.text().replace("APUNTATE", "") + "\n");

        return firstEventBuilder.toString();
    }

    private static String getFirstMadridBabyEvent(){
        String content = "";
        final BoilerpipeExtractor extractor = CommonExtractors.KEEP_EVERYTHING_EXTRACTOR;
        final HTMLHighlighter hh = HTMLHighlighter.newExtractingInstance();
        URL url = null;
        try {
            url = new URL(MADRID_BABY_EVENT_URL);
            content = hh.process(url, extractor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Document document = Jsoup.parse(content);
        Elements elements = document.select("div#flexslider-1");

        StringBuilder firstEventBuilder = new StringBuilder();
        Element firstElement = elements.first();
        for(Element child: firstElement.select("div.field-item.odd"))
            firstEventBuilder.append(child.text() + "\n");

        return firstEventBuilder.toString();
    }

    public static String getBabyEventURL(String town){
        if(town.equals("Barcelona"))
            return BARCELONA_BABY_EVENT_URL;
        if(town.equals("Madrid"))
            return MADRID_BABY_EVENT_URL;
        else{
            System.out.println("Town not available for generic events!!!");
            return "";
        }
    }

    public static String getGenericEventUrl(String town){
        if(town.equals("Barcelona"))
            return BARCELONA_GENERIC_EVENT_URL;
        if(town.equals("Madrid"))
            return MADRID_GENERIC_EVENT_URL;
        if(town.equals("Tübingen"))
            return TUBINGEN_GENERIC_EVENT_URL;
        else{
            System.out.println("Town not available for generic events!!!");
            return "";
        }
    }

    public static boolean crawlTubingenGermanEvents(){
        String databaseName = "db";
        String collectionName = "coll";
        String userName = "user";
        String password = "pass";
        MongoDBHandler mongo = new MongoDBHandler(databaseName,userName,password);

        // clear old records
        mongo.clearCollection(collectionName);

        int pageNum = 1;
        String url;
        while(true){

            System.out.println("Crawling tubingen events page " + pageNum);

            // get correct url based on page
            String missingUrlPart = "Heute.html";
            if(pageNum>1)
                missingUrlPart = "Heute-Part" + pageNum + ".html";
            url = String.format(TUBINGEN_GERMAN_EVENT_URL_PATTERN, missingUrlPart);

            // scrape document and handle <br /> tags
            Document document = null;
            try {
                document = Jsoup.connect(url).timeout(30000).get();
            } catch (IOException e) {
                e.printStackTrace();
                mongo.close();
                return false;
            }
            String temp = document.html().replace("<br />", "$$$$$"); //$$$$$ instead of <br \>
            document = Jsoup.parse(temp); //Parse again

            // form document map and add it to mongo db
            Element dateElement = document.select("div.STEventSearchCtrl_ResultList_DateHeader span").first();

            if (dateElement == null) {
                System.out.println("Events list finished at page " + (pageNum-1));
                break;
            }

            String dateStr =  dateElement.text().substring(2).trim();

            Elements elements = document.select("div.STEventSearchCtrl_Row");

            for (Element element:elements) {
                Map<String, Object> docMap = new HashMap<>();
                String timeStr = element.select(".STEventSearchCtrl_ResultList_Time").first().ownText().replace("$$$$$", "\n");
                Elements eventBoxElements = element.select("div.STEventSearchCtrl_ResultList_EventBox div");
                String type = eventBoxElements.get(0).text().trim();
                String title = eventBoxElements.get(1).text().trim();
                String townPlaceContent = eventBoxElements.get(2).text();
                String[] parts = townPlaceContent.split("\\$\\$\\$\\$\\$");
                String town = parts[0].trim();
                String place = parts[1].trim();
                docMap.put("date", dateStr);
                docMap.put("time", timeStr);
                docMap.put("event_type", type);
                docMap.put("title", title);
                docMap.put("town", town);
                docMap.put("place", place);
                mongo.insertMapToDB(docMap, collectionName);
            }
            ++pageNum;
        }

        mongo.close();
        return true;
    }

    public static String searchTubingenGermanEvents(String fieldName, String value, boolean containsSearch){

        String databaseName = "db";
        String collectionName = "coll";
        String userName = "user";
        String password = "pass";
        MongoDBHandler mongo = new MongoDBHandler(databaseName,userName,password);
        if (fieldName.equals(""))
            mongo.findAll(collectionName);
        else if (containsSearch)
            mongo.findContains(collectionName,fieldName,value);
        else
            mongo.find(collectionName,fieldName,value,null);
        String result = mongo.getResultsJSON();
        mongo.close();
        return result;
    }

    public static void main(String[] args) {
        EventSearchHandler.crawlTubingenGermanEvents();
        System.out.println(EventSearchHandler.searchTubingenGermanEvents("place","en", true));
    }

}
