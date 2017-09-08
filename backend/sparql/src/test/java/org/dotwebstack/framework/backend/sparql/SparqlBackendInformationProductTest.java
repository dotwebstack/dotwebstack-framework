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
  private InformationProduct informationProduct;

  @Mock
  private QueryEvaluator queryEvaluator;

  @Mock
  private RepositoryConnection repositoryConnection;

  @Test
  public void builder() {
    // Arrange
    when(informationProduct.getIdentifier()).thenReturn(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT);
    when(informationProduct.getLabel()).thenReturn(DBEERPEDIA.BREWERIES_LABEL.stringValue());

    // Act
    SparqlBackendInformationProduct result =
        new SparqlBackendInformationProduct.Builder(informationProduct, backend, GRAPH_QUERY,
            ResultType.GRAPH, queryEvaluator).build();

    // Assert
    assertThat(result.getQuery(), equalTo(GRAPH_QUERY));
    assertThat(result.getResultType(), equalTo(ResultType.GRAPH));
    assertThat(result.getIdentifier(), equalTo(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT));
    assertThat(result.getLabel(), equalTo(DBEERPEDIA.BREWERIES_LABEL.stringValue()));
  }

  @Test
  public void requiredInformationProduct() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProduct.Builder(null, backend, GRAPH_QUERY, ResultType.GRAPH,
        queryEvaluator).build();
  }

  @Test
  public void requiredBackend() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProduct.Builder(informationProduct, null, GRAPH_QUERY,
        ResultType.GRAPH, queryEvaluator).build();
  }

  @Test
  public void requiredQuery() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProduct.Builder(informationProduct, backend, null, ResultType.GRAPH,
        queryEvaluator).build();
  }

  @Test
  public void requiredSparqlQueryType() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProduct.Builder(informationProduct, backend, GRAPH_QUERY, null,
        queryEvaluator).build();
  }

  @Test
  public void requiredQueryEvaluator() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProduct.Builder(informationProduct, backend, GRAPH_QUERY,
        ResultType.GRAPH, null).build();
  }

  @Test
  public void getResult() {
    // Arrange
    Object expectedResult = new Object();
    when(backend.getConnection()).thenReturn(repositoryConnection);
    when(queryEvaluator.evaluate(repositoryConnection, GRAPH_QUERY)).thenReturn(expectedResult);
    SparqlBackendInformationProduct source =
        new SparqlBackendInformationProduct.Builder(informationProduct, backend, GRAPH_QUERY,
            ResultType.GRAPH, queryEvaluator).build();

    // Act
    Object result = source.getResult();

    // Assert
    assertThat(result, equalTo(expectedResult));
  }

}
