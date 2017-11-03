package gr.iti.mklab.simmo.core.segments;

import gr.iti.mklab.simmo.core.Segment;
import gr.iti.mklab.simmo.core.Segment.SEGMENT_METHOD;

public class SegmentFactory {


	//use getShape method to get object of type shape 
	public Segment getSegment(SEGMENT_METHOD segmentType){

		if(segmentType == null){
			return null;
		}		

		if(segmentType == SEGMENT_METHOD.LINEAR){
			return new LinearSegment();
		} 
		else if(segmentType == SEGMENT_METHOD.SPATIAL){
			return new SpatialSegment();
		} 
		else if(segmentType == SEGMENT_METHOD.TEMPORAL){
			return new TemporalSegment();
		}
		else if(segmentType == SEGMENT_METHOD.SPATIOTEMPORAL){
			return new SpatioTemporalSegment();
		}
		
		return null;
	}
}
