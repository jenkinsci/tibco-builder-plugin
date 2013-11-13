
package org.jenkinsci.plugins.tibco.studiotools;

import hudson.Launcher;
import hudson.console.LineTransformationOutputStream;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.jenkinsci.plugins.tibco.AmxEclipseAntBuilder;
import org.jenkinsci.plugins.tibco.AmxEclipseAntBuilder.DescriptorImpl;
import org.kohsuke.stapler.StaplerRequest;
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

public class StudioToolsConsoleAnnotator extends LineTransformationOutputStream {
    private final OutputStream outputConsole;
    private final Charset charset;

    private boolean isEmptyLine;

    public StudioToolsConsoleAnnotator(OutputStream out, Charset charset) {
        this.outputConsole = out;
        this.charset = charset;
    }

    @Override
    protected void eol(byte[] b, int len) throws IOException {
        String line = charset.decode(ByteBuffer.wrap(b, 0, len)).toString();
        line = trimEOL(line);
        if (isEmptyLine && endsWith(line,':') && line.indexOf(' ')<0)
            new StudioToolsTargetNote().encodeTo(outputConsole);

        if (line.equals("BUILD SUCCESSFUL") || line.equals("BUILD FAILED"))
            new StudioToolsOutcomeNote().encodeTo(outputConsole);

        isEmptyLine = line.length()==0;
        outputConsole.write(b,0,len);
    }

    private boolean endsWith(String line, char c) {
        int len = line.length();
        return len>0 && line.charAt(len-1)==c;
    }

    @Override
    public void close() throws IOException {
        super.close();
        outputConsole.close();
    }

}
