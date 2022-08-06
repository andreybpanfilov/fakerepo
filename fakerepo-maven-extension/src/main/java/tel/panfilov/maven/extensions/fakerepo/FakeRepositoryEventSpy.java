package tel.panfilov.maven.extensions.fakerepo;

import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.MavenExecutionRequest;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.aether.util.repository.ChainedWorkspaceReader;

@Component(role = EventSpy.class, hint = "fakerepo")
public class FakeRepositoryEventSpy extends AbstractEventSpy {

    @Requirement
    private FakeWorkspaceReader workspaceReader;

    @Override
    public void onEvent(Object event) {
        if (event instanceof MavenExecutionRequest) {
            MavenExecutionRequest request = (MavenExecutionRequest) event;
            request.setWorkspaceReader(ChainedWorkspaceReader.newInstance(
                    request.getWorkspaceReader(),
                    this.workspaceReader
            ));
        }
    }

}
