/*
 * The MIT License
 *
 * Copyright 2016 Red Hat, Inc.
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

package hudson.cli;

import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.ListView;
import hudson.model.View;
import jenkins.model.Jenkins;
import org.junit.Test;

import static hudson.cli.CLICommandInvoker.Matcher.succeededSilently;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class Part2Test extends ViewManipulationTestBase {

    @Override
    public CLICommandInvoker getCommand() {
        return new CLICommandInvoker(j, "add-job-to-view");
    }

    @Test public void addJob() throws Exception {

        j.jenkins.addView(new ListView("curView"));
        FreeStyleProject project = j.createFreeStyleProject("newProject");

        assertThat(j.jenkins.getView("curView").getAllItems().size(), equalTo(0));
        assertThat(j.jenkins.getView("curView").contains(project), equalTo(false));

        final CLICommandInvoker.Result result = command
                .authorizedTo(Jenkins.READ, View.READ, Job.READ, View.CONFIGURE)
                .invokeWithArgs("curView", "newProject");

        assertThat(result, succeededSilently());
        assertThat(j.jenkins.getView("curView").getAllItems().size(), equalTo(1));
        assertThat(j.jenkins.getView("curView").contains(project), equalTo(true));
    }
}
