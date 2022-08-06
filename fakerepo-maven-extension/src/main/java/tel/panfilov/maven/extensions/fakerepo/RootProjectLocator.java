package tel.panfilov.maven.extensions.fakerepo;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

import java.io.File;
import java.util.Optional;
import java.util.function.Predicate;

@Component(role = RootProjectLocator.class)
public class RootProjectLocator {

    public MavenProject getRootProject(MavenProject project, File rootDirectory) {
        MavenProject root = lookupByMultiModuleProjectDirectory(project, rootDirectory);
        if (root == null) {
            root = lookupRootByFolder(project);
        }
        if (root == null) {
            root = lookupRootByExecution(project);
        }
        if (root == null) {
            root = project;
        }
        return root;
    }

    protected MavenProject lookupByMultiModuleProjectDirectory(MavenProject project, File rootDirectory) {
        if (rootDirectory == null) {
            return null;
        }
        MavenProject root = project;
        while (root != null && !rootDirectory.equals(root.getBasedir())) {
            root = root.getParent();
        }
        return root;
    }

    protected MavenProject lookupRootByFolder(MavenProject project) {
        Predicate<MavenProject> hasMvnFolder = prj -> Optional.of(prj)
                .map(MavenProject::getBasedir)
                .map(dir -> new File(dir, ".mvn"))
                .map(File::isDirectory)
                .orElse(false);
        MavenProject root = project;
        while (root != null && !hasMvnFolder.test(root)) {
            root = root.getParent();
        }
        return root;
    }

    protected MavenProject lookupRootByExecution(MavenProject project) {
        MavenProject root = project;
        while (root != null && !root.isExecutionRoot()) {
            root = root.getParent();
        }
        return root;
    }

}
