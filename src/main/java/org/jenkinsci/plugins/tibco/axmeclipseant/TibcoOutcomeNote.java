package org.jenkinsci.plugins.tibco.axmeclipseant;

import hudson.Extension;
import hudson.MarkupText;
import hudson.console.ConsoleAnnotationDescriptor;
import hudson.console.ConsoleAnnotator;
import hudson.console.ConsoleNote;

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
