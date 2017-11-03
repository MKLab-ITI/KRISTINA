package gr.iti.mklab.kindex.Indexing.ContentManagers;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by spyridons on 26/9/2017.
 */
public class ContentManagerConstants {
    public static Map<String,String> documentTitleMap; // selectors per domain to extract title
    public static Map<String,String> documentListMap; // selectors per domain to filter list elements
    public static Map<String,String> contentElementMapPL; // selectors per domain to get content elements
    public static Map<String,String> contentElementMapDE; // selectors per domain to get content elements
    public static Map<String,String> contentElementMapES; // selectors per domain to get content elements

    static{
        documentTitleMap = new HashMap<>();
        documentTitleMap.put("poradnikzdrowie.pl","div.article_title");
        documentTitleMap.put("medonet.pl","div.articleTop");
        documentTitleMap.put("alzheimer-poznan.pl","h1");
        documentTitleMap.put("psychologiazdrowia.pl","h1");
        documentTitleMap.put("alzheimer-bw.de","#colcenter h1.csc-firstHeader");

        documentListMap = new HashMap<>();
        documentListMap.put("poradnikzdrowie.pl","div#main_column");
        documentListMap.put("medonet.pl","div.articleRight.intext");
        documentListMap.put("alzheimer-poznan.pl","h1");
        documentListMap.put("psychologiazdrowia.pl","h1");

        contentElementMapPL = new HashMap<>();
        contentElementMapPL.put("poradnikzdrowie.pl","div.text_article");
        contentElementMapPL.put("medonet.pl","div.articleRight.intext");
        contentElementMapPL.put("alzheimer-poznan.pl","#coll");
        contentElementMapPL.put("damy-rade.info","#training-field");
        contentElementMapPL.put("psychologiazdrowia.pl","div.td-post-content");
        contentElementMapPL.put("menudiabetyka.pl","div.post-entry");
        contentElementMapPL.put("portal.abczdrowie.pl",
                "div.secondary-font.article,div.grid-text," +
                        "section.grid-item-wide.article.secondary-font[role=article]"); 
        contentElementMapPL.put("slowoseniora.pl","div.entry-content");
        contentElementMapPL.put("medipe.pl","#blog-tekst");
        contentElementMapPL.put("promedica24.com.pl","body article");
        contentElementMapPL.put("oczymlekarze.pl","div.fulltext");
        contentElementMapPL.put("wylecz.to","div.news-content");
        contentElementMapPL.put("cafesenior.pl","div.post-content");
        contentElementMapPL.put("choroby-zdrowie.pl","div.entry.clearfix");
        contentElementMapPL.put("podyplomie.pl","div.article_body");
        contentElementMapPL.put("psychologia.edu.pl","div.artText");
        contentElementMapPL.put("forumneurologiczne.pl","div.text.wysiwyg");
        contentElementMapPL.put("bilobil.pl","body article");
        contentElementMapPL.put("choroby.senior.pl","#news_content_container");
        contentElementMapPL.put("naszsenior.pl","div.entrytext");
        contentElementMapPL.put("zdrowie.dziennik.pl","body article");
        contentElementMapPL.put("polki.pl","article.article");
        contentElementMapPL.put("opiekunki.aterima.pl","article.guide-item.guide-item-most-common-diseases");
        contentElementMapPL.put("seniovita.pl","#content");
        contentElementMapPL.put("zdrowie.gazeta.pl","#article_body");
        contentElementMapPL.put("poradnikprzedsiebiorcy.pl","div.body");
        contentElementMapPL.put("apteline.pl","div.article__content,div.topic-page__top-desc");
        contentElementMapPL.put("mowimyjak.se.pl","div.intextAd");
        contentElementMapPL.put("cukrzycapolska.pl","#content-data");

        contentElementMapDE = new HashMap<>();
        contentElementMapDE.put("alzheimer-bw.de","#colcenter");
        contentElementMapDE.put("deutsche-alzheimer.de","section.main-content");
        contentElementMapDE.put("wegweiser-demenz.de","#leftcontent");
        contentElementMapDE.put("pflege.de","div.text");
        contentElementMapDE.put("aok-gesundheitspartner.de","#content");
        contentElementMapDE.put("schlafgestoert.de","#content");
        contentElementMapDE.put("gesundheitsinformation.de","#center");
        contentElementMapDE.put("schlafzentrum-ruhrgebiet.de","#rechts");
        contentElementMapDE.put("ernaehrung.de","article");
        contentElementMapDE.put("diabetes-heute.uni-duesseldorf.de","#center");

        contentElementMapES = new HashMap<>();
        contentElementMapES.put("guiainfantil.com","div.post-description[itemprop=articleBody]");
        contentElementMapES.put("aeped.es","#content_documentos");
        contentElementMapES.put("vacunasaep.org","div.field.field-type-text.field-field-shared-body");
        contentElementMapES.put("fisterra.com","#textoContenido");
        contentElementMapES.put("enfamilia.aeped.es","div.field.field-name-body.field-type-text-with-summary.field-label-hidden");
//        contentElementMapES.put("medlineplus.gov","article");
        contentElementMapES.put("medlineplus.gov","#about-your-treat, #topic-summary");
    }
}
