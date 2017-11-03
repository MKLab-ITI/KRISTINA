package gr.iti.mklab.kindex.Version;

/**
 * Class for storing versions.
 */
public class VersionItem {

	private String version;
	private String date;
	private String description;

	public VersionItem(String version, String date, String descreption) {
		this.version = version;
		this.date = date;
		this.description = descreption;
	}

	public String getDescription() {

		return description;
	}

	public void setDescription(String descreption) {
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

	public void printVersion() {
		System.out.println("Current Version: "+getVersion());
	}

	public void printVersionDetailed(){
		String[] desc=description.split("--");
		int i = 0;
		System.out.println(" Version: " + version + ", Date: " + date + ", Description: - " + desc[i]);
		i++;
		while(i<desc.length) {
			System.out.println("                                                - " + desc[i]);
			i++;
		}
		System.out.println(" " );
	}
}
