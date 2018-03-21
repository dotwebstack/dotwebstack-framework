package org.dotwebstack.framework.transaction.flow.step.assertion;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.Optional;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.transaction.flow.step.StepExecutor;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssertionStepTest {

  private AssertionStep assertionStep;

  @Mock
  private Resource identifier;

  @Mock
  private RepositoryConnection repositoryConnection;

  @Mock
  private StepExecutor backendStepExecutor;

  private String label;

  private Optional<String> assertionQuery;

  private Optional<String> assertionNotQuery;

  ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Test
  public void build_CreateAssertionNotStep_WithAssertionAndAssertNotSet() {
    // Arrange
    label = DBEERPEDIA.BREWERIES_LABEL.toString();
    assertionQuery = Optional.ofNullable(DBEERPEDIA.ASK_ALL_QUERY.toString());
    assertionNotQuery = Optional.ofNullable(DBEERPEDIA.ASK2_ALL_QUERY.toString());

    // Act
    assertionStep = new AssertionStep.Builder(identifier).label(label).assertion(assertionQuery,
        assertionNotQuery).build();

    // Assert
    assertThat(assertionStep.getIdentifier(), equalTo(identifier));
    assertThat(assertionStep.getLabel(), equalTo(label));
    assertThat(assertionStep.getAssertionQuery(), equalTo(DBEERPEDIA.ASK2_ALL_QUERY.toString()));
    assertThat(assertionStep.isAssertionNot(), equalTo(true));
  }

  @Test
  public void build_CreateAssertionStep_WithAssertionSet() {
    // Arrange
    assertionQuery = Optional.ofNullable(DBEERPEDIA.ASK_ALL_QUERY.toString());
    assertionNotQuery = Optional.empty();

    // Act
    assertionStep = new AssertionStep.Builder(identifier).assertion(assertionQuery,
        assertionNotQuery).build();

    // Assert
    assertThat(assertionStep.getIdentifier(), equalTo(identifier));
    assertThat(assertionStep.getLabel(), equalTo(label));
    assertThat(assertionStep.getAssertionQuery(), equalTo(DBEERPEDIA.ASK_ALL_QUERY.toString()));
    assertThat(assertionStep.isAssertionNot(), equalTo(false));
  }

  @Test
  public void build_CreateAssertionNotStep_WithAssertNotSet() {
    // Arrange
    assertionQuery = Optional.empty();
    assertionNotQuery = Optional.ofNullable(DBEERPEDIA.ASK2_ALL_QUERY.toString());

    // Act
    assertionStep = new AssertionStep.Builder(identifier).assertion(assertionQuery,
        assertionNotQuery).build();

    // Assert
    assertThat(assertionStep.getIdentifier(), equalTo(identifier));
    assertThat(assertionStep.getLabel(), equalTo(label));
    assertThat(assertionStep.getAssertionQuery(), equalTo(DBEERPEDIA.ASK2_ALL_QUERY.toString()));
    assertThat(assertionStep.isAssertionNot(), equalTo(true));
  }

  @Test
  public void createStepExecutor_GetStepExecutor_WithValidData() {
    // Arrange
    assertionStep = new AssertionStep.Builder(identifier).build();

    // Act
    StepExecutor stepExecutor = assertionStep.createStepExecutor(repositoryConnection);

    // Assert
    assertThat(stepExecutor, instanceOf(AssertionTransactionRepositoryExecutor.class));
  }

}
