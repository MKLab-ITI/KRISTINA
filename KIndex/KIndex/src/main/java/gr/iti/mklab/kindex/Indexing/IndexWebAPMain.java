package gr.iti.mklab.kindex.Indexing;

/**
 * Created by Thodoris Tsompanidis on 6/7/2016.
 */
public class IndexWebAPMain {

	public static void main(String[] args) {
		IndexWebAP iWebApp = new IndexWebAP();
		iWebApp.openWriters();
		iWebApp.doIndexing();
		iWebApp.closeWriters();
	}
}
