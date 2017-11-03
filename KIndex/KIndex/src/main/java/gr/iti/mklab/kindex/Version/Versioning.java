package gr.iti.mklab.kindex.Version;

import java.util.ArrayList;

/**
 * Class created for storing KIndex current version and version history.
 * It can print all versions, with version number, date and description
 */
public class Versioning {

	private ArrayList<VersionItem> items = new ArrayList<VersionItem>();

	/**
	 * In constructor versionItems are created
	 * First item is current version
	 * If you wish to add new version, add a VersionItem at the top of items ArrayLis	t.
	 */
	public Versioning() {
		//when adding VersionItem, Separate 3rd parameter (description) with "|" character for printing in different lines.
		items.add(new VersionItem("1.0.0","2017-03-01","First version."));
	}

	public void printCurrentVersionDetails(){
		VersionItem item = items.get(0);
		item.printVersionDetailed();
	}
	public void printCurrentVersion(){
		VersionItem item = items.get(0);
		item.printVersionDetailed();
	}

	public void printVersionAll(){
		for(VersionItem item : items) {
			item.printVersionDetailed();
		}
	}
}
