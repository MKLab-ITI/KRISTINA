package MetaMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nih.nlm.nls.metamap.*;
import org.json.JSONArray;
import org.json.JSONObject;
import se.sics.prologbeans.PBTerm;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Thodoris Tsompanidis on 17/5/2016.
 */
public class LocalMetaMapHandler {

	public static String getAnnotatedXMLTags (String text){

		//remove the empty lines from the text
		text = text.replaceAll("(?m)^[ \\t]*\\r?\\n", "");

		int charOffset = 0; // the number of characters added during this procedure (tags) and they are not calculated in initial MetaMap annotation
		//int nlChar = 0; //the number of new line characters. MetaMap does not count them during annotation, so they have to be added manually
		int minPos = -1;
		HashSet<Integer> startPos = new HashSet<Integer>();


		List<Result> resultList = null;
		boolean metamapWorked = false;
		while (!metamapWorked) {
			try {
				MetaMapApi api = new MetaMapApiImpl(MetaMapCONSTANTS.METAMAP_LOCAL_IP);
				api.setOptions("-J acab,anab,antb,bact,bodm,comd,clnd,cgab,diap,dsyn,drdd,food,inpo,lbpr,medd,mobd,neop,patf,podg,phsu,sosy,topp,virs");
				resultList = api.processCitationsFromString(text);
				metamapWorked = true;
				api.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				try {Thread.sleep(1000);} catch(InterruptedException ex) {} //wait for one second
				System.out.println(" ");
				System.out.println("MetaMap Not Working!!!");
				System.out.println("If MetaMap is not running in the local installation, start it.");
				System.out.println("When MetaMap is running again, press ENTER to continue.");
				try {
					System.in.read();
				} catch (IOException e1) {
					System.out.println("Could not read the input");
				}
			}
		}
		Multimap<String, String> concepts = ArrayListMultimap.create();

		try {
			for (Result result : resultList) {
				for (Utterance utterance : result.getUtteranceList()) {
					for (PCM pcm : utterance.getPCMList()) {
						for (Mapping map : pcm.getMappingList()) {
							for (Ev mapEv : map.getEvList()) {

								int initialTextLength = text.length();

								String semType = mapEv.getSemanticTypes().get(0); //get always the first one
								//later on the semTypes will be weighted
								String semLabel = getConceptLabel(semType);

								Position position = mapEv.getPositionalInfo().get(0);
								int start = position.getX();
								int length = position.getY();

								//check if the same Term is already annotated
								if ((!startPos.contains(start)) && (start > minPos) && semLabel != null) {

									minPos = start + length - 1;
									startPos.add(start);
									//charOffset: the number of characters added during this procedure (tags) and they are not calculated in initial MetaMap annotation
									//nlChar: the number of new line characters. MetaMap does not count them during annotation, so they have to be added manually

									//nlChar = text.substring(0, start + charOffset + nlChar).split(System.getProperty("line.separator")).length - 1;

									String prev = text.substring(0, start + charOffset /*+ nlChar*/);
									String conceptTerms = text.substring(start + charOffset/* + nlChar*/, start + length + charOffset /*+ nlChar*/);
									//String conceptTerms = mapEv.getConceptName().replaceAll("[\\*\\^]","");
									//if (mapEv.getMatchedWords().size()>1){
									//	System.out.println("sad");
									//}
									//int length = conceptTerms.length();
									String next = text.substring(start + length + charOffset /*+ nlChar*/, text.length());
									if (!conceptTerms.matches("[a-zA-Z0-9].*")) { //in case concept Term's first character is not a letter or number
										Pattern p = Pattern.compile("\\p{L}");
										Matcher m = p.matcher(conceptTerms);
										if (m.find()) {
											int letterPos = m.start();
											int offset = conceptTerms.substring(0, letterPos).length();
											prev = text.substring(0, start + charOffset /*+ nlChar */ + offset);
											conceptTerms = text.substring(start + charOffset /*+ nlChar */ + offset, start + length + charOffset /*+ nlChar*/ + offset);
											next = text.substring(start + length + charOffset /*+ nlChar */ + offset, text.length());
										}
									}

									//MetaMapHandler must not "cut" the words
									if ((prev.equals("") || prev.endsWith(" ")) && (next.startsWith(" ") || next.matches("\\p{Punct} .*") || next.startsWith("\r\n"))) {
										text = prev + "<concept MetaMap=\"" + semLabel + "\">" + conceptTerms + "</concept>" + next;
										charOffset += text.length() - initialTextLength;
										//System.out.println("Offset: " + charOffset);
										//System.out.println("New Text: ");
										//System.out.println(text);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}

	/**
	 * Get annotations in MetaMapConcept format
	 * @param text
	 * @return
	 */
	public static List<MetaMapConcept> getAnnotations (String text){

		List<MetaMapConcept> concepts = new ArrayList<>();

		//remove the empty lines from the text
		text = text.replaceAll("(?m)^[ \\t]*\\r?\\n", "");

		int charOffset = 0; // the number of characters added during this procedure (tags) and they are not calculated in initial MetaMap annotation
		//int nlChar = 0; //the number of new line characters. MetaMap does not count them during annotation, so they have to be added manually
		int minPos = -1;
		HashSet<Integer> startPos = new HashSet<Integer>();


		List<Result> resultList = null;
		boolean metamapWorked = false;
		while (!metamapWorked) {
			try {
				MetaMapApi api = new MetaMapApiImpl(MetaMapCONSTANTS.METAMAP_LOCAL_IP);
				api.setOptions("-J acab,anab,antb,bact,bodm,comd,clnd,cgab,diap,dsyn,drdd,food,inpo,lbpr,medd,mobd,neop,patf,podg,phsu,sosy,topp,virs");
				resultList = api.processCitationsFromString(text);
				metamapWorked = true;
				api.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				try {Thread.sleep(1000);} catch(InterruptedException ex) {} //wait for one second
				System.out.println(" ");
				System.out.println("MetaMap Not Working!!!");
				System.out.println("If MetaMap is not running in the local installation, start it.");
				System.out.println("When MetaMap is running again, press ENTER to continue.");
				try {
					System.in.read();
				} catch (IOException e1) {
					System.out.println("Could not read the input");
				}
			}
		}

		try {
			for (Result result : resultList) {
				for (Utterance utterance : result.getUtteranceList()) {
					for (PCM pcm : utterance.getPCMList()) {
						for (Mapping map : pcm.getMappingList()) {
							for (Ev mapEv : map.getEvList()) {

								int initialTextLength = text.length();

								String semType = mapEv.getSemanticTypes().get(0); //get always the first one
								//later on the semTypes will be weighted
								String semLabel = getConceptLabel(semType);

								Position position = mapEv.getPositionalInfo().get(0);
								int start = position.getX();
								int length = position.getY();

								MetaMapConcept concept = new MetaMapConcept(start, length, semLabel);
								concepts.add(concept);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return concepts;
	}

	/**
	 * Call local semrep installation for relation extraction 
	 * @param text
	 * @return
	 */
	public static String getSemRepOutput(String text) {
		try {
			String url = "http://"+MetaMapCONSTANTS.METAMAP_LOCAL_IP+":"+MetaMapCONSTANTS.SemRep_LoCAL_PORT+"/"+MetaMapCONSTANTS.SemRep_LoCAL_PATH;
			URL obj = new URL(url);

			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			//add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			String urlParameters = MetaMapCONSTANTS.SemRep_LoCAL_TEXT_PARAMETER +"="+text;

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			//int responseCode = con.getResponseCode();
			//System.out.println("\nSending 'POST' request to URL : " + url);
			//System.out.println("Post parameters : " + urlParameters);
			//System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			//print result
			//System.out.println(response.toString());
			return response.toString();
		} catch (java.io.IOException e) {
			System.out.println("KIndex :: LocalMetaMapHandler.getSemRepOutput() IOException e");
			e.printStackTrace();
			return "";
		}
	}


	public static JSONObject getConceptsasJson(String text){
		text = text.replaceAll("[^\\x00-\\x7F]", "");

		try {
			MetaMapApi api = new MetaMapApiImpl(MetaMapCONSTANTS.METAMAP_LOCAL_IP);
			api.setOptions("-J acab,anab,antb,bact,bodm,comd,clnd,cgab,diap,dsyn,drdd,food,inpo,lbpr,medd,mobd,neop,patf,podg,phsu,sosy,topp,virs");
			List<Result> resultList = api.processCitationsFromString(text);
			Multimap<String,String> concepts = ArrayListMultimap.create();

			for (Result result : resultList) {
				for (Utterance utterance: result.getUtteranceList()) {
					for (PCM pcm: utterance.getPCMList()){
						for (Mapping map: pcm.getMappingList()) {
							for (Ev mapEv: map.getEvList()) {

								String prefix = "";
								String wordsString = "";
								List<String> words = mapEv.getMatchedWords();
								for (String word : words) {
									wordsString += prefix + word;
									prefix = " ";
								}
								String semType = mapEv.getSemanticTypes().get(0); //get always the first one
								//later on the semTypes will be weighted


								String semLabel = getConceptLabel(semType);
								if (semLabel != null) {
									concepts.put(semLabel, wordsString);
								}
							}
						}
					}
				}
			}

			JSONObject obj = new JSONObject();
			//convert to json Object
			for (String key : concepts.keys()) {
				JSONArray array = new JSONArray();
				Collection<String> collection = concepts.get(key);
				for (String c : collection) {
					if(!array.toString().contains("\""+c+"\"")) {
						array.put(c);
					}
				}
				obj.put(key,array);
			}

			return obj;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	private static String getConceptLabel(String concept){
		switch (concept) {
			//acab,anab,antb,bact,bodm,comd,clnd,cgab,diap,dsyn,drdd,food,inpo,lbpr,medd,mobd,neop,patf,podg,phsu,sosy,topp,virs
			case "acab":
				return "AcquiredAbnormality";
			case "anab":
				return "AnatomicalAbnormality";
			case "antb":
				return "Antibiotic";
			case "bact":
				return "Bacterium";
			case "bodm":
				return "BiomedicalOrDentalMaterial";
			case "comd":
				return "CellOrMolecularDysfunction";
			case "clnd":
				return "ClinicalDrug";
			case "cgab":
				return "CongenitalAbnormality";
			case "diap":
				return "DiagnosticProcedure";
			case "dsyn":
				return "DiseaseOrSyndrome";
			case "drdd":
				return "DrugDeliveryDevice";
			case "food":
				return "Food";
			case "inpo":
				return "InjuryOrPoisoning";
			case "lbpr":
				return "LaboratoryProcedure";
			case "medd":
				return "MedicalDevice";
			case "mobd":
				return "MentalOrBehavioralDysfunction";
			case "neop":
				return "NeoplasticProcess";
			case "patf":
				return "PathologicFunction";
			case "podg":
				return "PatientOrDisabledGroup";
			case "phsu":
				return "PharmacologicSubstance";
			case "sosy":
				return "SignOrSymptom";
			case "topp":
				return "TherapeuticOrPreventiveProcedure";
			case "virs":
				return "Virus";
		}
		return null;
	}
}
