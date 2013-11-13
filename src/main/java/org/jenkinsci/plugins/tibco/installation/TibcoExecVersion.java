/**
 * The MIT License
 * 
 * Copyright (c) 2013 - 2014, Federico Pastore
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE. 
 */
package org.jenkinsci.plugins.tibco.installation;


/**
 * @author Federico Pastore - federico.pastore@gmail.com
 * 
 * 
 * 
 * Disclaimer -  TIBCO, the TIBCO logo and 
 * TIBCO Software are trademarks or 
 * registered trademarks of TIBCO Software 
 * Inc. in the United States and/or other 
 * countries. All other product and company 
 * names and marks mentioned are the property of their respective 
 * owners.
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
