package org.dotwebstack.framework.transaction.flow.step.update;

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
public class UpdateStepFactoryTest {

  private UpdateStep updateStep;

  @Mock
  private BackendResourceProvider backendResourceProvider;

  @Mock
  private Backend backend;

  private QueryTransformator queryTransformator;

  private UpdateStepFactory updateStepFactory;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Before
  public void setup() {
    // Arrange
    queryTransformator = new QueryTransformator(backendResourceProvider);
    updateStepFactory = new UpdateStepFactory(backendResourceProvider, queryTransformator);
  }

  @Test
  public void supports_ReturnTrue_WhenSupported() {
    // Act/Assert
    assertTrue(updateStepFactory.supports(ELMO.UPDATE_STEP));
  }

  @Test
  public void supports_ReturnTrue_WhenNotSupported() {
    // Act/Assert
    assertFalse(updateStepFactory.supports(ELMO.VALIDATION_STEP));
  }

  @Test
  public void create_CreateUpdateStep_WithValidData() {
    // Arrange
    Model stepModel = new LinkedHashModel();
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.UPDATE_STEP, RDF.TYPE, ELMO.UPDATE_STEP));
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.UPDATE_STEP, RDFS.LABEL,
        DBEERPEDIA.BREWERIES_LABEL));
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.UPDATE_STEP, ELMO.QUERY,
        DBEERPEDIA.UPDATE_QUERY_SERVICE_TAG));
    // stepModel.add(valueFactory.createStatement(DBEERPEDIA.UPDATE_STEP, ELMO.QUERY,
    // DBEERPEDIA.SELECT_ALL_QUERY));
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.UPDATE_STEP, ELMO.BACKEND_PROP,
        DBEERPEDIA.BACKEND));

    Backend backend = mock(Backend.class);
    when(backend.getEndpoint()).thenReturn(mock(Literal.class));
    when(backend.getEndpoint().stringValue()).thenReturn("http://dbeerpedia.org#Backend");
    when(backendResourceProvider.get(any())).thenReturn(backend);

    // Act
    updateStep = updateStepFactory.create(stepModel, DBEERPEDIA.UPDATE_STEP);

    // Assert
    final Literal transformedQuery = valueFactory.createLiteral(
        "PREFIX dbeerpedia: <http://dbeerpedia.org#> insert { ?concept rdfs:label ?label } where { ?s ?p ?o SERVICE <http://dbeerpedia.org#Backend> { ?s rdfs:label ?p. } }");
    assertThat(updateStep.getIdentifier(), equalTo(DBEERPEDIA.UPDATE_STEP));
    assertThat(updateStep.getLabel(), equalTo(DBEERPEDIA.BREWERIES_LABEL.stringValue()));
    assertThat(updateStep.getQuery(), equalTo(transformedQuery.stringValue()));
    assertThat(updateStep.getBackendIri(), equalTo(DBEERPEDIA.BACKEND));
  }

}
