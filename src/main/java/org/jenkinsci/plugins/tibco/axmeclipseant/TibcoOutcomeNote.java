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
package org.jenkinsci.plugins.tibco.axmeclipseant;

import org.jenkinsci.plugins.tibco.AmxEclipseAntBuilder;
import org.jenkinsci.plugins.tibco.AmxEclipseAntBuilder.DescriptorImpl;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Launcher;
import hudson.MarkupText;
import hudson.console.ConsoleAnnotationDescriptor;
import hudson.console.ConsoleAnnotator;
import hudson.console.ConsoleNote;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

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

public class TibcoOutcomeNote extends ConsoleNote {
    public TibcoOutcomeNote() {
    }

    @Override
    public ConsoleAnnotator annotate(Object context, MarkupText text, int charPos) {
        if (text.getText().contains("FAIL"))
            text.addMarkup(0,text.length(),"<span class=tibco-ant-outcome-failure>","</span>");
        if (text.getText().contains("SUCCESS"))
            text.addMarkup(0,text.length(),"<span class=tibco-ant-outcome-success>","</span>");
        return null;
    }

    @Extension
    public static final class DescriptorImpl extends ConsoleAnnotationDescriptor {
        public String getDisplayName() {
            return "Tibco AMX Ant build outcome";
        }
    }
}
