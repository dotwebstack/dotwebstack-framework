package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.swagger.models.Operation;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.filter.Filter;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.informationproduct.InformationProduct;
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
public class FilterNameToParameterValueMapperTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private ContainerRequestContext contextMock;

  private InformationProduct product;

  private Filter filter;

  private Filter filter2;

  private FilterNameToParameterValueMapper mapper;

  @Before
  public void setUp() {
    SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

    filter = new TestFilter(valueFactory.createIRI("http://filter-iri"), "filter-name");
    filter2 = new TestFilter(valueFactory.createIRI("http://filter2-iri"), "filter2-name");

    product = new TestInformationProduct(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT,
        DBEERPEDIA.BREWERIES_LABEL.stringValue(), ResultType.GRAPH,
        ImmutableList.of(filter, filter2), ImmutableList.of());

    mapper = new FilterNameToParameterValueMapper();
  }

  @Test
  public void map_ReturnsEmptyMap_WhenOperationHasNoParameter() {
    Operation operation = new Operation();

    Map<String, String> result = mapper.map(operation, product, contextMock);

    assertThat(result.isEmpty(), is(true));
  }

  @Test
  public void map_ReturnsEmptyMap_WhenParameterHasNoFilterInputVendorExtension() {
    Operation operation = new Operation();
    PathParameter parameter = new PathParameter();

    parameter.setVendorExtension("x-dotwebstack-another-vendor-extension",
        filter.getIdentifier().stringValue());
    operation.setParameters(ImmutableList.of(parameter));

    Map<String, String> result = mapper.map(operation, product, contextMock);

    assertThat(result.isEmpty(), is(true));
  }

  @Test
  public void map_ThrowsException_ForUnknownFilterName() {
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("No filter found for vendor extension value:");

    Operation operation = new Operation();
    PathParameter parameter = new PathParameter();

    parameter.setVendorExtension(OpenApiSpecificationExtensions.FILTER_INPUT, "http://unknown");
    operation.setParameters(ImmutableList.of(parameter));

    mapper.map(operation, product, contextMock);
  }

  @Test
  public void map_ThrowsException_ForUnknownParameterLocation() {
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Unknown parameter location:");

    Operation operation = new Operation();
    PathParameter parameter = new PathParameter();

    parameter.setIn("unknown");
    parameter.setVendorExtension(OpenApiSpecificationExtensions.FILTER_INPUT,
        filter.getIdentifier().stringValue());

    operation.setParameters(ImmutableList.of(parameter));

    mapper.map(operation, product, contextMock);
  }

  @Test
  public void map_ReturnsCorrectFilterName_ForPathParameters() {
    PathParameter parameter = new PathParameter();

    parameter.setName("param");
    parameter.setIn("path");

    // Note this parameter has multiple vendor extensions
    parameter.setVendorExtension(OpenApiSpecificationExtensions.FILTER_INPUT,
        filter.getIdentifier().stringValue());
    parameter.setVendorExtension("x-dotwebstack-another-vendor-extension", "foo");

    // Note this operation has multiple parameters
    Operation operation = new Operation();
    operation.addParameter(parameter);

    PathParameter parameter2 = new PathParameter();

    parameter2.setName("param2");
    parameter2.setIn("path");
    parameter2.setVendorExtension(OpenApiSpecificationExtensions.FILTER_INPUT,
        filter2.getIdentifier().stringValue());

    operation.addParameter(parameter2);

    UriInfo uriInfoMock = mock(UriInfo.class);
    when(contextMock.getUriInfo()).thenReturn(uriInfoMock);

    MultivaluedMap<String, String> pathParameters = new MultivaluedHashMap<>();

    // Note there are multiple values for this parameter, to test that the first value is used only
    pathParameters.put(parameter.getName(), ImmutableList.of("value", "valueB"));
    pathParameters.put(parameter2.getName(), ImmutableList.of("value2"));

    when(uriInfoMock.getPathParameters()).thenReturn(pathParameters);

    Map<String, String> result = mapper.map(operation, product, contextMock);

    assertThat(result.size(), is(2));
    assertThat(result, hasEntry(filter.getName(), "value"));
    assertThat(result, hasEntry(filter2.getName(), "value2"));
  }

  @Test
  public void map_ReturnsCorrectFilterName_ForQueryParameter() {
    QueryParameter parameter = new QueryParameter();

    parameter.setName("param1");
    parameter.setIn("query");
    parameter.setVendorExtension(OpenApiSpecificationExtensions.FILTER_INPUT,
        filter.getIdentifier().stringValue());

    Operation operation = new Operation();
    operation.addParameter(parameter);

    UriInfo uriInfoMock = mock(UriInfo.class);

    when(contextMock.getUriInfo()).thenReturn(uriInfoMock);

    MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
    queryParameters.put(parameter.getName(), ImmutableList.of("value", "valueB"));

    when(uriInfoMock.getQueryParameters()).thenReturn(queryParameters);

    Map<String, String> result = mapper.map(operation, product, contextMock);

    assertThat(result.size(), is(1));
    assertThat(result, hasEntry(filter.getName(), "value"));
  }

  @Test
  public void map_ReturnsCorrectFilterName_ForHeaderParameter() {
    HeaderParameter parameter = new HeaderParameter();

    parameter.setName("param1");
    parameter.setIn("header");
    parameter.setVendorExtension(OpenApiSpecificationExtensions.FILTER_INPUT,
        filter.getIdentifier().stringValue());

    Operation operation = new Operation();
    operation.addParameter(parameter);

    MultivaluedMap<String, String> headerParameters = new MultivaluedHashMap<>();
    headerParameters.put(parameter.getName(), ImmutableList.of("value", "valueB"));

    when(contextMock.getHeaders()).thenReturn(headerParameters);

    Map<String, String> result = mapper.map(operation, product, contextMock);

    assertThat(result.size(), is(1));
    assertThat(result, hasEntry(filter.getName(), "value"));
  }

}
