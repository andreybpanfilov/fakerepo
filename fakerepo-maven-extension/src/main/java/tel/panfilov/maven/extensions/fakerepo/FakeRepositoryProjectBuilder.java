package tel.panfilov.maven.extensions.fakerepo;

import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;

import java.io.File;
import java.util.List;

public interface FakeRepositoryProjectBuilder {

    ProjectBuildingResult build(File projectFile, ProjectBuildingRequest request) throws ProjectBuildingException;

    List<ProjectBuildingResult> build(List<File> pomFiles, boolean recursive, ProjectBuildingRequest request) throws ProjectBuildingException;

}
