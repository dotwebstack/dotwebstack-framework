package org.dotwebstack.framework.transaction.flow.step.update;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.transaction.flow.step.StepExecutor;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateStepTest {

  private UpdateStep updateStep;

  @Mock
  private Resource identifier;

  @Mock
  private BackendResourceProvider backendResourceProvider;

  @Mock
  private Resource backendIri;

  @Mock
  private Backend backend;

  @Mock
  private RepositoryConnection repositoryConnection;

  @Mock
  private StepExecutor backendStepExecutor;

  private String label;

  private String query;

  ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Test
  public void build_CreateUpdateStep_WithValidData() {
    // Arrange
    label = DBEERPEDIA.BREWERIES_LABEL.toString();
    query = DBEERPEDIA.SELECT_ALL_QUERY.toString();

    // Act
    updateStep = new UpdateStep.Builder(identifier, backendResourceProvider).label(label)
        .backend(backendIri).query(query).build();

    // Assert
    assertThat(updateStep.getIdentifier(), equalTo(identifier));
    assertThat(updateStep.getBackendIri(), equalTo(backendIri));
    assertThat(updateStep.getLabel(), equalTo(label));
    assertThat(updateStep.getQuery(), equalTo(query));
  }

  @Test
  public void createStepExecutor_GetTransactionRepositoryExecutor_WithValidData() {
    // Arrange
    IRI backendIri = ELMO.TRANSACTION_REPOSITORY;
    updateStep = new UpdateStep.Builder(identifier, backendResourceProvider)
        .backend(backendIri).build();

    // Act
    StepExecutor stepExecutor = updateStep.createStepExecutor(repositoryConnection);

    // Assert
    assertThat(stepExecutor, instanceOf(UpdateTransactionRepositoryExecutor.class));
  }

  @Test
  public void createStepExecutor_GetStepExecutor_WithValidData() {
    // Arrange
    IRI backendIri = ELMO.BACKEND;
    updateStep = new UpdateStep.Builder(identifier, backendResourceProvider)
        .backend(backendIri).build();
    when(backendResourceProvider.get(any())).thenReturn(backend);
    when(backend.createUpdateStepExecutor(any())).thenReturn(backendStepExecutor);

    // Act
    StepExecutor stepExecutor = updateStep.createStepExecutor(repositoryConnection);

    // Assert
    assertThat(stepExecutor, instanceOf(StepExecutor.class));
    assertThat(stepExecutor, equalTo(backendStepExecutor));
  }

}
