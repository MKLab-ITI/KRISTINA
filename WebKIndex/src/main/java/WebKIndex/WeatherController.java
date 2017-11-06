package WebKIndex;

import Weather.WeatherHandler;
import Weather.WeatherResponseObject;
import com.google.gson.Gson;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by spyridons on 7/27/2016.
 */

@CrossOrigin
@EnableScheduling
@RestController
public class WeatherController {

    @RequestMapping(value = "/weather", produces = "application/json")
    @ResponseBody
    public String weatherResponse(@RequestParam(value="placeCode", required=false, defaultValue = "") String placeCode,
                                  @RequestParam(value="startDate", required=false, defaultValue = "") String startDate,
                                  @RequestParam(value="endDate", required=false, defaultValue = "") String endDate)
    {
        WeatherHandler wHandler = new WeatherHandler();
        String response = wHandler.getWeatherResponse(placeCode,startDate,endDate);
        return response;
    }

}
