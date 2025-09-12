package rs.ac.bg.etf.pp1;

import org.apache.tools.ant.DefaultLogger;
import java.io.File;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

public class Ant {
    public static void main(String[] args) {
        String cmnd = args[0];
        File buildXml = new File("build.xml");
        Project proj = new Project();
        proj.setUserProperty("ant.file", buildXml.getAbsolutePath());
        DefaultLogger log = new DefaultLogger();
        log.setMessageOutputLevel(Project.MSG_INFO);
        log.setOutputPrintStream(System.out);
        log.setErrorPrintStream(System.err);
        proj.addBuildListener(log);
        proj.init();
        ProjectHelper.configureProject(proj, buildXml);
        try {
            proj.fireBuildStarted();
            proj.executeTarget(cmnd);
            proj.fireBuildFinished(null);
        }
        catch (Exception exception) {
            proj.fireBuildFinished(exception);
            System.out.println("ANT GRESKA");
        }
    }
}
