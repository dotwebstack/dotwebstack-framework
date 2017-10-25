package org.dotwebstack.framework.backend.sparql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.filter.Filter;
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

  @Mock
  private Filter filter1Mock;

  @Mock
  private Filter filter2Mock;

  @Test
  public void build_CreatesInformationProduct_WithCorrectData() {
    // Act
    SparqlBackendInformationProduct result =
        new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
            GRAPH_QUERY, ResultType.GRAPH, queryEvaluator,
            ImmutableList.of(filter1Mock, filter2Mock)).build();

    // Assert
    assertThat(result.getQuery(), equalTo(GRAPH_QUERY));
    assertThat(result.getResultType(), equalTo(ResultType.GRAPH));
    assertThat(result.getIdentifier(), equalTo(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT));
    assertThat(result.getFilters(), contains(filter1Mock, filter2Mock));
  }

  @Test
  public void build_ThrowsException_WithMissingIdentifier() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProduct.Builder(null, backend, GRAPH_QUERY, ResultType.GRAPH,
        queryEvaluator, ImmutableList.of()).build();
  }

  @Test
  public void build_SetsLabel_WithValidLabel() {
    // Arrange
    SparqlBackendInformationProduct.Builder builder =
        new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
            GRAPH_QUERY, ResultType.GRAPH, queryEvaluator, ImmutableList.of());
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
        GRAPH_QUERY, ResultType.GRAPH, queryEvaluator, ImmutableList.of()).build();
  }

  @Test
  public void build_ThrowsException_WithMissingQuery() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
        null, ResultType.GRAPH, queryEvaluator, ImmutableList.of()).build();
  }

  @Test
  public void build_ThrowsException_WithMissingResultType() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
        GRAPH_QUERY, null, queryEvaluator, ImmutableList.of()).build();
  }

  @Test
  public void build_ThrowsException_WithMissingQueryEvaluator() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
        GRAPH_QUERY, ResultType.GRAPH, null, ImmutableList.of()).build();
  }

  @Test
  public void getResult_UsesEvaluator_WhenGettingResult() {
    // Arrange
    Object expectedResult = new Object();
    when(backend.getConnection()).thenReturn(repositoryConnection);
    when(queryEvaluator.evaluate(repositoryConnection, GRAPH_QUERY)).thenReturn(expectedResult);
    SparqlBackendInformationProduct source =
        new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
            GRAPH_QUERY, ResultType.GRAPH, queryEvaluator, ImmutableList.of()).build();

    // Act
    Object result = source.getResult(ImmutableMap.of());

    // Assert
    assertThat(result, equalTo(expectedResult));
  }

  @Test
  public void getResult_UsesFilter_WhenGettingResult() {
    // Arrange
    Map<String, String> values = ImmutableMap.of("name1", "value1", "name2", "value2");

    String originalQuery = "CONSTRUCT { ?s ?p ?o } WHERE { ${name1} ${name2} ?o}";
    String queryAfterFilter1 = "CONSTRUCT { ?s ?p ?o } WHERE { value1 ${name2} ?o}";
    String queryAfterFilter2 = "CONSTRUCT { ?s ?p ?o } WHERE { value1 value2 ?o}";

    when(filter1Mock.getName()).thenReturn("name1");
    when(filter1Mock.filter("value1", originalQuery)).thenReturn(queryAfterFilter1);

    when(filter2Mock.getName()).thenReturn("name2");
    when(filter2Mock.filter("value2", queryAfterFilter1)).thenReturn(queryAfterFilter2);

    Object expectedResult = new Object();

    when(backend.getConnection()).thenReturn(repositoryConnection);
    when(queryEvaluator.evaluate(repositoryConnection, queryAfterFilter2)).thenReturn(
        expectedResult);

    SparqlBackendInformationProduct source =
        new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
            originalQuery, ResultType.GRAPH, queryEvaluator,
            ImmutableList.of(filter1Mock, filter2Mock)).build();

    // Act
    Object result = source.getResult(values);

    // Assert
    assertThat(result, equalTo(expectedResult));
  }

  @Test
  public void getResult_ThrowsException_WhenUnknownFilterNameIsSupplied() {
    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage("No value found for filter 'name1'. Supplied values:");

    // Arrange
    Map<String, String> values = ImmutableMap.of("foo", "value1", "name2", "value2");

    String originalQuery = "CONSTRUCT { ?s ?p ?o } WHERE { ${name1} ${name2} ?o}";

    when(filter1Mock.getName()).thenReturn("name1");

    SparqlBackendInformationProduct source =
        new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
            originalQuery, ResultType.GRAPH, queryEvaluator,
            ImmutableList.of(filter1Mock, filter2Mock)).build();

    // Act
    source.getResult(values);
  }

}
