package LexParser;

import RelExtFusion.RelExtConstants;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by spyridons on 8/24/2016.
 */
public class NounPhraseDetector {

    private Pattern regexForUnitexLabels;

    public NounPhraseDetector()
    {
        //build regex
        StringBuilder patternSB = new StringBuilder();
        patternSB.append("<(");
        //build pattern
        for (String tag: RelExtConstants.labels) {
            patternSB.append(tag+"|");
        }
        for (String tag: RelExtConstants.labels) {
            patternSB.append("/"+tag+"|");
        }
        for (String tag: RelExtConstants.labelsWithNum) {
            patternSB.append(tag+"|");
        }
        for (String tag: RelExtConstants.labelsWithNum) {
            patternSB.append("/"+tag+"|");
        }
        patternSB.append("EVAL1|/EVAL1|EVAL2|/EVAL2|Negation|/Negation|E1|/E1|E2|/E2|concept(.+?)|/concept|)>");
        String regexp = patternSB.toString();
        this.regexForUnitexLabels = Pattern.compile(regexp);
    }

    /***
     * clear all xml tags from a text
     * @param text
     * @return
     */
    private String clearXML(String text) {
        String textWithoutXML = text.replaceAll("<[^>]+>", "");
        return textWithoutXML;
    }

    /***
     * Clear only specific xml tags
     * @param text
     * @return
     */
    private String clearSpecificXML(String text) {

        StringBuffer sb = new StringBuffer();
        Matcher m = this.regexForUnitexLabels.matcher(text);

        while (m.find())
            m.appendReplacement(sb, "");
        m.appendTail(sb);

        String textWithoutXML = sb.toString();
        return textWithoutXML;
    }



    /***
     * return start and end position of a sentence in the text containing xml tags
     * @param textWithoutXML
     * @param sentenceStart
     * @param sentenceEnd
     * @param xmlIndices
     * @return
     */
    private int[] findStartEndIndicesInXML(String textWithoutXML, int sentenceStart, int sentenceEnd, HashMap<Integer,Integer> xmlIndices)
    {
        // 0:start 1:end
        int[] startEndIndices = {0,0};
        // get length of xml before phrase starts
        int xmlLengthBeforeSentence = 0;
        int xmlLengthInSentence = 0;
        String prev = textWithoutXML.substring(0,sentenceStart);
        int prevEnd = sentenceStart;
        SortedSet<Integer> keys = new TreeSet<>(xmlIndices.keySet());
        for (int key : keys) {
            if(key < prevEnd){
                int length = xmlIndices.get(key) - key;
                xmlLengthBeforeSentence += length;
                prevEnd += length;
            }
        }

        // get length of xml in phrase
        int textEnd = prevEnd + sentenceEnd - sentenceStart;
        for (int key : keys) {
            if(key >= prevEnd && key <= textEnd){
                int length = xmlIndices.get(key) - key;
                xmlLengthInSentence += length;
                textEnd += length;
            }
        }

        // calculate indices on text with xml, based on the length of all the xml tags and the indices on the text without xml
        startEndIndices[0] = sentenceStart + xmlLengthBeforeSentence;
        startEndIndices[1] = sentenceEnd + xmlLengthBeforeSentence + xmlLengthInSentence;

        return startEndIndices;
    }

