package LexParser;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

import java.io.IOException;
import java.util.*;

/**
 * Created by spyridons on 10/7/2016.
 */
public class NLPFunctions {

    public static HashMap<Integer,SentenceWithTags> annotateText(String text) throws IOException {
        HashMap<Integer,SentenceWithTags> sentencesWithTagsMap= new HashMap<>();
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, parse");
        props.setProperty("ssplit.eolonly", "true");
        props.put("parse.model", "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

        // shut off the annoying intialization messages
        RedwoodConfiguration.empty().capture(System.err).apply();

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // enable stderr again
        RedwoodConfiguration.current().clear().apply();

        Annotation document = new Annotation(text);
        // run all Annotators on this text
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        int id = 1;
        System.out.println("Number of sentences detected: " + sentences.size());
        for (CoreMap sentence : sentences) {
            HashMap<Integer, WordWithTags> sentenceMap = new HashMap<>();

            // get sentence info
            int sentenceBeginPos = sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
            int sentenceEndPos = sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
            String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // get word info
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String posTag = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                int beginPos = token.beginPosition();
                int endPos = token.endPosition();
                int index = token.index();

                // create word object
                WordWithTags wwt = new WordWithTags();
                wwt.setText(word);
                wwt.setPosTag(posTag);
                wwt.setBeginPos(beginPos);
                wwt.setEndPos(endPos);
                ArrayList<String> dependencies = new ArrayList<>();
                wwt.setDependencies(dependencies);

                // add word to sentence
                sentenceMap.put(index,wwt);
            }

            // calculate dependencies for the current sentence
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            TreebankLanguagePack tlp = new PennTreebankLanguagePack();
            GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
            GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
            Collection<TypedDependency> td = gs.typedDependencies();
//            Collection<TypedDependency> td = gs.typedDependenciesCollapsedTree();
//            System.out.println(GrammaticalStructure.dependenciesToString(gs, td, tree, false, false));
            Map<Integer, Integer> indexToPos = Generics.newHashMap();
            indexToPos.put(0,0); // to deal with the special node "ROOT"
            List<Tree> gsLeaves = gs.root().getLeaves();
            for (int i = 0; i < gsLeaves.size(); i++) {
                TreeGraphNode leaf = (TreeGraphNode) gsLeaves.get(i);
                indexToPos.put(leaf.label().index(), i + 1);
            }
            for(TypedDependency tdp: td){
                String relation = tdp.reln().toString();
                int wordIndex = Integer.parseInt(indexToPos.get(tdp.dep().index()) + tdp.dep().toPrimes());
                sentenceMap.get(wordIndex).addDependency(relation);
//                System.out.println(relation);
            }

            // create sentence object
            SentenceWithTags swt = new SentenceWithTags();
            swt.setBeginPos(sentenceBeginPos);
            swt.setEndPos(sentenceEndPos);
            swt.setText(sentenceText);
            swt.setWords(sentenceMap);

            // add sentence object to the sentence map
            sentencesWithTagsMap.put(id,swt);
            ++id;
        }
        return sentencesWithTagsMap;
    }

    /***
     * annotates a single sentence (also used for service)
     * @param text
     * @return
     * @throws IOException
     */
    public static SentenceWithTags annotateSentence(String text, boolean whitespaceSplit, boolean lemma) {

        HashMap<Integer,SentenceWithTags> sentencesWithTagsMap= new HashMap<>();
        Properties props = new Properties();
        String annotators = "tokenize, ssplit, pos, parse";
        if(lemma)
            annotators = "tokenize, ssplit, pos, lemma, parse";
        props.setProperty("annotators", annotators);
        props.setProperty("ssplit.eolonly", "true");
        props.put("parse.model", "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

        if(whitespaceSplit)
            props.setProperty("tokenize.whitespace","true");

        // shut off the annoying intialization messages
        RedwoodConfiguration.empty().capture(System.err).apply();

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // enable stderr again
        RedwoodConfiguration.current().clear().apply();

        Annotation document = new Annotation(text);
        // run all Annotators on this text
        pipeline.annotate(document);

        CoreMap doc = document.get(CoreAnnotations.SentencesAnnotation.class).get(0);

        HashMap<Integer, WordWithTags> sentenceMap = new HashMap<>();

        // get sentence info
        String sentenceText = doc.get(CoreAnnotations.TextAnnotation.class);
        for (CoreLabel token : doc.get(CoreAnnotations.TokensAnnotation.class)) {
            // get word info
            String word = token.get(CoreAnnotations.TextAnnotation.class);
            String posTag = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            int beginPos = token.beginPosition();
            int endPos = token.endPosition();
            int index = token.index();

            // create word object
            WordWithTags wwt = new WordWithTags();
            wwt.setText(word);
            wwt.setPosTag(posTag);
            wwt.setBeginPos(beginPos);
            wwt.setEndPos(endPos);
            if(lemma)
                wwt.setLemma(token.get(CoreAnnotations.LemmaAnnotation.class));
            ArrayList<String> dependencies = new ArrayList<>();
            wwt.setDependencies(dependencies);

            // add word to sentence
            sentenceMap.put(index,wwt);
        }

        // calculate dependencies for the current sentence
        Tree tree = doc.get(TreeCoreAnnotations.TreeAnnotation.class);
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
        Collection<TypedDependency> td = gs.typedDependencies();
        Map<Integer, Integer> indexToPos = Generics.newHashMap();
        indexToPos.put(0,0); // to deal with the special node "ROOT"
        List<Tree> gsLeaves = gs.root().getLeaves();
        for (int i = 0; i < gsLeaves.size(); i++) {
            TreeGraphNode leaf = (TreeGraphNode) gsLeaves.get(i);
            indexToPos.put(leaf.label().index(), i + 1);
        }
        for(TypedDependency tdp: td){
            String relation = tdp.reln().toString();
            int wordIndex = Integer.parseInt(indexToPos.get(tdp.dep().index()) + tdp.dep().toPrimes());
            sentenceMap.get(wordIndex).addDependency(relation);
//                System.out.println(relation);
        }

        // create sentence object
        SentenceWithTags swt = new SentenceWithTags();
        swt.setText(sentenceText);
        swt.setWords(sentenceMap);

        return swt;

    }
}
