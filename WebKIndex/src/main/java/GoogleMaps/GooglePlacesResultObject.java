package GoogleMaps;

import com.google.maps.model.PlacesSearchResult;

/**
 * Created by spyridons on 4/10/2017.
 */
public class GooglePlacesResultObject {
    private PlacesSearchResult result;
    private String url;
    private double lat;
    private double lng;

    public PlacesSearchResult getResult() {
        return result;
    }

    public void setResult(PlacesSearchResult result) {
        this.result = result;
        this.lat = result.geometry.location.lat;
        this.lng = result.geometry.location.lng;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GooglePlacesResultObject that = (GooglePlacesResultObject) o;

        if (Double.compare(that.lat, lat) != 0) return false;
        return Double.compare(that.lng, lng) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(lat);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lng);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
