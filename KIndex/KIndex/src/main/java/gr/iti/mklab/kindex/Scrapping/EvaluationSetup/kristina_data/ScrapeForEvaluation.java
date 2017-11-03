package gr.iti.mklab.kindex.Scrapping.EvaluationSetup.kristina_data;

import com.kohlschutter.boilerpipe.BoilerpipeProcessingException;
import com.kohlschutter.boilerpipe.document.TextDocument;
import com.kohlschutter.boilerpipe.extractors.ArticleExtractor;
import com.kohlschutter.boilerpipe.extractors.CommonExtractors;
import com.kohlschutter.boilerpipe.extractors.DefaultExtractor;
import com.kohlschutter.boilerpipe.extractors.ExtractorBase;
import com.kohlschutter.boilerpipe.sax.BoilerpipeSAXInput;
import com.kohlschutter.boilerpipe.sax.HTMLDocument;
import com.kohlschutter.boilerpipe.sax.HTMLHighlighter;
import gr.iti.mklab.kindex.Functions.FileFunctions;
import gr.iti.mklab.kindex.Indexing.ContentManagers.ContentManager;
import gr.iti.mklab.kindex.Indexing.ContentManagers.ContentManagerConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * Boilerpipe: Article/ Default extractor then extract p, li and h1-6 elements
 * Custom: Keep everything extractor, then get central element, then extract p, li and h1-6 elements
 * Created by spyridons on 16/10/2017.
 */
public class ScrapeForEvaluation {


    public static void main(String[] args){
        scrapeBoilepipe();
        scrapeCustom();
    }

