package tel.panfilov.maven.extensions.fakerepo;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.eclipse.aether.util.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeWorkspaceReader implements WorkspaceReader {

    private final WorkspaceReader workspaceReader;

    private final Map<String, MavenProject> projectMap;

    private final ArtifactHandlerManager artifactHandlerManager;

    public FakeWorkspaceReader(WorkspaceReader workspaceReader, MavenProject mavenProject, ArtifactHandlerManager artifactHandlerManager) {
        this.workspaceReader = workspaceReader;
        this.artifactHandlerManager = artifactHandlerManager;
        this.projectMap = buildProjectMap(mavenProject);
    }

    @Override
    public WorkspaceRepository getRepository() {
        return workspaceReader.getRepository();
    }

    @Override
    public File findArtifact(Artifact artifact) {
        File result;
        if ("pom".equals(artifact.getExtension())) {
            result = getPom(artifact);
        } else {
            result = getArtifact(artifact);
        }
        if (result == null) {
            result = workspaceReader.findArtifact(artifact);
        }
        return result;
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
        return workspaceReader.findVersions(artifact);
    }

    protected Map<String, MavenProject> buildProjectMap(MavenProject project) {
        Map<String, MavenProject> result = new HashMap<>();
        result.put(getId(project), project);
        for (MavenProject child : project.getCollectedProjects()) {
            result.put(getId(child), child);
        }
        return result;
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
