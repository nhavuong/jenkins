package hudson.cli;
import hudson.Functions;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.labels.LabelAtom;
import hudson.tasks.BatchFile;
import hudson.tasks.Shell;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static hudson.cli.CLICommandInvoker.Matcher.failedWith;
import static hudson.cli.CLICommandInvoker.Matcher.hasNoStandardOutput;
import static hudson.cli.CLICommandInvoker.Matcher.succeeded;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class Part4Test {
    private CLICommandInvoker command;

    @Rule public final JenkinsRule j = new JenkinsRule();

    @Before public void setUp() {
        command = new CLICommandInvoker(j, new RunRangeCommandTest.DummyRangeCommand());
    }

    @Test public void dummyRangeShouldFailIfJobNameIsEmptyOnEmptyJenkins() throws Exception {
        j.createFreeStyleProject("aProject").scheduleBuild2(0).get();
        assertThat(((FreeStyleProject) j.jenkins.getItem("aProject")).getBuilds().size(), equalTo(1));

        CLICommandInvoker.Result result = command
                .authorizedTo(Jenkins.READ, Job.READ)
                .invokeWithArgs("", "1");
        assertThat(result, failedWith(3));
        assertThat(result, hasNoStandardOutput());
        assertThat(result.stderr(), containsString("ERROR: No such job ''"));
    }

    @Test public void dummyRangeShouldFailIfJobNameIsSpaceOnEmptyJenkins() throws Exception {
        j.createFreeStyleProject("aProject").scheduleBuild2(0).get();
        assertThat(((FreeStyleProject) j.jenkins.getItem("aProject")).getBuilds().size(), equalTo(1));

        CLICommandInvoker.Result result = command
                .authorizedTo(Jenkins.READ, Job.READ)
                .invokeWithArgs(" ", "1");
        assertThat(result, failedWith(3));
        assertThat(result, hasNoStandardOutput());
        assertThat(result.stderr(), containsString("ERROR: No such job ' '"));
    }

    @Test public void dummyRangeShouldSuccessEvenTheBuildIsRunning() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject("aProject");
        project.getBuildersList().add(Functions.isWindows() ? new BatchFile("echo 1\r\nping -n 10 127.0.0.1 >nul") : new Shell("echo 1\nsleep 10s"));
        assertThat("Job wasn't scheduled properly", project.scheduleBuild(0), equalTo(true));

        // Wait until classProject is started (at least 1s)
        while(!project.isBuilding()) {
            System.out.println("Waiting for build to start and sleep 1s...");
            Thread.sleep(1000);
        }

        // Wait for the first sleep
        if(!project.getBuildByNumber(1).getLog().contains("echo 1")) {
            Thread.sleep(1000);
        }

        final CLICommandInvoker.Result result = command
                .authorizedTo(Jenkins.READ, Job.READ)
                .invokeWithArgs("aProject", "1");
        assertThat(result, succeeded());
        assertThat(result.stdout(), containsString("Builds: 1" + System.lineSeparator()));
    }

    @Test public void dummyRangeShouldSuccessEvenTheBuildIsStuckInTheQueue() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject("aProject");
        project.getBuildersList().add(new Shell("echo 1\nsleep 10s"));
        project.setAssignedLabel(new LabelAtom("never_created"));
        assertThat("Job wasn't scheduled properly", project.scheduleBuild(0), equalTo(true));
        Thread.sleep(1000);
        assertThat("Job wasn't scheduled properly - it isn't in the queue",
                project.isInQueue(), equalTo(true));
        assertThat("Job wasn't scheduled properly - it is running on non-exist node",
                project.isBuilding(), equalTo(false));

        final CLICommandInvoker.Result result = command
                .authorizedTo(Jenkins.READ, Job.READ)
                .invokeWithArgs("aProject", "1");
        assertThat(result, succeeded());
        assertThat(result.stdout(), containsString("Builds: " + System.lineSeparator()));
    }

}

