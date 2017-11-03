package gr.iti.mklab.simmo.core;


import gr.iti.mklab.simmo.core.items.Audio;
import gr.iti.mklab.simmo.core.items.Image;
import gr.iti.mklab.simmo.core.items.Text;
import gr.iti.mklab.simmo.core.items.Video;

/**
 *
 * @author amoumtzidou
 * @version 1.0.0
 * @since July 8, 2014
 */

//public abstract class Segment extends Annotatable {
public abstract class Segment extends Item { // CHANGED FOR MULTISENSOR
	
	/** The unique internal Item id */
	//@Id
	//public String id = new ObjectId().toString();
	
	
	/** Types of frame blocks */
    public enum FRAMES_BLOCK_TYPE{SHOT, SCENE, UNDEFINED};

    
    /** The frame block type */
    FRAMES_BLOCK_TYPE framesBlockType = FRAMES_BLOCK_TYPE.UNDEFINED;

	
    /** Method used for segmenting */
    public enum SEGMENT_METHOD{LINEAR, SPATIAL, TEMPORAL, SPATIOTEMPORAL};

    
    /** Type segment */
    public Item segment;
    
    
    /** The first frame of the segment */
    public int firstFrame = 0;

    
    /** The last frame of the segment */
    public int lastFrame = 0;
    
    public Segment(){}
    
    public Segment(Image img){
    	segment = img;
    }
    
    public Segment(Video vid){
    	segment = vid;
    }
    
    public Segment(Audio aud){
    	segment = aud;
    }
    
    public Segment(Text txt){
    	segment = txt;
    }
    
    
    public FRAMES_BLOCK_TYPE getFRAMES_BLOCK_TYPE() {
        return framesBlockType;
    }

    public void setFRAMES_BLOCK_TYPE(FRAMES_BLOCK_TYPE framesBlockType) {
        this.framesBlockType = framesBlockType;
    }

    public int getFirstFrame() {
        return firstFrame;
    }

    public void setFirstFrame(int firstFrame) {
        this.firstFrame = firstFrame;
    }
    
    public int getLastFrame() {
        return lastFrame;
    }

    public void setLastFrame(int lastFrame) {
        this.lastFrame = lastFrame;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		 this.id = id;	
	}
  
    
    
}
