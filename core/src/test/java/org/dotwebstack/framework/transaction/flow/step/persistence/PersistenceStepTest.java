package org.dotwebstack.framework.transaction.flow.step.persistence;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PersistenceStepTest {

  private PersistenceStep persistenceStep;

  @Mock
  private Resource identifier;

  @Mock
  private BackendResourceProvider backendResourceProvider;

  @Mock
  private IRI persistenceStragey;

  @Mock
  private Backend backend;

  @Mock
  private IRI targetGraph;

  @Test
  public void build_CreateStepExecutor_WithValidData() {
    persistenceStep = new PersistenceStep.Builder(identifier, backendResourceProvider)
        .persistenceStrategy(persistenceStragey).backend(backend).targetGraph(targetGraph).build();

    assertThat(persistenceStep.getIdentifier(), equalTo(identifier));
    assertThat(persistenceStep.getPersistenceStrategy(), equalTo(persistenceStragey));
    assertThat(persistenceStep.getBackend(), equalTo(backend));
    assertThat(persistenceStep.getTargetGraph(), equalTo(targetGraph));
  }

}
