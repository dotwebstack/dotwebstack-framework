package org.dotwebstack.framework.backend.sparql.informationproduct;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.backend.sparql.informationproduct.SparqlBackendInformationProduct;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.template.TemplateProcessor;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.param.term.StringTermParameter;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Before;
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
  private QueryEvaluator queryEvaluatorMock;

  @Mock
  private TemplateProcessor templateProcessorMock;

  @Mock
  private RepositoryConnection repositoryConnection;

  private Parameter<?> requiredParameter1;

  private Parameter<?> requiredParameter2;

  private Parameter<?> optionalParameter1;

  private Parameter<?> optionalParameter2;

  @Before
  public void setUp() {
    SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

    requiredParameter1 = new StringTermParameter(valueFactory.createIRI("http://foo#", "required1"),
        "nameOfRequired1", true);
    requiredParameter2 = new StringTermParameter(valueFactory.createIRI("http://foo#", "required2"),
        "nameOfRequired2", true);
    optionalParameter1 = new StringTermParameter(valueFactory.createIRI("http://foo#", "optional1"),
        "nameOfOptional1", false);
    optionalParameter2 = new StringTermParameter(valueFactory.createIRI("http://foo#", "optional2"),
        "nameOfOptional2", false);
  }

  @Test
  public void build_CreatesInformationProduct_WithCorrectData() {
    // Act
    SparqlBackendInformationProduct result =
        new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
            GRAPH_QUERY, ResultType.GRAPH, queryEvaluatorMock, templateProcessorMock,
            ImmutableList.of(requiredParameter1, requiredParameter2, optionalParameter1,
                optionalParameter2)).build();

    // Assert
    assertThat(result.getQuery(), equalTo(GRAPH_QUERY));
    assertThat(result.getResultType(), equalTo(ResultType.GRAPH));
    assertThat(result.getIdentifier(), equalTo(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT));
    assertThat(result.getParameters(),
        contains(requiredParameter1, requiredParameter2, optionalParameter1, optionalParameter2));
  }

  @Test
  public void build_ThrowsException_WithMissingIdentifier() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProduct.Builder(null, backend, GRAPH_QUERY, ResultType.GRAPH,
        queryEvaluatorMock, templateProcessorMock, ImmutableList.of()).build();
  }

  @Test
  public void build_SetsLabel_WithValidLabel() {
    // Arrange
    SparqlBackendInformationProduct.Builder builder = new SparqlBackendInformationProduct.Builder(
        DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend, GRAPH_QUERY, ResultType.GRAPH,
        queryEvaluatorMock, templateProcessorMock, ImmutableList.of());
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
        GRAPH_QUERY, ResultType.GRAPH, queryEvaluatorMock, templateProcessorMock,
        ImmutableList.of()).build();
  }

  @Test
  public void build_ThrowsException_WithMissingQuery() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
        null, ResultType.GRAPH, queryEvaluatorMock, templateProcessorMock,
        ImmutableList.of()).build();
  }

  @Test
  public void build_ThrowsException_WithMissingResultType() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
        GRAPH_QUERY, null, queryEvaluatorMock, templateProcessorMock, ImmutableList.of()).build();
  }

  @Test
  public void build_ThrowsException_WithMissingQueryEvaluator() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
        GRAPH_QUERY, ResultType.GRAPH, null, templateProcessorMock, ImmutableList.of()).build();
  }

  @Test
  public void getResult_ModifiesQuery_WithSingleVariable() {
    // Arrange
    when(backend.getConnection()).thenReturn(repositoryConnection);

    String originalQuery = "CONSTRUCT { ?s ?p ?o } WHERE { ${nameOfRequired1}}";
    Map<String, Object> templateValues = Collections.singletonMap("nameOfRequired1", "value1");
    String modifiedQuery = "CONSTRUCT { ?s ?p ?o } WHERE { value1 }";

    when(templateProcessorMock.processString(originalQuery, templateValues)).thenReturn(
        modifiedQuery);

    Object expectedResult = new Object();
    Literal literalValue1 = SimpleValueFactory.getInstance().createLiteral("value1");

    when(queryEvaluatorMock.evaluate(repositoryConnection, modifiedQuery,
        ImmutableMap.of("nameOfRequired1", literalValue1))).thenReturn(expectedResult);

    SparqlBackendInformationProduct source =
        new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
            originalQuery, ResultType.GRAPH, queryEvaluatorMock, templateProcessorMock,
            ImmutableList.of(requiredParameter1)).build();

    Map<String, String> parameterValues = ImmutableMap.of("nameOfRequired1", "value1");

    // Act
    Object result = source.getResult(parameterValues);

    // Assert
    assertThat(result, equalTo(expectedResult));
  }

  @Test
  public void getResult_ModifiesQuery_WithMultipleVariablesRequiredAndOptional() {
    // Arrange
    when(backend.getConnection()).thenReturn(repositoryConnection);

    String originalQuery =
        "CONSTRUCT { ?s ?p ?o } WHERE { ${nameOfRequired1} ${nameOfRequired2} ${nameOfOptional1}}";
    Map<String, Object> templateValues =
        ImmutableMap.of("nameOfRequired1", "value1", "nameOfRequired2", "value2");
    String modifiedQuery = "CONSTRUCT { ?s ?p ?o } WHERE { value1 value2 null}";

    when(templateProcessorMock.processString(originalQuery, templateValues)).thenReturn(
        modifiedQuery);

    Literal literalValue1 = SimpleValueFactory.getInstance().createLiteral("value1");
    Literal literalValue2 = SimpleValueFactory.getInstance().createLiteral("value2");
    Object expectedResult = new Object();

    when(queryEvaluatorMock.evaluate(repositoryConnection, modifiedQuery, ImmutableMap.of(
        "nameOfRequired1", literalValue1, "nameOfRequired2", literalValue2))).thenReturn(
            expectedResult);

    SparqlBackendInformationProduct source =
        new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
            originalQuery, ResultType.GRAPH, queryEvaluatorMock, templateProcessorMock,
            ImmutableList.of(requiredParameter1, requiredParameter2, optionalParameter1,
                optionalParameter2)).build();

    Map<String, String> parameterValues =
        ImmutableMap.of("nameOfRequired1", "value1", "nameOfRequired2", "value2");

    // Act
    Object result = source.getResult(parameterValues);

    // Assert
    assertThat(result, equalTo(expectedResult));
  }

  @Test
  public void getResult_DoesNotModifyQuery_WithoutTemplateVariable() {
    // Arrange
    when(backend.getConnection()).thenReturn(repositoryConnection);

    String originalQuery = "SELECT ?noTemplateVariable";
    Map<String, Object> templateValues = Collections.singletonMap("nameOfRequired1", "value1");
    String unmodifiedQuery = "SELECT ?noTemplateVariable";

    when(templateProcessorMock.processString(originalQuery, templateValues)).thenReturn(
        unmodifiedQuery);

    Literal literalValue1 = SimpleValueFactory.getInstance().createLiteral("value1");
    Object expectedResult = new Object();
    when(queryEvaluatorMock.evaluate(repositoryConnection, unmodifiedQuery,
        ImmutableMap.of("nameOfRequired1", literalValue1))).thenReturn(expectedResult);

    SparqlBackendInformationProduct source =
        new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
            originalQuery, ResultType.GRAPH, queryEvaluatorMock, templateProcessorMock,
            ImmutableList.of(requiredParameter1)).build();

    Map<String, String> parameterValues = ImmutableMap.of("nameOfRequired1", "value1");

    // Act
    Object result = source.getResult(parameterValues);

    // Assert
    assertThat(result, equalTo(expectedResult));
  }

  @Test
  public void getResult_ThrowsException_WhenUnknownParameterNameIsSupplied() {
    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage(
        String.format("No value found for required parameter '%s'. Supplied parameterValues:",
            requiredParameter1.getIdentifier()));

    // Arrange
    String originalQuery =
        "CONSTRUCT { ?s ?p ?o } WHERE { ${nameOfRequired1} ${nameOfRequired2} ?o}";

    SparqlBackendInformationProduct source =
        new SparqlBackendInformationProduct.Builder(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, backend,
            originalQuery, ResultType.GRAPH, queryEvaluatorMock, templateProcessorMock,
            ImmutableList.of(requiredParameter1, requiredParameter2, optionalParameter1,
                optionalParameter2)).build();
    Map<String, String> parameterValues =
        ImmutableMap.of("foo", "value1", "nameOfRequired2", "value2");

    // Act
    source.getResult(parameterValues);
  }

}
