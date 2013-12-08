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
import hudson.FilePath;
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
import java.util.Properties;
import java.util.Set;

import org.jenkinsci.plugins.tibco.axmeclipseant.TibcoConsoleAnnotator;
import org.jenkinsci.plugins.tibco.installation.TibcoInstallation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * TIBCO {@link AmxEclipseAntBuilder} plugin allows you to run fully automated builds over AMX Businessworks, AMX Service Grig components and AMX BPM.
 * 
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked and a new
 * {@link AmxEclipseAntBuilder} is created. The created instance is persisted to
 * the project configuration XML by using XStream, so this allows you to use
 * instance fields (like {@link #name}) to remember the configuration.
 * 
 * <p>
 * When a build is performed, the
 * {@link #perform(AbstractBuild, Launcher, BuildListener)} method will be
 * invoked.
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
public class AmxEclipseAntBuilder extends Builder {

	/**
	 * Identifies {@link TibcoInstallation} name to be used.
	 */
	private final String name;

	/**
	 * The targets, properties, and other Ant options. Either separated by
	 * whitespace or newline.
	 */
	private final String targets;

	/**
	 * Optional ant options.
	 */
	private final String antOpts;

	/**
	 * Optional build script path relative to the workspace. Used for the
	 * amx_eclispe_ant '-f' option.
	 */
	private final String buildFile;

	/**
	 * Optional properties to be passed to amx_eclispe_ant. Follows
	 * {@link Properties} syntax.
	 */
	private final String properties;

	/**
	 * Optional properties to be passed to amx_eclispe_ant. You can use also
	 * --propValue sintax in {@link Properties}.TRA properties are mandatory to
	 * run tibco executables.
	 */
	private final String amxEclipseAntTra;
	/**
	 * Optional path to custom workspace to be passed to amx_eclispe_ant.
	 */
	private final String businessStudioWs;

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public AmxEclipseAntBuilder(String targets, String name, String antOpts,
			String buildFile, String properties, String amxEclipseAntTra,
			String businessStudioWs) {
		this.targets = Util.fixEmptyAndTrim(targets);
		this.antOpts = Util.fixEmptyAndTrim(antOpts);
		this.buildFile = Util.fixEmptyAndTrim(buildFile);
		this.properties = Util.fixEmptyAndTrim(properties);
		this.amxEclipseAntTra = Util.fixEmptyAndTrim(amxEclipseAntTra);
		this.businessStudioWs = Util.fixEmptyAndTrim(businessStudioWs);
		this.name = name;
	}

	/**
	 * 
	 */
	public String getName() {
		return name;
	}

	public String getBusinessStudioWs() {
		return businessStudioWs;
	}

	public String getBuildFile() {
		return buildFile;
	}

	public String getProperties() {
		return properties;
	}

	public String getTargets() {
		return targets;
	}

	public String getAmxEclipseAntTra() {
		return amxEclipseAntTra;
	}

	/**
	 * Gets the ANT_OPTS parameter, or null.
	 */
	public String getAntOpts() {
		return antOpts;
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
                listener.fatalError(Messages.ExecutableNotFound(ti.getName()));
        } else {
            ti = ti.forNode(Computer.currentComputer().getNode(), listener);
            ti = ti.forEnvironment(env);
            exe = ti.getAmxEclipseAntExecutable(launcher);
            if (exe==null) {
                listener.fatalError(Messages.ExecutableNotFound(ti.getName()));
                return false;
            }
            args.add(exe);
        }

        VariableResolver<String> vr = new VariableResolver.ByMap<String>(env);
        String workspace = env.expand(this.businessStudioWs);
        String buildFile = env.expand(this.buildFile);
        String targets = env.expand(this.targets);
        String amxEclipseAntTraProp= env.expand(this.amxEclipseAntTra);
        
        FilePath buildFilePath = buildFilePath(build.getModuleRoot(), buildFile, targets);

        if(!buildFilePath.exists()) {

            // first check if this appears to be a valid relative path from workspace root
            FilePath buildFilePath2 = buildFilePath(build.getWorkspace(), buildFile, targets);
            if(buildFilePath2.exists()) {
                // This must be what the user meant. Let it continue.
                buildFilePath = buildFilePath2;
            } else {
                // neither file exists. So this now really does look like an error.
                listener.fatalError(Messages.UnableToFindBuildFile(buildFilePath));//("Unable to find build script at "+buildFilePath);
                return false;
            }
        }

        
        
        
        if(amxEclipseAntTraProp!=null) {
	        args.add("--propFile", amxEclipseAntTraProp);
	    }else{
	    	
	    	if(!launcher.isUnix()){
	    		args.add("--propFile",exe.substring(0,exe.indexOf(".exe")).concat(".tra"));
	    	}
	    	else{
	    		args.add("--propFile",exe.concat(".tra"));
	    	}
	    }
        
        
        if(buildFile!=null) {
            args.add("-file", buildFilePath.getName());
        }

        if(workspace!=null) {
                args.add("-data",workspace);
        }
        
        Set<String> sensitiveVars = build.getSensitiveBuildVariables();

        args.addKeyValuePairs("-D",build.getBuildVariables(),sensitiveVars);

        args.addKeyValuePairsFromPropertyString("-D",properties,vr,sensitiveVars);
        
        if(targets!=null)
                args.addTokenized(targets.replaceAll("[\t\r\n]+"," "));

        if(ti!=null)
            ti.buildEnvVars(env);
        
        if(this.antOpts!=null)
            env.put("ANT_OPTS",env.expand(this.antOpts));

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
            TibcoConsoleAnnotator aca = new TibcoConsoleAnnotator(listener.getLogger(),build.getCharset());
            int r;
            try {
                r = launcher.launch().cmds(args).envs(env).stdout(aca).pwd(buildFilePath.getParent()).join();
            } finally {
                aca.forceEol();
            }
            return r==0;
        } catch (IOException e) {
            Util.displayIOException(e,listener);

            String errorMessage = Messages.ExecFailed();
            if(ti==null && (System.currentTimeMillis()-startTime)<1000) {
                if(getDescriptor().getTibcoInstallations()==null)
                    // looks like the user didn't configure any Tibco installation
                    errorMessage += Messages.GlobalConfigNeeded();
                else
                    // There are Tibco installations configured but the project didn't pick it
                    errorMessage += Messages.ProjectConfigNeeded();
            }
            e.printStackTrace( listener.fatalError(errorMessage) );
            return false;
        }
    }

    private static FilePath buildFilePath(FilePath base, String buildFile, String targets) {
        if(buildFile!=null)     return base.child(buildFile);
        // some users specify the -f option in the targets field, so take that into account as well.
        // see 
        String[] tokens = Util.tokenize(targets);
        for (int i = 0; i<tokens.length-1; i++) {
            String a = tokens[i];
            if(a.equals("-f") || a.equals("-file") || a.equals("-buildfile"))
                return base.child(tokens[i+1]);
        }
        return base.child("build.xml");
    }

	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	/**
	 * Descriptor for {@link AmxEclipseAntBuilder}. Used as a singleton. The
	 * class is marked as public so that it can be accessed from views.
	 * 
	 * <p>
	 * See
	 * <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
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
			return Messages.AMXEclipseAntDisplayName();
		}

	}
}
