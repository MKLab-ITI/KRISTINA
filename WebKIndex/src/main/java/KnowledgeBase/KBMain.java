package KnowledgeBase;

import java.io.IOException;

/**
 * Created by spyridons on 10/12/2016.
 */
public class KBMain {
    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        KBHandler kbh = new KBHandler();
        boolean success = false;
        if(kbh.getQueries()==null)
            System.out.println("Empty query. Cannot start pipeline!!!");
        else
            success = kbh.generateInputKB();
//            success = kbh.generateConceptFiles();
        if(success)
            System.out.println("Success: Input file for KB is updated!!!");
        else
            System.out.println("Failure: Input file for KB is NOT updated!!!");
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Elapsed time: " + elapsed);
    }
}