    public static void scrapeBoilepipe(){
        String[] languages = {"de","es","pl"};

        String root = "input/scraping_dataset/kristina/";

        ArrayList<ExtractorBase> extractors = new ArrayList<>();
        extractors.add(CommonExtractors.DEFAULT_EXTRACTOR);
        extractors.add(CommonExtractors.ARTICLE_EXTRACTOR);
        for (String language:languages){
            long totalElapsedAE = 0;
            long totalElapsedDE = 0;
            String datasetFolder = root + language;
            String urlsFile = datasetFolder + "/urls.txt";
            String urls = FileFunctions.readInputFile(urlsFile);
            String[] urlsArray = urls.split("\r\n");
            int urlsArraySize = urlsArray.length;
            for (int i = 1; i <= urlsArraySize; i++) {
                if(urlsArray[i-1].startsWith("#"))
                    continue;
                String url = urlsArray[i-1].split("\\s+")[1];
                System.out.println(url);
//                String scrapedContentFilename = datasetFolder + "/" + i + ".txt";
//                String scrapedContent = FileFunctions.readInputFile(scrapedContentFilename);
                for (ExtractorBase extractor : extractors){
                    try {
                        String fileName = "";
                        if(extractor instanceof DefaultExtractor)
                            fileName = datasetFolder + "/" + i + "_DE.txt";
                        else if (extractor instanceof ArticleExtractor)
                            fileName = datasetFolder + "/" + i + "_AE.txt";
                        long t0 = System.currentTimeMillis();
                        final HTMLHighlighter hh = HTMLHighlighter.newExtractingInstance();
                        String html = hh.process(new URL(url), extractor);
                        String text = getText(html);
                        long t1 = System.currentTimeMillis();
                        long elapsed = t1 - t0 ;
                        if(extractor instanceof DefaultExtractor)
                            totalElapsedDE += elapsed;
                        else if(extractor instanceof ArticleExtractor)
                            totalElapsedAE += elapsed;

                        FileFunctions.writeToFile(fileName,text);
                    } catch (BoilerpipeProcessingException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            System.out.println("Total elapsed AE (" + language + "): " + totalElapsedAE);
            System.out.println("Total elapsed DE (" + language + "): " + totalElapsedDE);
        }

    }

    public static void scrapeCustom(){
        String[] languages = {"de","es","pl"};
//        String[] languages = {"pl"};
        Map<String, String> contentElementMap = new HashMap<>(ContentManagerConstants.contentElementMapES);
        contentElementMap.putAll(ContentManagerConstants.contentElementMapDE);
        contentElementMap.putAll(ContentManagerConstants.contentElementMapPL);

        String root = "input/scraping_dataset/kristina/";

        ArrayList<ExtractorBase> extractors = new ArrayList<>();
        extractors.add(CommonExtractors.KEEP_EVERYTHING_EXTRACTOR);
        for (String language:languages){

            long totalElapsed = 0;
            String datasetFolder = root + language;
            String urlsFile = datasetFolder + "/urls.txt";
            String urls = FileFunctions.readInputFile(urlsFile);
            String[] urlsArray = urls.split("\r\n");
            int urlsArraySize = urlsArray.length;
            for (int i = 1; i <= urlsArraySize; i++) {
                if(urlsArray[i-1].startsWith("#"))
                    continue;
                String url = urlsArray[i-1].split("\\s+")[1];
                if(url.contains("choroby.senior"))
                    System.out.println();
                System.out.println(url);
//                String scrapedContentFilename = datasetFolder + "/" + i + ".txt";
//                String scrapedContent = FileFunctions.readInputFile(scrapedContentFilename);
                for (ExtractorBase extractor : extractors){
                    try {
                        String fileName = datasetFolder + "/" + i + "_custom.txt";
                        long t0 = System.currentTimeMillis();
                        final HTMLHighlighter hh = HTMLHighlighter.newExtractingInstance();
                        String html = hh.process(new URL(url), extractor);
                        String text = getText(html, contentElementMap.get(getHostName(url)));
                        long t1 = System.currentTimeMillis();
                        long elapsed = t1 - t0 ;
                        totalElapsed += elapsed;

                        FileFunctions.writeToFile(fileName,text);
                    } catch (BoilerpipeProcessingException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("Total elapsed custom (" + language + "): " + totalElapsed);
        }

    }

    public static String getText(String html){
        List<String> defaultElements = Arrays.asList(new String[]{"h1","h2","h3","h4","h5","h6","p","li"});
        // get html element and iterate through them
        Document document = Jsoup.parse(html);
        Elements contentElements = document.getAllElements();
        StringBuilder textBuilder = new StringBuilder();
        Elements validElements = contentElements.select(String.join(",", defaultElements));
        for (int j = 0; j < validElements.size(); j++) {
            Element element = validElements.get(j);
            // if element is not child of another valid element
            if(isValidElement(element, defaultElements)){
                String elementText = element.text();
                textBuilder.append(elementText + "\n");
            }
        }
        String text = textBuilder.toString();
        return text;
    }

    public static String getText(String html, String contentSelector){
        List<String> defaultElements = Arrays.asList(new String[]{"h1","h2","h3","h4","h5","h6","p","li"});
        // get html element and iterate through them
        Document document = Jsoup.parse(html);
        Elements elements = document.getAllElements();
        Elements contentElements = elements.select(contentSelector);
        StringBuilder textBuilder = new StringBuilder();
        if(contentElements.size()!=1)
            System.out.println(contentElements.size());
        for (int i = 0; i < contentElements.size(); i++) {
            Elements validElements = contentElements.get(i).select(String.join(",", defaultElements));
            for (int j = 0; j < validElements.size(); j++) {
                Element element = validElements.get(j);
                // if element is not child of another valid element
                if(isValidElement(element, defaultElements)){
                    String elementText = element.text();
                    textBuilder.append(elementText + "\n");
                }
            }
        }
        String text = textBuilder.toString();
        return text;
    }


    /**
     * Checks the rule that an element should not be a child of a set of specified elements
     * @param element
     * @return
     */
    public static boolean isValidElement(Element element, List<String> defaultElements){

        if(element.text().trim().replaceAll("\\u00A0", "").length() == 0)
            return false;

        Elements parents = element.parents();
        for (Element parent: parents){
            String parentTag = parent.tagName();
            if(defaultElements.contains(parentTag))
                return false;
        }
        return true;
    }

    public static String getHostName(String url) {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String hostname = uri.getHost();
        // to provide faultproof result, check if not null then return only hostname, without www.
        if (hostname != null) {
            return hostname.startsWith("www.") ? hostname.substring(4) : hostname;
        }
        return hostname;
    }
}
