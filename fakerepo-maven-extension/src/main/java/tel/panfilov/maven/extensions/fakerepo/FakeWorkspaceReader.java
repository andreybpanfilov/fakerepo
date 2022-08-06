package tel.panfilov.maven.extensions.fakerepo;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.eclipse.aether.util.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(role = FakeWorkspaceReader.class)
public class FakeWorkspaceReader implements WorkspaceReader {

    @Requirement
    private ArtifactHandlerManager artifactHandlerManager;

    private final WorkspaceRepository repository = new WorkspaceRepository();

    private final Map<String, MavenProject> projectMap = new HashMap<>();

    public void addProject(MavenProject project) {
        projectMap.put(getId(project), project);
    }

    @Override
    public WorkspaceRepository getRepository() {
        return repository;
    }

    @Override
    public File findArtifact(Artifact artifact) {
        if ("pom".equals(artifact.getExtension())) {
            return getPom(artifact);
        }
        return getArtifact(artifact);
    }

    protected File getPom(Artifact artifact) {
        MavenProject project = projectMap.get(getId(artifact));
        if (project == null) {
            return null;
        }
        File pom = project.getFile();
        if (pom.exists()) {
            return pom;
        }
        return null;
    }

    protected File getArtifact(Artifact artifact) {
        MavenProject project = projectMap.get(getId(artifact));
        if (project == null) {
            return null;
        }

        Build build = project.getBuild();

        StringBuilder name = new StringBuilder(artifact.getArtifactId());
        name.append('-').append(artifact.getVersion());
        if (!StringUtils.isEmpty(artifact.getClassifier())) {
            name.append('-').append(artifact.getClassifier());
        }
        name.append('.').append(artifact.getExtension());
        File file = new File(build.getDirectory(), name.toString());
        if (file.exists()) {
            return file;
        }

        boolean sameExtension = artifact.getExtension().equals(getExtension(project));

        if (sameExtension && !StringUtils.isEmpty(build.getFinalName())) {
            file = new File(build.getDirectory(), build.getFinalName() + "." + artifact.getExtension());
            if (file.exists()) {
                return file;
            }
        }

        return null;
    }

    @Override
    public List<String> findVersions(Artifact artifact) {
        MavenProject project = projectMap.get(getId(artifact));
        if (project != null) {
            return Collections.singletonList(project.getVersion());
        }
        return Collections.emptyList();
    }

    protected String getId(MavenProject project) {
        return project.getGroupId() + ':' + project.getArtifactId() + ':' + project.getVersion();
    }

    protected String getId(Artifact artifact) {
        return artifact.getGroupId() + ':' + artifact.getArtifactId() + ':' + artifact.getVersion();
    }

    protected String getExtension(MavenProject project) {
        ArtifactHandler artifactHandler = artifactHandlerManager.getArtifactHandler(project.getPackaging());
        return artifactHandler.getExtension();
    }

}
