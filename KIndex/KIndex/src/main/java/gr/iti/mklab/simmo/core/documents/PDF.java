package gr.iti.mklab.simmo.core.documents;

import gr.iti.mklab.simmo.core.Document;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.util.ArrayList;
import java.util.List;


/**
 * A PDF is a {@link Document}. It may contain other {@link gr.iti.mklab.simmo.core.Item} objects.
 *
 * @author thtsompanidis
 * @version 1.0.0
 * @see Document
 * @since January 27, 2016
 */
@Entity
public class PDF extends Document {

    public PDF() {}

}
