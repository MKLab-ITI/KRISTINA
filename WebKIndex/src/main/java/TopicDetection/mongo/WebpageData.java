package TopicDetection.mongo;

import java.util.Map;

public class WebpageData {

	Map<String, String> textIdDocIdMap = null;
	Map<String, String> docIdDocTitleMap = null;
	Map<String, String> docIdDocDateMap = null;
	
	public WebpageData(){}
	public WebpageData(Map<String, String> textIdDocIdMap, Map<String, String> docIdDocTitleMap, Map<String, String> docIdDocDateMap){
		this.textIdDocIdMap = textIdDocIdMap;
		this.textIdDocIdMap = textIdDocIdMap;
		this.docIdDocDateMap = docIdDocDateMap;
	}
	
	public void setTextIdDocIdMap(Map<String, String> textIdDocIdMap){
		this.textIdDocIdMap = textIdDocIdMap;
	}
	
	public void setDocIdDocTitleMap(Map<String, String> docIdDocTitleMap){
		this.docIdDocTitleMap = docIdDocTitleMap;
	}
	
	public void setDocIdDocDateMap(Map<String, String> docIdDocDateMap){
		this.docIdDocDateMap = docIdDocDateMap;
	}
	
	public Map<String, String> getTextIdDocIdMap(){ return textIdDocIdMap; }
	public Map<String, String> getDocIdDocTitleMap(){ return docIdDocTitleMap; }
	public Map<String, String> getDocIdDocDateMap(){ return docIdDocDateMap; }
}
