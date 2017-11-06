package Weather;

import java.util.List;

/**
 * Created by spyridons on 7/27/2016.
 */
public class WeatherResponseObject {

    private List<String> fields;
    private List<String> values;

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

}
