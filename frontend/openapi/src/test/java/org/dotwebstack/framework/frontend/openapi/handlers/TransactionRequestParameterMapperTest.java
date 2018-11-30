package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.param.term.StringTermParameter;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.transaction.Transaction;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionRequestParameterMapperTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private ContainerRequestContext contextMock;

  private Transaction transaction;

  private Parameter<?> parameter1;

  private Parameter<?> parameter2;

  private TransactionRequestParameterMapper transactionRequestParameterMapper;

  private RequestParameters requestParameters;

  @Before
  public void setUp() {
    SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

    parameter1 =
        new StringTermParameter(valueFactory.createIRI("http://parameter1-iri"), "param1", true);
    parameter2 =
        new StringTermParameter(valueFactory.createIRI("http://parameter2-iri"), "param2", true);

    transaction = new Transaction.Builder(DBEERPEDIA.TRANSACTION).parameters(
        ImmutableList.of(parameter1, parameter2)).build();

    transactionRequestParameterMapper =
        new TransactionRequestParameterMapper(new RequestParameterMapperHelper());
  }

  @Test
  public void map_ReturnsEmptyMap_WhenOperationHasNoParameter() {
    // Arrange
    MultivaluedMap<String, String> mvMap = new MultivaluedHashMap<>();
    mvMap.put("param1", ImmutableList.of("value1", "valueB"));
    mvMap.put("param2", ImmutableList.of("value2"));

    requestParameters = new RequestParameters();
    requestParameters.putAll(mvMap);

    Operation operation = new Operation();

    // Act
    Map<String, String> result = transactionRequestParameterMapper.map(operation, transaction,
        requestParameters);

    // Assert
    assertThat(result.isEmpty(), is(true));
  }

  @Test
  public void map_ReturnsEmptyMap_WhenParameterHasNoParameterInputVendorExtension() {
    // Arrange
    Operation operation = new Operation();
    PathParameter pathParameter = new PathParameter();

    pathParameter.addExtension("x-dotwebstack-another-vendor-extension",
        parameter1.getIdentifier().stringValue());
    operation.setParameters(ImmutableList.of(pathParameter));

    MultivaluedMap<String, String> mvMap = new MultivaluedHashMap<>();
    mvMap.put("param1", ImmutableList.of("value1", "valueB"));
    mvMap.put("param2", ImmutableList.of("value2"));

    requestParameters = new RequestParameters();
    requestParameters.putAll(mvMap);

    // Act
    Map<String, String> result = transactionRequestParameterMapper.map(operation, transaction,
        requestParameters);

    // Assert
    assertThat(result.isEmpty(), is(true));
  }

  @Test
  public void map_ThrowsException_ForUnknownParameterName() {
    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("No parameter found for vendor extension value:");

    // Arrange
    Operation operation = new Operation();
    PathParameter parameter = new PathParameter();

    parameter.addExtension(OpenApiSpecificationExtensions.PARAMETER, "http://unknown");
    operation.setParameters(ImmutableList.of(parameter));

    MultivaluedMap<String, String> mvMap = new MultivaluedHashMap<>();
    mvMap.put("param1", ImmutableList.of("value1", "valueB"));
    mvMap.put("param2", ImmutableList.of("value2"));

    requestParameters = new RequestParameters();
    requestParameters.putAll(mvMap);

    // Act
    transactionRequestParameterMapper.map(operation, transaction, requestParameters);
  }

  @Test
  public void map_ReturnsCorrectParameterName_ForPathParameters() {
    // Arrange
    PathParameter pathParameter1 = new PathParameter();
    pathParameter1.setName("param1");
    pathParameter1.setIn("path");

    // Note this parameter has multiple vendor extensions
    pathParameter1.addExtension(OpenApiSpecificationExtensions.PARAMETER,
        parameter1.getIdentifier().stringValue());
    pathParameter1.addExtension("x-dotwebstack-another-vendor-extension", "foo");

    // Note this operation has multiple parameters
    Operation operation = new Operation();
    operation.addParametersItem(pathParameter1);

    PathParameter pathParameter2 = new PathParameter();
    pathParameter2.setName("param2");
    pathParameter2.setIn("path");
    pathParameter2.addExtension(OpenApiSpecificationExtensions.PARAMETER,
        parameter2.getIdentifier().stringValue());

    operation.addParametersItem(pathParameter2);

    MultivaluedMap<String, String> pathParameters = new MultivaluedHashMap<>();

    // Note there are multiple values for this parameter, to test that the first value is used only
    pathParameters.put(pathParameter1.getName(), ImmutableList.of("value1", "valueB"));
    pathParameters.put(pathParameter2.getName(), ImmutableList.of("value2"));

    requestParameters = new RequestParameters();
    requestParameters.putAll(pathParameters);

    // Act
    Map<String, String> result = transactionRequestParameterMapper.map(operation, transaction,
        requestParameters);

    // Assert
    assertThat(result.size(), is(2));
    assertThat(result, hasEntry(parameter1.getName(), "value1"));
    assertThat(result, hasEntry(parameter2.getName(), "value2"));
  }

  @Test
  public void map_ReturnsCorrectParameterName_ForQueryParameter() {
    // Arrange
    QueryParameter queryParameter = new QueryParameter();
    queryParameter.setName("param1");
    queryParameter.setIn("query");
    queryParameter.addExtension(OpenApiSpecificationExtensions.PARAMETER,
        parameter1.getIdentifier().stringValue());

    Operation operation = new Operation();
    operation.addParametersItem(queryParameter);

    MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
    queryParameters.put(queryParameter.getName(), ImmutableList.of("value1", "valueB"));

    requestParameters = new RequestParameters();
    requestParameters.putAll(queryParameters);

    // Act
    Map<String, String> result = transactionRequestParameterMapper.map(operation, transaction,
        requestParameters);

    // Assert
    assertThat(result.size(), is(1));
    assertThat(result, hasEntry(parameter1.getName(), "value1"));
  }

  @Test
  public void map_ReturnsCorrectParameterName_ForHeaderParameter() {
    // Arrange
    HeaderParameter headerParameter = new HeaderParameter();
    headerParameter.setName("param1");
    headerParameter.setIn("header");
    headerParameter.addExtension(OpenApiSpecificationExtensions.PARAMETER,
        parameter1.getIdentifier().stringValue());

    Operation operation = new Operation();
    operation.addParametersItem(headerParameter);

    MultivaluedMap<String, String> headerParameters = new MultivaluedHashMap<>();
    headerParameters.put(headerParameter.getName(), ImmutableList.of("value1", "valueB"));

    requestParameters = new RequestParameters();
    requestParameters.putAll(headerParameters);

    // Act
    Map<String, String> result = transactionRequestParameterMapper.map(operation, transaction,
        requestParameters);

    // Assert
    assertThat(result.size(), is(1));
    assertThat(result, hasEntry(parameter1.getName(), "value1"));
  }

  @Test
  public void map_ReturnsNothing_ForBodyParameter() {
    // Arrange
    Schema property = new ObjectSchema();
    property.addExtension(OpenApiSpecificationExtensions.PARAMETER,
        parameter1.getIdentifier().stringValue());

    Schema schema = new ObjectSchema();
    schema.setProperties(ImmutableMap.of("param1", property));

    RequestBody requestBody = new RequestBody();
    requestBody.setDescription("body");
    requestBody.setContent(new Content().addMediaType("", new MediaType().schema(schema)));

    Operation operation = new Operation();
    operation.setRequestBody(requestBody);

    MultivaluedMap<String, String> bodyParameters = new MultivaluedHashMap<>();
    bodyParameters.put("param1", ImmutableList.of("value1"));

    requestParameters = new RequestParameters();
    requestParameters.putAll(bodyParameters);

    // Act
    Map<String, String> result = transactionRequestParameterMapper.map(operation, transaction,
        requestParameters);

    // Assert
    assertThat(result.isEmpty(), is(true));
  }

}
