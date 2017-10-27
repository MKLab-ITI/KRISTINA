package gr.iti.kristina.topicflow.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.hp.hpl.jena.ontology.OntClass;

import gr.iti.kristina.test.testcases.Output;
import gr.iti.kristina.topicflow.ThemeKB;
import marytts.MaryInterface;
import marytts.client.RemoteMaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.data.audio.MaryAudioUtils;

public class LanguageGeneration implements LineListener {

	static final String NAME = "txt2wav";
	static final String IN_OPT = "input";
	static final String OUT_OPT = "output";

	Clip clip;

	ThemeKB themesKB;

	Set<Output> outputs;

	public LanguageGeneration(Set<Output> outputs, ThemeKB themesKB) {
		this.outputs = outputs;
		this.themesKB = themesKB;
	}

	private String getSpokenText(OntClass topic) {
		return themesKB.getSpokenText(topic) + ". ";
	}

	private String getSpokenTexts(Set<OntClass> topics, String join) {
		List<String> sentences = new ArrayList<>();
		for (OntClass ontClass : topics) {
			sentences.add(themesKB.getSpokenText(ontClass));
		}
		return String.join(join, sentences) + ". ";
	}

	private String getSentence(Output o) {
		String sentence = "";
		if (o.type.equals("STATEMENT")) {
			sentence += "Here is what I got from the KB about " + getSpokenText(o.topics.iterator().next());
		} else if (o.type.equals("IR")) {
			sentence += "Here is what I found on the Web about " + getSpokenText(o.topics.iterator().next());
		} else if (o.type.equals("CLARIFICATION")) {
			sentence += "I am not so sure what you mean. Do you want to talk about " + getSpokenTexts(o.topics, " or ");
		} else if (o.type.equals("PROACTIVE")) {
			sentence += "I can also talk about " + getSpokenTexts(o.topics, " and ");
		} else if (o.type.equals("ADDITIONAL")) {
			sentence += "Here is some further info about " + getSpokenText(o.topics.iterator().next());
		} else if (o.type.equals("SPECIFYINGMOREINFORMATION")) {
			sentence += "I can talk about " + getSpokenTexts(o.topics, " and ");
		}

		else if (o.type.equals("UNKNOWN")) {
			sentence += "Sorry, I do not understand what you mean. Can you please repeat?";
		} else if (o.type.equals("TEXT")) {
			sentence += themesKB.getText(o.topics.iterator().next()) + ". ";
		} else if (o.type.equals("NOTFOUND")) {
			sentence += "Sorry, I cannot find an answer.";
		} else if (o.type.equals("URL")) {
			sentence += "Here is a link about the topic.";
		} else {
			sentence += "Here is some information.";
		}
		return sentence;
	}

	public String getSentences() {
		String sentence = "";
		int size = outputs.size();

		if (size == 0) {
			sentence += "Sorry, I cannot find an answer. ";
		} else if (size > 1) {
//			sentence += "I have found " + size + " responses. ";
			for (Output o : outputs) {
				sentence += getSentence(o);
			}
		} else {
			for (Output o : outputs) {
				sentence += getSentence(o);
			}
		}
		System.err.println(sentence);
		return sentence;
	}

	public void play() throws MaryConfigurationException, SynthesisException, IOException,
			InterruptedException, LineUnavailableException, UnsupportedAudioFileException {

		MaryInterface marytts = new RemoteMaryInterface();
		
		System.out.println("I currently have " + marytts.getAvailableVoices() + " voices in "
			    + marytts.getAvailableLocales() + " languages available.");
			System.out.println("Out of these, " + marytts.getAvailableVoices(Locale.US) + " are for US English.");
		
		marytts.setVoice("dfki-prudence");
		AudioInputStream audio = marytts.generateAudio(getSentences());
		MaryAudioUtils.writeWavFile(MaryAudioUtils.getSamplesAsDoubleArray(audio), "lg.wav", audio
				.getFormat());
		// MaryAudioUtils.playWavFile("lg.wav", 2);
		// Thread.sleep(2000);
		_play();
		// Thread.sleep(2000);
	}

	private void _play() throws LineUnavailableException, IOException, UnsupportedAudioFileException,
			InterruptedException {
		AudioInputStream ais = AudioSystem.getAudioInputStream(new File("lg.wav"));
		clip = AudioSystem.getClip();
		clip.addLineListener(this);
		clip.open(ais);
		clip.start();
		waitUntilDone();
	}

	// public static void main(String[] args) throws MaryConfigurationException,
	// SynthesisException, IOException,
	// InterruptedException, LineUnavailableException,
	// UnsupportedAudioFileException {
	// new LanguageGeneration().generate(
	// "Hello George!Hello George!");
	//
	// }

	public synchronized void waitUntilDone() {
		try {
			this.wait();

		} catch (InterruptedException ignore) {

		}
	}

	@Override
	public synchronized void update(LineEvent le) {
		LineEvent.Type type = le.getType();
		if (type == LineEvent.Type.OPEN) {
			System.out.println("OPEN");

		} else if (type == LineEvent.Type.CLOSE) {
			System.out.println("CLOSE");
			this.notifyAll();
		} else if (type == LineEvent.Type.START) {
			System.out.println("START");

		} else if (type == LineEvent.Type.STOP) {
			System.out.println("STOP");
			clip.close();
		}

	}
}