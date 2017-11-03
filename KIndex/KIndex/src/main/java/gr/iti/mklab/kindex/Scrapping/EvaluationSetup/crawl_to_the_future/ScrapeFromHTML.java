package gr.iti.mklab.kindex.Scrapping.EvaluationSetup.crawl_to_the_future;

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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

/**
 * Created by spyridons on 23/10/2017.
 */
public class ScrapeFromHTML {
    public static void main(String[] args) {
        scrapeFirstAlternative();
        scrapeSecondAlternative();
    }

    public static void scrapeBoilerpipeOnly(){
        File dataset = new File("input/scraping_dataset/crawl_to_the_future/original");
        ArrayList<ExtractorBase> extractors = new ArrayList<>();
        extractors.add(CommonExtractors.DEFAULT_EXTRACTOR);
        extractors.add(CommonExtractors.ARTICLE_EXTRACTOR);
        System.out.println(CommonExtractors.DEFAULT_EXTRACTOR.toString());
        for (File subfolder: dataset.listFiles()){
            if(subfolder.isDirectory()){
                File dateFolder = new File(subfolder.getPath() + "/2015");
                File scrapedContentFolder = new File(dateFolder.getPath().replace("original","scraped"));
                scrapedContentFolder.mkdirs();
                for(File file: dateFolder.listFiles()){
                    if(file.getName().endsWith(".html")){
                        for (ExtractorBase extractor : extractors){
                            try {
                                String fileName = "";
                                if(extractor instanceof DefaultExtractor)
                                    fileName = scrapedContentFolder.getPath() + "/" +
                                            file.getName().replace(".html", "_DE.txt");
                                else if (extractor instanceof ArticleExtractor)
                                    fileName = scrapedContentFolder.getPath() + "/" +
                                            file.getName().replace(".html", "_AE.txt");
                                String text = extractor.getText(new FileReader(file));
                                FileFunctions.writeToFile(fileName,text);
                            } catch (BoilerpipeProcessingException e) {
                                e.printStackTrace();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Version where we extracted scraped content in HTML format (with boilerpipe
     * and then extracted p, li and h1-6 elements to form final text
     */
    public static void scrapeFirstAlternative(){
        long totalElapsedAE = 0;
        long totalElapsedDE = 0;

        File dataset = new File("input/scraping_dataset/crawl_to_the_future/original");
        ArrayList<ExtractorBase> extractors = new ArrayList<>();
        extractors.add(CommonExtractors.DEFAULT_EXTRACTOR);
        extractors.add(CommonExtractors.ARTICLE_EXTRACTOR);
        System.out.println(CommonExtractors.DEFAULT_EXTRACTOR.toString());
        for (File subfolder: dataset.listFiles()){
            if(subfolder.isDirectory()){
                File dateFolder = new File(subfolder.getPath() + "/2015");
                File scrapedContentFolder = new File(dateFolder.getPath().replace("original","scraped_cut"));
                scrapedContentFolder.mkdirs();
                for(File file: dateFolder.listFiles()){
                    if(file.getName().endsWith(".html")){
                        for (ExtractorBase extractor : extractors){
                            try {
                                String fileName = "";
                                if(extractor instanceof DefaultExtractor)
                                    fileName = scrapedContentFolder.getPath() + "/" +
                                            file.getName().replace(".html", "_DE.txt");
                                else if (extractor instanceof ArticleExtractor)
                                    fileName = scrapedContentFolder.getPath() + "/" +
                                            file.getName().replace(".html", "_AE.txt");
                                long t0 = System.currentTimeMillis();
                                String html = getHtml(file,extractor);
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
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Total elapsed AE: " + totalElapsedAE);
        System.out.println("Total elapsed DE: " + totalElapsedDE);
    }

    /**
     * Version for the manual scraping using keep everything extractor and central div
     */
    public static void scrapeSecondAlternative(){
        long totalElapsed = 0;

        Map<String,String> contentElementMap = new HashMap<>();
        contentElementMap.put("news.bbc.co.uk","div.story-body");
        contentElementMap.put("news.yahoo.com","div.book,div.body.read-more-cut");
        contentElementMap.put("thenation.com","div.blog-body .content,#wysiwyg");
        contentElementMap.put("www.cnn.com","#body-text");
        contentElementMap.put("www.esquire.com","#article_body,div.imageContent");
        contentElementMap.put("www.forbes.com","div.body_inner, div.top_promo_block.clearfix");
        contentElementMap.put("www.foxnews.com","div[itemprop=articleBody]");
        contentElementMap.put("www.latimes.com","section.trb_mainContent");
        contentElementMap.put("www.nymag.com","div[itemprop=articleBody], div.par.parsys");


        File dataset = new File("input/scraping_dataset/crawl_to_the_future/original_without_msn");
        ArrayList<ExtractorBase> extractors = new ArrayList<>();
        extractors.add(CommonExtractors.KEEP_EVERYTHING_EXTRACTOR);
        for (File subfolder: dataset.listFiles()){
            if(subfolder.isDirectory()){
                File dateFolder = new File(subfolder.getPath() + "/2015");
                File scrapedContentFolder = new File(dateFolder.getPath().replace("original_without_msn","scraped_cut"));
                scrapedContentFolder.mkdirs();
                for(File file: dateFolder.listFiles()){
                    if(file.getName().endsWith(".html")){
                        System.out.println(file.getPath());
                        for (ExtractorBase extractor : extractors){
                            try {
                                String fileName = "";
                                fileName = scrapedContentFolder.getPath() + "/" +
                                        file.getName().replace(".html", "_custom.txt");
                                long t0 = System.currentTimeMillis();
                                String html = getHtml(file,extractor);
//                                if(subfolder.getName().contains("foxnews"))
//                                    System.out.println();
                                String text = getText(html, contentElementMap.get(subfolder.getName()));
                                long t1 = System.currentTimeMillis();
                                long elapsed = t1 - t0 ;
                                totalElapsed +=elapsed;
                                FileFunctions.writeToFile(fileName,text);
                            } catch (BoilerpipeProcessingException e) {
                                e.printStackTrace();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (SAXException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Elapsed custom: " + totalElapsed);
    }

    public static String getHtml(File file, ExtractorBase extractor) throws SAXException, BoilerpipeProcessingException, FileNotFoundException {
        final HTMLHighlighter hh = HTMLHighlighter.newExtractingInstance();
        InputSource is = new InputSource(new FileReader(file));
        HTMLDocument htmlDoc = new HTMLDocument(FileFunctions.readInputFile(file.getPath()));
        TextDocument doc = new BoilerpipeSAXInput(htmlDoc.toInputSource()).getTextDocument();
        extractor.process(doc);
        String html = hh.process(doc, htmlDoc.toInputSource());
        return html;
    }

    public static String getText(String html){
        List<String> defaultElements = Arrays.asList(new String[]{"h1","h2","h3","h4","h5","h6","p","li"});
        // get html element and iterate through them
        Document document = Jsoup.parse(html);
        Elements contentElements = document.getAllElements();
        StringBuilder textBuilder = new StringBuilder();
//        for (int i = 0; i < contentElements.size() ; i++) {
        Elements validElements = contentElements.select(String.join(",", defaultElements));
        for (int j = 0; j < validElements.size(); j++) {
            Element element = validElements.get(j);
            // if element is not child of another valid element
            if(isValidElement(element, defaultElements)){
                String elementText = element.text();
                textBuilder.append(elementText + "\n");
            }
        }
//        }
        // for the end of the html
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
//        for (int i = 0; i < contentElements.size() ; i++) {
        if(contentElements.size()!=1)
            System.out.println(contentElements.size());
        Elements validElements = contentElements.get(0).select(String.join(",", defaultElements));
        for (int j = 0; j < validElements.size(); j++) {
            Element element = validElements.get(j);
            // if element is not child of another valid element
            if(isValidElement(element, defaultElements)){
                String elementText = element.text();
                textBuilder.append(elementText + "\n");
            }
        }
//        }
        // for the end of the html
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
}
