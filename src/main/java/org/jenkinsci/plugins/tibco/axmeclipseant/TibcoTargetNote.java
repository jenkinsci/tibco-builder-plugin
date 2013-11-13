
package org.jenkinsci.plugins.tibco.axmeclipseant;

import hudson.Extension;
import hudson.MarkupText;
import hudson.console.ConsoleNote;
import hudson.console.ConsoleAnnotationDescriptor;
import hudson.console.ConsoleAnnotator;

import java.util.regex.Pattern;

/**
*
*   Copyright 2013 federico pastore - federico.pastore@gmail.com
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
*
*
* This plugin, based on Jenkins Ant plugin, enable any jenkins installation to run step builds for Tibco applications.
* Actually this plugin has been tested in Windows environment
*
* @author federico pastore - federico.pastore@gmail.com
*
*/
public final class TibcoTargetNote extends ConsoleNote {
	
    public TibcoTargetNote() {
    }

    @Override
    public ConsoleAnnotator annotate(Object context, MarkupText text, int charPos) {
    	
        if (!ENABLED)   return null;

        MarkupText.SubText t = text.findToken(Pattern.compile(".*(?=:)"));
        if (t!=null)
            t.addMarkup(0,t.length(),"<b class=ant-target>","</b>");
        return null;
    }

    @Extension
    public static final class DescriptorImpl extends ConsoleAnnotationDescriptor {
        public String getDisplayName() {
            return "AMX Tibco Ant targets";
        }
    }

    public static boolean ENABLED = !Boolean.getBoolean(TibcoTargetNote.class.getName()+".disabled");
}
