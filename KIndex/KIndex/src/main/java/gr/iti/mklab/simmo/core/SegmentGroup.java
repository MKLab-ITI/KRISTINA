package gr.iti.mklab.simmo.core;


import java.util.ArrayList;
import java.util.List;


public class SegmentGroup extends Segment {

	
    /**
     * A list of Segments that the SegmentGroup contains
     */
    @org.mongodb.morphia.annotations.Reference
    protected List<Segment> segments = new ArrayList<Segment>();


    
}