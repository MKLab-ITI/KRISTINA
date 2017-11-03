package gr.iti.mklab.kindex.Functions;

import java.util.*;

/**
 * Created by spyridons on 10/5/2016.
 */
public class MapFunctions {

    public static Map.Entry<String,Double> getMaxEntry(HashMap<String,Double> labelMap){
        // find label with max score
        Map.Entry<String,Double> maxEntry = null;
            for (Map.Entry<String,Double> entry : labelMap.entrySet())
            {
                if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
                {
                    maxEntry = entry;
                }
            }
        return maxEntry;
    }

    public static Map.Entry<String,Double> getFilteredMaxEntry(HashMap<String,Double> labelMap){

        List<String> weakTags = Arrays.asList("TrIP", "TrWP", "TrCP", "TrNAP", "TeCP");
        List<String> strongTags = Arrays.asList("PIP", "TeRP");
        for (String tag: weakTags){
            if(labelMap.get(tag)>0.5){
                for(String sTag: strongTags){
                    labelMap.put(sTag,0.0);
                }
            }
        }

        // find label with max score
        Map.Entry<String,Double> maxEntry = null;
        for (Map.Entry<String,Double> entry : labelMap.entrySet())
        {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
            {
                maxEntry = entry;
            }
        }
        return maxEntry;
    }
}
