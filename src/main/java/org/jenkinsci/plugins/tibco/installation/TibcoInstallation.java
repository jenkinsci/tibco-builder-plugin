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

import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.EnvironmentSpecific;
import hudson.model.TaskListener;
import hudson.model.Node;
import hudson.remoting.Callable;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolProperty;
import hudson.tools.ToolInstallation;
import hudson.util.FormValidation;
import hudson.util.XStream2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import jenkins.model.Jenkins;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.jenkinsci.plugins.tibco.AmxEclipseAntBuilder;
import org.jenkinsci.plugins.tibco.AmxEclipseAntBuilder.DescriptorImpl;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.xml.sax.InputSource;

/**
 * 
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
public class TibcoInstallation extends ToolInstallation implements
EnvironmentSpecific<TibcoInstallation>, NodeSpecific<TibcoInstallation>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1010286355295860854L;

	@DataBoundConstructor
	public TibcoInstallation(String name, String home, List<? extends ToolProperty<?>> properties) {
		super(name, launderHome(home), properties);
		// TODO Auto-generated constructor stub
	}

	
	private static String launderHome(String home) {
        if(home.endsWith("/") || home.endsWith("\\")) {
            return home.substring(0,home.length()-1);
        } else {
            return home;
        }
    }
	
	public void buildEnvVars(EnvVars env) {
        env.put("TIBCO_HOME",getHome());
    }

    public TibcoInstallation forEnvironment(EnvVars environment) {
        return new TibcoInstallation(getName(), environment.expand(getHome()), getProperties().toList());
    }

    public TibcoInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new TibcoInstallation(getName(), translateFor(node, log), getProperties().toList());
    }

    
	public String getAmxEclipseAntExecutable(Launcher launcher) throws IOException, InterruptedException {
		return launcher.getChannel().call(new Callable<String,IOException>() {
            public String call() throws IOException {
                File exe = getAmxEclipseAntExeFile();
                if(exe.exists())
                    return exe.getPath();
                return null;
            }
        });
	}
    
	/**
     * Gets the executable path of this Tibco installation  on the given target system.
     */
    public String getStudioToolsExecutable(Launcher launcher) throws IOException, InterruptedException {
        return launcher.getChannel().call(new Callable<String,IOException>() {
            public String call() throws IOException {
                File exe = getStudioToolsExeFile();
                if(exe.exists())
                    return exe.getPath();
                return null;
            }
        });
    }

    private File getAmxEclipseAntExeFile() {
        String execName = Functions.isWindows() ? "amx_eclipse_ant.exe" : "amx_eclipse_ant";
        String home = Util.replaceMacro(getHome(), EnvVars.masterEnvVars);
        File exec = findTibcoExecutable(home,execName);
        
        return exec;
    }

    private File getStudioToolsExeFile() {
        String execName = Functions.isWindows() ? "studio-tools.exe" : "studio-tools";
        String home = Util.replaceMacro(getHome(), EnvVars.masterEnvVars);
        File exec = findTibcoExecutable(home,execName);
        return exec;
    }
    
    /**
     * find Tibco executable fo exec name. actually it finds only ant wrapper
     * TODO add a registry wher lookup for exec name and version
     * @param home
     * @param execName
     * @return File executable
     */
    private File findTibcoExecutable(String home, String execName) {
    	String installPath="";
    	File installInfo= new File(home, "_installInfo");
    	if(execName.startsWith("amx_eclipse_ant")){
    	
		WildcardFileFilter filter =new WildcardFileFilter("amx-design_*_prodInfo.xml");
		String [] list =installInfo.list(filter);
		
	    javax.xml.xpath.XPathFactory xpathFactory = javax.xml.xpath.XPathFactory.newInstance();
	    XPath xpath = xpathFactory.newXPath();
	    
	    try {
	    		//for(int i = 0; i<list.length;i++){
				InputSource source = new InputSource(new FileInputStream(new File(installInfo,list[0])));//TODO add combo with more exec version for installation
				installPath= (String)xpath.evaluate("/TIBCOInstallerFeatures/installerFeature[@name=\"sds-core\"]/assemblyList/assembly[@uid=\"product_tibco_com_tibco_amx_eclipse_ant\"]/@installLocation", source,XPathConstants.STRING);
				//String execVersion= (String)xpath.evaluate("/TIBCOInstallerFeatures/installerFeature[@name=\"sds-core\"]/assemblyList/assembly[@uid=\"product_tibco_com_tibco_amx_eclipse_ant\"]/@version", source,XPathConstants.STRING);
				//TibcoExecVersion version = new TibcoExecVersion(i, installPath, execName, execVersion);
				//this.addTibcoExecVersion(version);
	    		//}
	    } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    else{//TODO fixme it doesn't really good. i need to find a way to lookup a studio tools installation
    	
    	WildcardFileFilter filter =new WildcardFileFilter("businessevents-standard_*_prodInfo.xml");
		String [] list =installInfo.list(filter);
		

	    javax.xml.xpath.XPathFactory xpathFactory = javax.xml.xpath.XPathFactory.newInstance();
	    XPath xpath = xpathFactory.newXPath();
	    
	    try {
	    	//for(int i = 0; i<list.length;i++){
	    		installPath+=getHome();
	    		installPath+="/";
				InputSource source = new InputSource(new FileInputStream(new File(installInfo,list[0])));
				installPath+= (String)xpath.evaluate("/TIBCOInstallerFeatures/productDef[@compatDisplayName=\"TIBCO BusinessEvents\"]/@installDir", source,XPathConstants.STRING);
				installPath+="/studio/bin";			
				//String execVersion ="5.1";//(String)xpath.evaluate("/TIBCOInstallerFeatures/productDef/featureConfigInclude/include[@id=\"be-studio\"]/@version", source,XPathConstants.STRING);
				//TibcoExecVersion version = new TibcoExecVersion(i, installPath, execName, execVersion);
				//this.addTibcoExecVersion(version);
	    	//}
	    } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	    
		return new File(installPath,execName);
	}



	/**
     * Returns true if the executable exists.
     */
    public boolean getAmxEclipseAntExists() throws IOException, InterruptedException {
        return getAmxEclipseAntExecutable(new Launcher.LocalLauncher(TaskListener.NULL))!=null;
    }
	
	/**
     * Returns true if the executable exists.
     */
    public boolean getStudioToolsExists() throws IOException, InterruptedException {
        return getStudioToolsExecutable(new Launcher.LocalLauncher(TaskListener.NULL))!=null;
    }
    
    @Extension
    public static class DescriptorImpl extends ToolDescriptor<TibcoInstallation> {
    	
        @CopyOnWrite
        private volatile TibcoInstallation[] installations = new TibcoInstallation[0];

        @Override
        public String getDisplayName() {
            return "Tibco";//Messages.TibcoAntInstallation_DisplayName();
        }
        
        @Override
        public TibcoInstallation[] getInstallations() {
            return installations;
        }

        @Override
        public void setInstallations(TibcoInstallation... installations) {
            this.installations = installations;
            save();
        }

        /**
         * Used to load configuration
         * */
        public DescriptorImpl() {
            load();
        }

        /**
         * Checks if the Tibco Home is valid.
         */
        public FormValidation doCheckHome(@QueryParameter File value) {
            if(!Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER))
                return FormValidation.ok();

            if(value.getPath().equals(""))
                return FormValidation.ok();

            if(!value.isDirectory())
                return FormValidation.error("Not a valid directory");//Messages.TibcoAnt_NotADirectory(value));
            
            if(!isValidTibcoHome(value))
            	return FormValidation.error("NotATibcoHome");//Messages.TibcoAnt_NotATibcoHome(value));
            
            return FormValidation.ok();
        }

        private boolean isValidTibcoHome(File value) {
			String home =value.getPath();
			File installInfo= new File(value, "_installInfo");
			if(!installInfo.exists() && !installInfo.isDirectory())
				return false;
			WildcardFileFilter filter =new WildcardFileFilter("amx-design_*_prodInfo.xml");
			String [] list =installInfo.list(filter);
			if(list.length==0)
				return false;
			
			return true;
		}

		public FormValidation doCheckName(@QueryParameter String value) {
            return FormValidation.validateRequired(value);
        }
    }

    public static class ConverterImpl extends ToolConverter {
        public ConverterImpl(XStream2 xstream) { super(xstream); }
        @Override protected String oldHomeField(ToolInstallation obj) {
            return ((TibcoInstallation)obj).getHome();
        }
    }


}
    

