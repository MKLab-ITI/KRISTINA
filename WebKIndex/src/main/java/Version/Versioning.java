package Version;

import java.util.ArrayList;
import java.util.List;

/**
 * Class created for storing KIndex current version and version history.
 * It can print all versions, with version number, date and description
 */
public class Versioning {

	private ArrayList<VersionItem> items = new ArrayList<VersionItem>();

	/**
	 * In constructor versionItems are created
	 * First item is current version
	 * If you wish to add new version, add a VersionItem at the top of items ArrayList.
	 */
	public Versioning() {
		items.add(new VersionItem("1.0.0","2017-10-05",new String[] {""}));
		
	}


	public VersionItem getCurernt(){
		return items.get(0);
	}

	public List<VersionItem> getPreviousVersions(){
		items.remove(0);
		return items;
	}
}
