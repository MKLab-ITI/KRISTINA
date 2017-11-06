package WebKIndex;

import GoogleMaps.GoogleMapsHandler;
import GoogleMaps.GooglePlacesResultObject;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by spyridons on 2/23/2017.
 */

@CrossOrigin
@EnableScheduling
@Controller
public class MapsController {

    static Logger log = LogManager.getLogger("Global");

    @RequestMapping(value = "/placesAtRadiusJSON", produces = "application/json")
    @ResponseBody
    public String getPlacesAtRadiusJSON(@RequestParam(value="address") String address,
                                  @RequestParam(value="placeType", required=false, defaultValue = "") String placeType,
                                  @RequestParam(value="radius", required=false, defaultValue = "2000") int radius)
    {
        SimpleDateFormat ft = new SimpleDateFormat ("hh:mm:ss a zzz, E, dd.MM.yyyy");
        log.info("Requested nearby places for address: " +address);
        System.out.println("Requested nearby places for address: " +address + " at " + ft.format(new Date()));

        long start = System.currentTimeMillis();

        GoogleMapsHandler mapsHandler = new GoogleMapsHandler(radius);
        LatLng coordinates = mapsHandler.getAddressCoordinates(address);
        String response;
        if ( ! placeType.equals("") ){
            PlaceType type = null;
            for (PlaceType pt : PlaceType.values()){
                if (pt.toString().equals(placeType)){
                    type = pt;
                    break;
                }
            }
            PlacesSearchResponse psResponse = mapsHandler.getPlacesAtRadius(coordinates,type);
            response = GoogleMapsHandler.getJsonFromSearchResponse(psResponse);
        }
        else{
            PlacesSearchResponse psResponse = mapsHandler.getPlacesAtRadius(coordinates);
            response = GoogleMapsHandler.getJsonFromSearchResponse(psResponse);
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Elapsed time (millis): " + elapsed);

        return response;
    }

    @RequestMapping(value = "/placesAtRadiusCoordJSON", produces = "application/json")
    @ResponseBody
    public String getPlacesAtRadiusCoordJSON(@RequestParam(value="lat") double latitude,
                                  @RequestParam(value="lng") double longitude,
                                  @RequestParam(value="placeType", required=false, defaultValue = "") String placeType,
                                  @RequestParam(value="radius", required=false, defaultValue = "2000") int radius)
    {
        SimpleDateFormat ft = new SimpleDateFormat ("hh:mm:ss a zzz, E, dd.MM.yyyy");
        log.info("Requested nearby places for lat,lng: " + latitude + ", " + longitude);
        System.out.println("Requested nearby places for lat,lng: "  + latitude + ", " + longitude + " at " + ft.format(new Date()));

        long start = System.currentTimeMillis();

        GoogleMapsHandler mapsHandler = new GoogleMapsHandler(radius);
        LatLng coordinates = new LatLng(latitude, longitude);
        String response;
        if ( ! placeType.equals("") ){
            PlaceType type = null;
            for (PlaceType pt : PlaceType.values()){
                if (pt.toString().equals(placeType)){
                    type = pt;
                    break;
                }
            }
            PlacesSearchResponse psResponse = mapsHandler.getPlacesAtRadius(coordinates,type);
            response = GoogleMapsHandler.getJsonFromSearchResponse(psResponse);
        }
        else{
            PlacesSearchResponse psResponse = mapsHandler.getPlacesAtRadius(coordinates);
            response = GoogleMapsHandler.getJsonFromSearchResponse(psResponse);
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Elapsed time (millis): " + elapsed);

        return response;
    }

    @RequestMapping(value = "/placesJSON", produces = "application/json")
    @ResponseBody
    public String getNearbyPlacesJSON(@RequestParam(value="address") String address,
                                  @RequestParam(value="placeType", required=false, defaultValue = "") String placeType)
    {
        SimpleDateFormat ft = new SimpleDateFormat ("hh:mm:ss a zzz, E, dd.MM.yyyy");
        log.info("Requested nearby places for address: " +address);
        System.out.println("Requested nearby places for address: " +address + " at " + ft.format(new Date()));

        long start = System.currentTimeMillis();

        GoogleMapsHandler mapsHandler = new GoogleMapsHandler();
        LatLng coordinates = mapsHandler.getAddressCoordinates(address);
        String response;
        if ( ! placeType.equals("") ){
            PlaceType type = null;
            for (PlaceType pt : PlaceType.values()){
                if (pt.toString().equals(placeType)){
                    type = pt;
                    break;
                }
            }
            PlacesSearchResponse psResponse = mapsHandler.getNearbyPlaces(coordinates,type);
            response = GoogleMapsHandler.getJsonFromSearchResponse(psResponse);
        }
        else{
            PlacesSearchResponse psResponse = mapsHandler.getNearbyPlaces(coordinates);
            response = GoogleMapsHandler.getJsonFromSearchResponse(psResponse);
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Elapsed time (millis): " + elapsed);

        return response;
    }

    @RequestMapping(value = "/placesCoordJSON", produces = "application/json")
    @ResponseBody
    public String getNearbyPlacesCoordJSON(@RequestParam(value="lat") double latitude,
                                  @RequestParam(value="lng") double longitude,
                                  @RequestParam(value="placeType", required=false, defaultValue = "") String placeType)
    {
        SimpleDateFormat ft = new SimpleDateFormat ("hh:mm:ss a zzz, E, dd.MM.yyyy");
        log.info("Requested nearby places for lat,lng: " + latitude + ", " + longitude);
        System.out.println("Requested nearby places for lat,lng: "  + latitude + ", " + longitude + " at " + ft.format(new Date()));

        long start = System.currentTimeMillis();

        GoogleMapsHandler mapsHandler = new GoogleMapsHandler();
        LatLng coordinates = new LatLng(latitude, longitude);
        String response;
        if ( ! placeType.equals("") ){
            PlaceType type = null;
            for (PlaceType pt : PlaceType.values()){
                if (pt.toString().equals(placeType)){
                    type = pt;
                    break;
                }
            }
            PlacesSearchResponse psResponse = mapsHandler.getNearbyPlaces(coordinates,type);
            response = GoogleMapsHandler.getJsonFromSearchResponse(psResponse);
        }
        else{
            PlacesSearchResponse psResponse = mapsHandler.getNearbyPlaces(coordinates);
            response = GoogleMapsHandler.getJsonFromSearchResponse(psResponse);
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Elapsed time (millis): " + elapsed);

        return response;
    }

    @RequestMapping("/places")
    public String getNearbyPlaces(@RequestParam(value="address") String address,
                                  @RequestParam(value="placeType", required=false, defaultValue = "") String placeType,
                                  Model model)
    {
        SimpleDateFormat ft = new SimpleDateFormat ("hh:mm:ss a zzz, E, dd.MM.yyyy");
        log.info("Requested nearby places for address: " +address);
        System.out.println("Requested nearby places for address: " +address + " at " + ft.format(new Date()));

        long start = System.currentTimeMillis();

        GoogleMapsHandler mapsHandler = new GoogleMapsHandler();
        LatLng coordinates = mapsHandler.getAddressCoordinates(address);
        PlacesSearchResponse response;
        if ( ! placeType.equals("") ){
            PlaceType type = null;
            for (PlaceType pt : PlaceType.values()){
                if (pt.toString().equals(placeType)){
                    type = pt;
                    break;
                }
            }
            response = mapsHandler.getNearbyPlaces(coordinates,type);
        }
        else{
            response = mapsHandler.getNearbyPlaces(coordinates);
        }

        Set<GooglePlacesResultObject> results = new LinkedHashSet<>();
        for (PlacesSearchResult result: response.results){
            String url = mapsHandler.getLinkByPlaceID(result.placeId);
            GooglePlacesResultObject obj = new GooglePlacesResultObject();
            obj.setResult(result);
            obj.setUrl(url);
            results.add(obj);
        }

        model.addAttribute("results", results);
        model.addAttribute("address", address);

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Elapsed time (millis): " + elapsed);

        return "nearestPlaces/Places";
    }

    @RequestMapping("/placesCoord")
    public String getNearbyPlacesCoord(@RequestParam(value="lat") double latitude,
                                       @RequestParam(value="lng") double longitude,
                                       @RequestParam(value="placeType", required=false, defaultValue = "") String placeType,
                                       Model model)
    {
        SimpleDateFormat ft = new SimpleDateFormat ("hh:mm:ss a zzz, E, dd.MM.yyyy");
        log.info("Requested nearby places for lat,lng: " + latitude + ", " + longitude);
        System.out.println("Requested nearby places for lat,lng: "  + latitude + ", " + longitude + " at " + ft.format(new Date()));

        long start = System.currentTimeMillis();

        GoogleMapsHandler mapsHandler = new GoogleMapsHandler();
        LatLng coordinates = new LatLng(latitude, longitude);
        PlacesSearchResponse response;
        if ( ! placeType.equals("") ){
            PlaceType type = null;
            for (PlaceType pt : PlaceType.values()){
                if (pt.toString().equals(placeType)){
                    type = pt;
                    break;
                }
            }
            response = mapsHandler.getNearbyPlaces(coordinates,type);
        }
        else{
            response = mapsHandler.getNearbyPlaces(coordinates);
        }

        Set<GooglePlacesResultObject> results = new LinkedHashSet<>();
        for (PlacesSearchResult result: response.results){
            String url = mapsHandler.getLinkByPlaceID(result.placeId);
            GooglePlacesResultObject obj = new GooglePlacesResultObject();
            obj.setResult(result);
            obj.setUrl(url);
            results.add(obj);
        }

        model.addAttribute("results", results);
        model.addAttribute("address", latitude + ", " + longitude);

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Elapsed time (millis): " + elapsed);

        return "nearestPlaces/Places";
    }

    @RequestMapping(value = "/placesURL", produces = "text/plain")
    @ResponseBody
    public String getNearbyPlacesURL(@RequestParam(value="address") String address,
                                  @RequestParam(value="placeType", required=false, defaultValue = "") String placeType)
    {
        SimpleDateFormat ft = new SimpleDateFormat ("hh:mm:ss a zzz, E, dd.MM.yyyy");
        log.info("Requested nearby places URL for address: " +address);
        System.out.println("Requested nearby places URL for address: " +address + " at " + ft.format(new Date()));

        String response = DefaultValues.baseURL + "/places?address=" + address;

        if(!placeType.equals(""))
            response += "&placeType=" + placeType;

        return response;
    }

    @RequestMapping(value = "/placesCoordURL", produces = "text/plain")
    @ResponseBody
    public String getNearbyPlacesCoordURL(@RequestParam(value="lat") double latitude,
                                     @RequestParam(value="lng") double longitude,
                                     @RequestParam(value="placeType", required=false, defaultValue = "") String placeType)
    {
        SimpleDateFormat ft = new SimpleDateFormat ("hh:mm:ss a zzz, E, dd.MM.yyyy");
        log.info("Requested nearby places URL for lat,lng: " + latitude + ", " + longitude);
        System.out.println("Requested nearby places URL for lat,lng: "  + latitude + ", " + longitude + " at " + ft.format(new Date()));

        String response = DefaultValues.baseURL + "/placesCoord?lat=" + latitude + "&lng=" + longitude;

        if(!placeType.equals(""))
            response += "&placeType=" + placeType;

        return response;
    }
}
