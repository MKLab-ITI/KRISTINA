package gr.iti.mklab.simmo.core.annotations;

import gr.iti.mklab.simmo.core.Annotation;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;


/**
 * @author amoumtzid
 * @version 1.0.1
 * @since October 20, 2015
 */
@Entity
public class NamedEntities implements Annotation {


	@Id
	public String id = new ObjectId().toString();

	
	public List<NamedEntity> namedEntitiesList;
	

	public List<NamedEntity> getNamedEntitiesList() {
		return namedEntitiesList;
	}

	public void setNamedEntitiesList(List<NamedEntity> namedEntitiesList) {
		this.namedEntitiesList = namedEntitiesList;
	}


	
}
