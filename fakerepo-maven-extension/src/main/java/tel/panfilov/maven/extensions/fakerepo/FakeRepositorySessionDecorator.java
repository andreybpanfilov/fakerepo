package tel.panfilov.maven.extensions.fakerepo;

import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.RepositorySessionDecorator;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.WorkspaceReader;

@Component(role = RepositorySessionDecorator.class)
public class FakeRepositorySessionDecorator implements RepositorySessionDecorator {

    @Requirement
    protected ArtifactHandlerManager artifactHandlerManager;

    @Override
    public RepositorySystemSession decorate(MavenProject project, RepositorySystemSession session) {
        String enabled = session.getUserProperties().get("fakerepo");
        if (!"true".equalsIgnoreCase(enabled)) {
            return null;
        }
        MavenProject root = project;
        while (root != null && !root.isExecutionRoot()) {
            root = root.getParent();
        }
        if (root != null) {
            WorkspaceReader workspaceReader = session.getWorkspaceReader();
            workspaceReader = new FakeWorkspaceReader(workspaceReader, root, artifactHandlerManager);
            return new DefaultRepositorySystemSession(session)
                    .setWorkspaceReader(workspaceReader);
        }
        return null;
    }

}
