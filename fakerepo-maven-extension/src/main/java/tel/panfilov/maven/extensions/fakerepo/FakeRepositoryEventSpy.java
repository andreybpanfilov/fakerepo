package tel.panfilov.maven.extensions.fakerepo;

import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.aether.util.repository.ChainedWorkspaceReader;

import java.util.Date;
import java.util.Optional;

@Component(role = EventSpy.class, hint = "fakerepo")
public class FakeRepositoryEventSpy extends AbstractEventSpy {

    @Requirement
    private FakeWorkspaceReader workspaceReader;

    @Override
    public void onEvent(Object event) {
        if (event instanceof MavenExecutionRequest) {
            MavenExecutionRequest request = (MavenExecutionRequest) event;
            Optional.of(request)
                    .map(MavenExecutionRequest::getProjectBuildingRequest)
                    .map(ProjectBuildingRequest::getBuildStartTime)
                    .map(Date::getTime)
                    .ifPresent(workspaceReader::setBuildStartTime);
            request.setWorkspaceReader(ChainedWorkspaceReader.newInstance(
                    workspaceReader,
                    request.getWorkspaceReader()
            ));
        }
    }

}
