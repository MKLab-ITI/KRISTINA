package gr.iti.mklab.simmo.core.annotations;


import gr.iti.mklab.simmo.core.Annotation;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;


/**
 * @author kandreadou
 * @version 1.0.1
 * @since November 5, 2014
 */
@Entity
public class Concept implements Annotation {

	@Id
	public String id = new ObjectId().toString();


	public enum CONCEPT_MODALITY{TEXTUAL, VISUAL, HYBRID};


	/** The modality  of descriptor */
	private CONCEPT_MODALITY conceptModality;


	/** The name of the concept (URI or String) */
	private String concept;


	/** The score of the concept */
	private double score;


	public Concept(){ }


	public Concept(String concept, double score, CONCEPT_MODALITY conceptModality){
		this.concept = concept;
		this.score = score;
		this.conceptModality = conceptModality;
	}


	public void setScore(double score) {
		this.score = score;
	}

	public double getScore() {
		return score;
	}  



	public void setConcept(String concept) {
		this.concept = concept;
	}

	public String getConcept() {
		return concept;
	}



	public void setConceptModality(CONCEPT_MODALITY conceptModality) {
		this.conceptModality = conceptModality;
	}

	public CONCEPT_MODALITY getConceptModality() {
		return conceptModality;
	}

}
