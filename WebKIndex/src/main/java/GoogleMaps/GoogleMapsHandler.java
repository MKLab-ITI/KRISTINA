package GoogleMaps;

import MetaMap.LocalMetaMapNewHandler;
import com.google.gson.Gson;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by spyridons on 2/23/2017.
 */
public class GoogleMapsHandler {
    private static final String apiKey = "key";
    public static PlaceType[] defaultPlaceTypes = {PlaceType.PARK, PlaceType.HOSPITAL};
    private int radius;
    private GeoApiContext context;

    public GoogleMapsHandler(){
        context = new GeoApiContext().setApiKey(this.apiKey);
        this.radius = 2000;
    }

    public GoogleMapsHandler(int radius){
        context = new GeoApiContext().setApiKey(this.apiKey);
        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public LatLng getAddressCoordinates(String address){
        GeocodingResult[] results;
        try {
            results = GeocodingApi.geocode(this.context,
                    address).await();
            return results[0].geometry.location;
        } catch (Exception e) {
            System.out.println("Error while calling Google Geocoding api!!!");
            System.out.println(e.getMessage());
            return null;
        }
    }

    public PlacesSearchResponse getPlacesAtRadius(LatLng coordinates, PlaceType... types){

        if(types.length == 0){
            types = this.defaultPlaceTypes;
        }

        try {
            PlacesSearchResponse results = PlacesApi.nearbySearchQuery(context, coordinates)
                    .radius(this.radius)
                    .type(types)
                    .await();
            return results;
        } catch (Exception e) {
            System.out.println("Error while calling Google Places api!!!");
            System.out.println(e.getMessage());
            return new PlacesSearchResponse();
        }
    }

    public PlacesSearchResponse getNearbyPlaces(LatLng coordinates, PlaceType... types){

        if(types.length == 0){
            types = this.defaultPlaceTypes;
        }

        try {
            PlacesSearchResponse results = PlacesApi.nearbySearchQuery(context, coordinates)
                    .type(types)
                    .rankby(RankBy.DISTANCE) // cannot be ranked by distance when a radius is given
                    .await();
            return results;
        } catch (Exception e) {
            System.out.println("Error while calling Google Places api!!!");
            System.out.println(e.getMessage());
            return new PlacesSearchResponse();
        }
    }

    public static PlacesSearchResponse getSearchResponseFromJson(String json){
        PlacesSearchResponse response = new Gson().fromJson(json, PlacesSearchResponse.class);
        return response;
    }

    public static String getJsonFromSearchResponse(PlacesSearchResponse response){
        String json = new Gson().toJson(response);
        return json;
    }

    public String getLinkByPlaceID(String placeID){
        try {
            PlaceDetails details = PlacesApi.placeDetails(context,placeID).await();
            return details.url.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }
}