    /***
     * extract number of noun phrases in a sentence
     * @param sentenceWithXML
     * @return
     */
    public Integer extractNounPhrasesOneSentence(String sentenceWithXML) {

        long start = System.currentTimeMillis();
//        System.out.println("START OF LEXICALIZED PARSER");
//        System.out.println("---------------------------");

        if(!(sentenceWithXML.endsWith(".")) && !(sentenceWithXML.endsWith("!")) && !(sentenceWithXML.endsWith("?"))){
            sentenceWithXML = sentenceWithXML + ".";
        }
        sentenceWithXML = sentenceWithXML.replace(".{S}",";{S}");
        String sentenceNoXML = clearSpecificXML(sentenceWithXML);

        // initiate stanford nlp pipeline (split sentences, extract POS tags and relations)
        SentenceWithTags sentenceWithTags = NLPFunctions.annotateSentence(sentenceNoXML, false, false);

        // get indices of E1 and E2 tags in sentence
        int fiE1 = sentenceWithXML.indexOf("<E1>");
        int liE1 = sentenceWithXML.lastIndexOf("</E1>");
        int fiE2 = sentenceWithXML.indexOf("<E2>");
        int liE2 = sentenceWithXML.lastIndexOf("</E2>");
        int liE1Size = "</E1>".length();
        int liE2Size = "</E2>".length();

        int phraseCount = 0;

        // if an E1 or E2 tag is missing no noun phrases are considered to exist
        if (fiE1 == -1 || fiE2 == -1 || liE1 == -1 || liE2 == -1) {
            return phraseCount;
        }



        if (liE2 < fiE1) {
            String prev = sentenceWithXML.substring(0, liE2 + liE2Size);
            String prevWithoutXML = clearSpecificXML(prev);
            String importantText = sentenceWithXML.substring(liE2 + liE2Size, fiE1);
            String importantTextWithoutXML = clearSpecificXML(importantText);

            // get indices of the important text (the one between E1 and E2 tags) in the text without xml tags
            int startOfSentence = prevWithoutXML.length();
            int endOfSentence = prevWithoutXML.length() + importantTextWithoutXML.length();

            // get important text and find noun phrases
            HashMap<Integer, WordWithTags> words = sentenceWithTags.getWords();
            for (Map.Entry<Integer, WordWithTags> entry : words.entrySet())
            {
                int key = entry.getKey();
                WordWithTags wwt = entry.getValue();
                // if word is a verb and exists inside the important text and does not follow another verb
                if(wwt.getBeginPos() > startOfSentence && wwt.getEndPos() < endOfSentence
                        && LexParserConstants.verbs.contains(wwt.getPosTag())
                        && hasNotModalOrPastTense(words.get(key - 1),wwt)){
                    // check two words before the verb to find subjects
                    wordWindowLoop:
                    for(int j = 1; j<= LexParserConstants.wordsBeforeVerb; j++) {
                        int newKey = key - j;
                        // there must exist a word and this word must exist in the important text
                        if( newKey > 0 && words.get(newKey).getBeginPos() > startOfSentence ){
                            for (String subj : LexParserConstants.subjects) {
                                if(words.get(newKey).getDependencies().contains(subj))
                                {
                                    phraseCount += 1;
                                    break wordWindowLoop;
                                }
                            }
                        }
                    }
                }
            }

        } else if (liE1 < fiE2) {
            String prev = sentenceWithXML.substring(0, liE1 + liE1Size);
            String prevWithoutXML = clearSpecificXML(prev);
            String importantText = sentenceWithXML.substring(liE1 + liE1Size, fiE2);
            String importantTextWithoutXML = clearSpecificXML(importantText);

            // get indices of the important text (the one between E1 and E2 tags) in the text without xml tags
            int startOfSentence = prevWithoutXML.length();
            int endOfSentence = prevWithoutXML.length() + importantTextWithoutXML.length();

            // get sentence and find noun phrases
            HashMap<Integer, WordWithTags> words = sentenceWithTags.getWords();
            for (Map.Entry<Integer, WordWithTags> entry : words.entrySet()) {
                int key = entry.getKey();
                WordWithTags wwt = entry.getValue();
                // if word is a verb and exists inside the important text and does not follow another verb
                if (wwt.getBeginPos() > startOfSentence && wwt.getEndPos() < endOfSentence
                        && LexParserConstants.verbs.contains(wwt.getPosTag())
                        && hasNotModalOrPastTense(words.get(key - 1),wwt)) {
                    wordWindowLoop:
                    for (int j = 1; j <= LexParserConstants.wordsBeforeVerb; j++) {
                        // check two words before the verb to find subjects
                        int newKey = key - j;
                        // there must exist a word and this word must exist in the important text
                        if (newKey > 0 && words.get(newKey).getBeginPos() > startOfSentence) {
                            for (String subj : LexParserConstants.subjects) {
                                if (words.get(newKey).getDependencies().contains(subj)) {
                                    phraseCount += 1;
                                    break wordWindowLoop;
                                }
                            }
                        }
                    }
                }
            }
        }


//                            System.out.println(i + ".\t[" + phraseCount + "]\t" + sentenceWithTags.getText() );
            // write counted noun phrases


//        System.out.println("DONE");

        // print elapsed time
        long elapsed = System.currentTimeMillis() - start;
        double elapsedSecs = elapsed / 1000.0;
//        System.out.println("Elapsed time (seconds): " + elapsedSecs);
//
//
//
//
//        System.out.println("-------------------------");
//        System.out.println("END OF LEXICALIZED PARSER");
        return phraseCount;
    }

    /***
     * test if a verb follows on another verb, if so, they must be counted as one (and the noun phrases too)
     * @param former
     * @param latter
     * @return
     */
    boolean hasNotModalOrPastTense(WordWithTags former, WordWithTags latter){
        if(former == null)
            return false;
        String[] latterTags = {"VB", "VBN", "VBG"};
        String[] formerTags = {"MD", "VBD"};
        String posFormer = former.getPosTag();
        String posLatter = latter.getPosTag();
        if(Arrays.asList(formerTags).contains(posFormer) && Arrays.asList(latterTags).contains(posLatter)){
//            System.out.println();
//            former.printWord();
//            latter.printWord();
//            System.out.println();
            return false;
        }
        else{
            return true;
        }
    }


}
