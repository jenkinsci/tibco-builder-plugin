
package org.jenkinsci.plugins.tibco.studiotools;

import hudson.console.LineTransformationOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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
