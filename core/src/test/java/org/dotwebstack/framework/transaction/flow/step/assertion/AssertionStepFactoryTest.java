package org.dotwebstack.framework.transaction.flow.step.assertion;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.query.transformator.QueryTransformator;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssertionStepFactoryTest {

  @Mock
  private BackendResourceProvider backendResourceProvider;

  private QueryTransformator queryTransformator;

  private AssertionStep assertionStep;

  private AssertionStepFactory assertionStepFactory;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Before
  public void setup() {
    // Arrange
    queryTransformator = new QueryTransformator(backendResourceProvider);
    assertionStepFactory = new AssertionStepFactory(queryTransformator);
    Backend backend = mock(Backend.class);
    when(backend.getEndpoint()).thenReturn(mock(Literal.class));
    when(backend.getEndpoint().stringValue()).thenReturn("http://localhost:8080/sparql");
    when(backendResourceProvider.get(any())).thenReturn(backend);

  }

  @Test
  public void supports_ReturnTrue_WhenSupported() {
    // Act/Assert
    assertTrue(assertionStepFactory.supports(ELMO.ASSERTION_STEP));
  }

  @Test
  public void supports_ReturnTrue_WhenNotSupported() {
    // Act/Assert
    assertFalse(assertionStepFactory.supports(ELMO.VALIDATION_STEP));
  }

  @Test
  public void create_CreateAssertionStep_WithValidData() {
    // Arrange
    Model stepModel = new LinkedHashModel();
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.ASSERTION_IF_EXIST_STEP, RDF.TYPE,
        ELMO.ASSERTION_STEP));
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.ASSERTION_IF_EXIST_STEP, RDFS.LABEL,
        DBEERPEDIA.BREWERIES_LABEL));
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.ASSERTION_IF_EXIST_STEP, ELMO.ASSERT,
        DBEERPEDIA.ASK_ALL_QUERY));

    // Act
    assertionStep = assertionStepFactory.create(stepModel, DBEERPEDIA.ASSERTION_IF_EXIST_STEP);

    // Assert
    assertThat(assertionStep.getIdentifier(), equalTo(DBEERPEDIA.ASSERTION_IF_EXIST_STEP));
    assertThat(assertionStep.getLabel(), equalTo(DBEERPEDIA.BREWERIES_LABEL.stringValue()));
    assertThat(assertionStep.getAssertionQuery(), equalTo(DBEERPEDIA.ASK_ALL_QUERY.stringValue()));
  }

  @Test
  public void create_CreateAssertionStep_WithValidDataAndServiceTag() {
    // Arrange
    Model stepModel = new LinkedHashModel();
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.ASSERTION_IF_EXIST_STEP, RDF.TYPE,
        ELMO.ASSERTION_STEP));
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.ASSERTION_IF_EXIST_STEP, RDFS.LABEL,
        DBEERPEDIA.BREWERIES_LABEL));
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.ASSERTION_IF_EXIST_STEP, ELMO.ASSERT_NOT,
        DBEERPEDIA.ASK_ALL_QUERY_SERVICE_TAG));

    // Act
    assertionStep = assertionStepFactory.create(stepModel, DBEERPEDIA.ASSERTION_IF_EXIST_STEP);

    // Assert
    final Literal transformedQuery = valueFactory.createLiteral(
        "PREFIX dbeerpedia: <http://dbeerpedia.org#> ASK { ?s ?p ?o SERVICE "
            + "<http://localhost:8080/sparql> { ?s ?p ?o } }");
    assertThat(assertionStep.getAssertionQuery(), equalTo(transformedQuery.stringValue()));
  }

}
