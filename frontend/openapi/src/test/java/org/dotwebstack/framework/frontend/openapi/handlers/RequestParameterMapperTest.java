package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import io.swagger.models.Operation;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.template.TemplateProcessor;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.param.types.TermParameter;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RequestParameterMapperTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private ContainerRequestContext contextMock;

  @Mock
  private TemplateProcessor templateProcessorMock;

  private InformationProduct product;

  private Parameter<?> parameter;

  private Parameter<?> parameter2;

  private RequestParameterMapper mapper;

  private RequestParameters requestParameters;

  @Before
  public void setUp() {
    SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

    parameter = TermParameter.requiredTermParameter(valueFactory.createIRI("http://parameter-iri"),
        "parameter-name");
    parameter2 = TermParameter.requiredTermParameter(
        valueFactory.createIRI("http://parameter2-iri"), "parameter2-name");

    product = new TestInformationProduct(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT,
        DBEERPEDIA.BREWERIES_LABEL.stringValue(), ResultType.GRAPH,
        ImmutableList.of(parameter, parameter2), templateProcessorMock);

    mapper = new RequestParameterMapper();

    requestParameters = new RequestParameters();
    MultivaluedMap<String, String> mvMap = new MultivaluedHashMap<>();
    mvMap.put("param1", ImmutableList.of("value", "valueB"));
    mvMap.put("param2", ImmutableList.of("value2"));
    requestParameters.putAll(mvMap);
  }

  @Test
  public void map_ReturnsEmptyMap_WhenOperationHasNoParameter() {
    // Arrange
    Operation operation = new Operation();

    // Act
    Map<String, String> result = mapper.map(operation, product, requestParameters);

    // Assert
    assertThat(result.isEmpty(), is(true));
  }

  @Test
  public void map_ReturnsEmptyMap_WhenParameterHasNoParameterInputVendorExtension() {
    // Arrange
    Operation operation = new Operation();
    PathParameter pathParameter = new PathParameter();

    pathParameter.setVendorExtension("x-dotwebstack-another-vendor-extension",
        parameter.getIdentifier().stringValue());
    operation.setParameters(ImmutableList.of(pathParameter));

    // Act
    Map<String, String> result = mapper.map(operation, product, requestParameters);

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

    // Act
    mapper.map(operation, product, requestParameters);
  }

  @Test
  public void map_ReturnsCorrectParameterName_ForPathParameters() {
    // Arrange
    PathParameter pathParameter = new PathParameter();

    pathParameter.setName("param1");
    pathParameter.setIn("path");

    // Note this parameter has multiple vendor extensions
    pathParameter.setVendorExtension(OpenApiSpecificationExtensions.PARAMETER,
        parameter.getIdentifier().stringValue());
    pathParameter.setVendorExtension("x-dotwebstack-another-vendor-extension", "foo");

    // Note this operation has multiple parameters
    Operation operation = new Operation();
    operation.addParameter(pathParameter);

    PathParameter pathParameter2 = new PathParameter();

    pathParameter2.setName("param2");
    pathParameter2.setIn("path");
    pathParameter2.setVendorExtension(OpenApiSpecificationExtensions.PARAMETER,
        parameter2.getIdentifier().stringValue());

    operation.addParameter(pathParameter2);

    MultivaluedMap<String, String> pathParameters = new MultivaluedHashMap<>();

    // Note there are multiple values for this parameter, to test that the first value is used only
    pathParameters.put(pathParameter.getName(), ImmutableList.of("value", "valueB"));
    pathParameters.put(pathParameter2.getName(), ImmutableList.of("value2"));

    // Act
    Map<String, String> result = mapper.map(operation, product, requestParameters);

    // Assert
    assertThat(result.size(), is(2));
    assertThat(result, hasEntry(parameter.getName(), "value"));
    assertThat(result, hasEntry(parameter2.getName(), "value2"));
  }

  @Test
  public void map_ReturnsCorrectParameterName_ForQueryParameter() {
    // Arrange
    QueryParameter queryParameter = new QueryParameter();

    queryParameter.setName("param1");
    queryParameter.setIn("query");
    queryParameter.setVendorExtension(OpenApiSpecificationExtensions.PARAMETER,
        parameter.getIdentifier().stringValue());

    Operation operation = new Operation();
    operation.addParameter(queryParameter);

    MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
    queryParameters.put(queryParameter.getName(), ImmutableList.of("value", "valueB"));

    // Act
    Map<String, String> result = mapper.map(operation, product, requestParameters);

    // Assert
    assertThat(result.size(), is(1));
    assertThat(result, hasEntry(parameter.getName(), "value"));
  }

  @Test
  public void map_ReturnsCorrectParameterName_ForHeaderParameter() {
    // Arrange
    HeaderParameter headerParameter = new HeaderParameter();

    headerParameter.setName("param1");
    headerParameter.setIn("header");
    headerParameter.setVendorExtension(OpenApiSpecificationExtensions.PARAMETER,
        parameter.getIdentifier().stringValue());

    Operation operation = new Operation();
    operation.addParameter(headerParameter);

    MultivaluedMap<String, String> headerParameters = new MultivaluedHashMap<>();
    headerParameters.put(headerParameter.getName(), ImmutableList.of("value", "valueB"));

    // Act
    Map<String, String> result = mapper.map(operation, product, requestParameters);

    // Assert
    assertThat(result.size(), is(1));
    assertThat(result, hasEntry(parameter.getName(), "value"));
  }

}
