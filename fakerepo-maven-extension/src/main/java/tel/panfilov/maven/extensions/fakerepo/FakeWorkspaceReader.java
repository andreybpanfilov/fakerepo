package tel.panfilov.maven.extensions.fakerepo;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.eclipse.aether.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component(role = FakeWorkspaceReader.class)
public class FakeWorkspaceReader implements WorkspaceReader {

    @Requirement
    private ArtifactHandlerManager artifactHandlerManager;

    @Requirement
    private Logger logger;

    private final WorkspaceRepository repository = new WorkspaceRepository();

    private long buildStartTime = -1L;

    private final Map<String, MavenProject> projectMap = new HashMap<>();

    public void addProject(MavenProject project) {
        projectMap.put(getId(project), project);
    }

    public void setBuildStartTime(long buildStartTime) {
        this.buildStartTime = buildStartTime;
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
        StringBuilder name = new StringBuilder(build.getFinalName());
        if (!StringUtils.isEmpty(artifact.getClassifier())) {
            name.append('-').append(artifact.getClassifier());
        }
        name.append('.').append(artifact.getExtension());
        File file = new File(build.getDirectory(), name.toString());
        if (isActual(file, artifact, project)) {
            return file;
        }
        return null;
    }

    protected boolean isActual(File packaged, Artifact artifact, MavenProject project) {
        if (!packaged.exists() || !packaged.isFile()) {
            return false;
        }

        Build build = project.getBuild();
        Path directory;
        if ("tests".equals(artifact.getClassifier())) {
            directory = Paths.get(build.getTestOutputDirectory());
        } else {
            directory = Paths.get(build.getOutputDirectory());
        }

        if (Files.notExists(directory) || !Files.isDirectory(directory)) {
            return true;
        }

        try (Stream<Path> outputFiles = Files.walk(directory)) {
            long artifactTime = Files.getLastModifiedTime(packaged.toPath()).toMillis();
            if (buildStartTime > 0 && artifactTime > buildStartTime) {
                return true;
            }

            Iterator<Path> iterator = outputFiles.iterator();
            while (iterator.hasNext()) {
                Path outputFile = iterator.next();

                if (Files.isDirectory(outputFile)) {
                    continue;
                }

                long outputFileLastModified = Files.getLastModifiedTime(outputFile).toMillis();
                if (outputFileLastModified > artifactTime) {
                    return false;
                }
            }

            return true;
        } catch (IOException e) {
            logger.warn("An I/O error occurred while checking if the packaged artifact is up-to-date "
                    + "against the build output directory. "
                    + "Continuing with the assumption that it is up-to-date.", e);
            return true;
        }
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
