package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
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

    transaction = new Transaction.Builder(DBEERPEDIA.TRANSACTION)
        .parameters(ImmutableList.of(parameter1, parameter2)).build();

    transactionRequestParameterMapper = new TransactionRequestParameterMapper();
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

    pathParameter.setVendorExtension("x-dotwebstack-another-vendor-extension",
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

    parameter.setVendorExtension(OpenApiSpecificationExtensions.PARAMETER, "http://unknown");
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
    pathParameter1.setVendorExtension(OpenApiSpecificationExtensions.PARAMETER,
        parameter1.getIdentifier().stringValue());
    pathParameter1.setVendorExtension("x-dotwebstack-another-vendor-extension", "foo");

    // Note this operation has multiple parameters
    Operation operation = new Operation();
    operation.addParameter(pathParameter1);

    PathParameter pathParameter2 = new PathParameter();
    pathParameter2.setName("param2");
    pathParameter2.setIn("path");
    pathParameter2.setVendorExtension(OpenApiSpecificationExtensions.PARAMETER,
        parameter2.getIdentifier().stringValue());

    operation.addParameter(pathParameter2);

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
    queryParameter.setVendorExtension(OpenApiSpecificationExtensions.PARAMETER,
        parameter1.getIdentifier().stringValue());

    Operation operation = new Operation();
    operation.addParameter(queryParameter);

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
    headerParameter.setVendorExtension(OpenApiSpecificationExtensions.PARAMETER,
        parameter1.getIdentifier().stringValue());

    Operation operation = new Operation();
    operation.addParameter(headerParameter);

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
    Property property = new ObjectProperty();
    property.getVendorExtensions().put(OpenApiSpecificationExtensions.PARAMETER,
        parameter1.getIdentifier().stringValue());

    Model schema = new ModelImpl();
    schema.setProperties(ImmutableMap.of("param1", property));

    BodyParameter bodyParameter = new BodyParameter();
    bodyParameter.setName("body");
    bodyParameter.setIn("body");
    bodyParameter.setSchema(schema);

    Operation operation = new Operation();
    operation.addParameter(bodyParameter);

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
