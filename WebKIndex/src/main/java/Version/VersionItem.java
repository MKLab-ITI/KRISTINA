package Version;

/**
 * Class for storing versions.
 */
public class VersionItem {

	private String version;
	private String date;
	private String[] description;

	public VersionItem(String version, String date, String[] descreption) {
		this.version = version;
		this.date = date;
		this.description = descreption;
	}

	public String[] getDescription() {

		return description;
	}

	public void setDescription(String[] descreption) {
		this.description = descreption;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}


}
