/**
 * 
 */
package org.jenkinsci.plugins.tibco.installation;

/**
 * @author federicopastore
 *
 */
public class TibcoExecVersion {

	private String id;
	private String installPath;
	private String execName;
	private String execVersion;
	private String description;

	public TibcoExecVersion(int i, String installPath, String execName,
			String execVersion) {
		this.id= "i";
		this.installPath= installPath;
		this.execName = execName;
		this.execVersion= execVersion;
		this.description= this.execName.concat(" - version ").concat(this.execVersion).concat(" - installed on ").concat(this.installPath);
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

}
