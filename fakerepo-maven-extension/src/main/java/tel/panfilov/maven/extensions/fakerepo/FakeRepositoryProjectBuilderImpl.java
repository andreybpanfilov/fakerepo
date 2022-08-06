package tel.panfilov.maven.extensions.fakerepo;

import org.apache.maven.project.DefaultProjectBuilder;
import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.sisu.plexus.Strategies;

@Component(role = FakeRepositoryProjectBuilder.class, instantiationStrategy = Strategies.PER_LOOKUP)
public class FakeRepositoryProjectBuilderImpl extends DefaultProjectBuilder implements FakeRepositoryProjectBuilder {


}
