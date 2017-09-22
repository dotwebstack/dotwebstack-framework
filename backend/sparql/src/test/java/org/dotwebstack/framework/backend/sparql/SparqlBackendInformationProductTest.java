package org.dotwebstack.framework.backend.sparql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SparqlBackendInformationProductTest {

  private static final String GRAPH_QUERY = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o}";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private SparqlBackend backend;

  @Mock
  private QueryEvaluator queryEvaluator;

  @Mock
  private RepositoryConnection repositoryConnection;

  @Test
  public void build_CreatesInformationProduct_WithCorrectData() {
    // Act
    SparqlBackendInformationProduct result =
        new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
            GRAPH_QUERY, ResultType.GRAPH, queryEvaluator).build();

    // Assert
    assertThat(result.getQuery(), equalTo(GRAPH_QUERY));
    assertThat(result.getResultType(), equalTo(ResultType.GRAPH));
    assertThat(result.getIdentifier(), equalTo(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT));
  }

  @Test
  public void build_ThrowsException_WithMissingIdentifier() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProduct.Builder(null, backend, GRAPH_QUERY, ResultType.GRAPH,
        queryEvaluator).build();
  }

  @Test
  public void build_SetsLabel_WithValidLabel() {
    // Arrange
    SparqlBackendInformationProduct.Builder builder =
        new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
            GRAPH_QUERY, ResultType.GRAPH, queryEvaluator);
    builder.label(DBEERPEDIA.BREWERIES_LABEL.stringValue());

    // Act
    InformationProduct product = builder.build();

    // Assert
    assertThat(product.getLabel(), equalTo(DBEERPEDIA.BREWERIES_LABEL.stringValue()));
  }

  @Test
  public void build_ThrowsException_WithMissingBackend() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, null,
        GRAPH_QUERY, ResultType.GRAPH, queryEvaluator).build();
  }

  @Test
  public void build_ThrowsException_WithMissingQuery() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
        null, ResultType.GRAPH, queryEvaluator).build();
  }

  @Test
  public void build_ThrowsException_WithMissingResultType() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
        GRAPH_QUERY, null, queryEvaluator).build();
  }

  @Test
  public void build_ThrowsException_WithMissingQueryEvaluator() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
        GRAPH_QUERY, ResultType.GRAPH, null).build();
  }

  @Test
  public void getResult_UsesEvaluator_WhenGettingResult() {
    // Arrange
    Object expectedResult = new Object();
    when(backend.getConnection()).thenReturn(repositoryConnection);
    when(queryEvaluator.evaluate(repositoryConnection, GRAPH_QUERY)).thenReturn(expectedResult);
    SparqlBackendInformationProduct source =
        new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
            GRAPH_QUERY, ResultType.GRAPH, queryEvaluator).build();

    // Act
    Object result = source.getResult();

    // Assert
    assertThat(result, equalTo(expectedResult));
  }

}
