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
package org.jenkinsci.plugins.tibco;

import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tools.ToolInstallation;
import hudson.util.ArgumentListBuilder;
import hudson.util.VariableResolver;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jenkinsci.plugins.tibco.installation.TibcoInstallation;
import org.jenkinsci.plugins.tibco.studiotools.StudioToolsConsoleAnnotator;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author federicopastore
 *
 */
public class StudioToolsBuilder extends Builder {

	
	private final String name;
	private final String operation;
	private final String projectDir;
	private final String outputArchiveFile;
	
	private final String extendedClassPath;

	
	private final String TRAPropertyFile;
	private final boolean overwriteOutput;







	@DataBoundConstructor
	public StudioToolsBuilder(String name, String operation, String projectDir,String outputArchiveFile, String extendedClassPath, boolean overwriteOutput, String TRAPropertyFIle){
		this.name=Util.fixEmptyAndTrim(name);
		this.operation=Util.fixEmptyAndTrim(operation);
		this.projectDir=Util.fixEmptyAndTrim(projectDir);
		this.outputArchiveFile=Util.fixEmptyAndTrim(outputArchiveFile);
		this.extendedClassPath=Util.fixEmptyAndTrim(extendedClassPath);
		this.overwriteOutput=overwriteOutput;
		this.TRAPropertyFile=Util.fixEmptyAndTrim(TRAPropertyFIle);

	}
	
	
	
	public boolean isOverwriteOutput() {
		return overwriteOutput;
	}



	public String getOutputArchiveFile() {
		return outputArchiveFile;
	}



	public String getExtendedClassPath() {
		return extendedClassPath;
	}



	public String getTRAPropertyFile() {
		return TRAPropertyFile;
	}



	public String getOperation() {
		return operation;
	}



	public String getProjectDir() {
		return projectDir;
	}



	/**
	 * 
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the Tibco installation for name.
	 */
	public TibcoInstallation getTibcoInstallation() {
		for (TibcoInstallation i : getDescriptor().getTibcoInstallations()) {
			if (getName() != null && getName().equals(i.getName()))
				return i;
		}
		return null;
	}
	
	
	@Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        ArgumentListBuilder args = new ArgumentListBuilder();

        EnvVars env = build.getEnvironment(listener);
        env.overrideAll(build.getBuildVariables());
        
        TibcoInstallation ti = getTibcoInstallation();
        String exe=null;
        if(ti==null) {
                listener.fatalError("ExecutableNotFound");//Messages.TibcoAnt_ExecutableNotFound(tai.getName()));
        } else {
            ti = ti.forNode(Computer.currentComputer().getNode(), listener);
            ti = ti.forEnvironment(env);
             exe = ti.getStudioToolsExecutable(launcher);
            if (exe==null) {
                listener.fatalError("ExecutableNotFound");//Messages.TibcoAnt_ExecutableNotFound(tai.getName()));
                return false;
            }
            args.add(exe);
        }

        VariableResolver<String> vr = new VariableResolver.ByMap<String>(env);
        String operation = env.expand(this.operation);
        String projectDir = env.expand(this.projectDir);
        
        
        if(operation!=null) {
            args.add("-core",this.operation);
	    }
	    
	    if(projectDir!=null) {
	        args.add("-p", projectDir);
	    }
        
	    if(this.isOverwriteOutput()){
	    	args.add("-x");
	    }
	    if(this.outputArchiveFile!=null){
	    	args.add("-o", this.outputArchiveFile);
	    }
	    
	    if(this.extendedClassPath!=null){
	    	args.add("-cp",this.extendedClassPath);
	    }
	    
	    if(TRAPropertyFile!=null) {
	        args.add("--propFile", TRAPropertyFile);
	    }else{
	    	
	    	if(!launcher.isUnix()){
	    		args.add("--propFile",exe.substring(exe.indexOf(".exe")).concat(".tra"));
	    	}
	    	else{
	    		args.add("--propFile",exe.concat(".tra"));
	    	}
	    }
	    
        
        
        if(!launcher.isUnix()) {
            args = args.toWindowsCommand();
            // For some reason, ant on windows rejects empty parameters but unix does not.
            // Add quotes for any empty parameter values:
            List<String> newArgs = new ArrayList<String>(args.toList());
            newArgs.set(newArgs.size() - 1, newArgs.get(newArgs.size() - 1).replaceAll(
                    "(?<= )(-D[^\" ]+)= ", "$1=\"\" "));
            args = new ArgumentListBuilder(newArgs.toArray(new String[newArgs.size()]));
        }

        long startTime = System.currentTimeMillis();
        try {
            StudioToolsConsoleAnnotator sca = new StudioToolsConsoleAnnotator(listener.getLogger(),build.getCharset());
            int r;
            try {
                r = launcher.launch().cmds(args).envs(env).stdout(sca).pwd(projectDir).join();
            } finally {
                sca.forceEol();
            }
            return r==0;
        } catch (IOException e) {
            Util.displayIOException(e,listener);

            String errorMessage = "StudioTools_ExecFailed";//Messages.TibcoAnt_ExecFailed();
            if(ti==null && (System.currentTimeMillis()-startTime)<1000) {
                if(getDescriptor().getTibcoInstallations()==null)
                    errorMessage += "GlobalConfigNeeded";//Messages.TibcoAnt_GlobalConfigNeeded();
                else
                    // There are Ant installations configured but the project didn't pick it
                    errorMessage += "ProjectConfigNeeded";//Messages.TibcoAnt_ProjectConfigNeeded();
            }
            e.printStackTrace( listener.fatalError(errorMessage) );
            return false;
        }
    }
	
	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}
	
	
	
	
	
	
	
	/**
	 * Descriptor for {@link StudioToolsBuilder}. Used as a singleton. The
	 * class is marked as public so that it can be accessed from views.
	 * 
	 */
	@Extension
	// This indicates to Jenkins that this is an implementation of an extension
	// point.
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Builder> {

		@CopyOnWrite
		private volatile TibcoInstallation[] installations = new TibcoInstallation[0];

		/**
		 * Obtains the {@link TibcoInstallation.DescriptorImpl} instance.
		 */
		public TibcoInstallation.DescriptorImpl getToolDescriptor() {
			return ToolInstallation.all().get(
					TibcoInstallation.DescriptorImpl.class);
		}

		


		public TibcoInstallation[] getTibcoInstallations() {
			return getToolDescriptor().getInstallations();
		}

		public ListBoxModel doFillNameItems() {
			ListBoxModel items = new ListBoxModel();
			for (TibcoInstallation ti : getTibcoInstallations()) {
				items.add(ti.getName(), ti.getName());
			}
			return items;
		}
		


		public ListBoxModel doFillOperationItems() {
			ListBoxModel items = new ListBoxModel();
			//items.add( "Import Designer Project","importDesigner");
			//items.add("Migrate Coherence Function Calls", "migrateCoherenceCalls");
			//items.add("Import Existing TIBCO BusinessEvents Studio Project","importExistingProject");
			//items.add("Create TIBCO BusinessEvents Studio 5.1 Project Library","buildLibrary");
			items.add("Build Enterprise Archive","buildEar");
			//items.add("Generate Class","generateClass");
			return items;
		}
		/**
		 * In order to load the persisted global configuration, you have to call
		 * load() in the constructor.
		 */
		public DescriptorImpl() {
			load();
		}


		

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "Tibco Business Events Builder";
		}

	}
}
