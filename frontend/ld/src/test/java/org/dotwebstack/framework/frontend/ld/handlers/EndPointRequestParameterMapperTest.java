package org.dotwebstack.framework.frontend.ld.handlers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.template.TemplateProcessor;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.param.term.StringTermParameter;
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
public class EndPointRequestParameterMapperTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private ContainerRequestContext contextMock;

  @Mock
  private TemplateProcessor templateProcessorMock;

  @Mock
  private UriInfo uriInfoMock;

  private InformationProduct product;

  private Parameter<?> requiredParameter;

  private Parameter<?> optionalParameter;

  private EndPointRequestParameterMapper endPointRequestParameterMapper;

  @Before
  public void setUp() {
    // Arrange
    SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

    requiredParameter = new StringTermParameter(
        valueFactory.createIRI("http://required-parameter-iri"), "required-parameter-name", true);
    optionalParameter = new StringTermParameter(
        valueFactory.createIRI("http://optional-parameter-iri"), "optional-parameter-name", false);

    product = new TestInformationProduct(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT,
        DBEERPEDIA.BREWERIES_LABEL.stringValue(), ResultType.GRAPH,
        ImmutableList.of(requiredParameter, optionalParameter), templateProcessorMock);

    endPointRequestParameterMapper = new EndPointRequestParameterMapper();

    uriInfoMock = mock(UriInfo.class);
    when(contextMock.getUriInfo()).thenReturn(uriInfoMock);

  }

  @Test
  public void map_ReturnsEmptyMap_WhenInformationProductHasNoParameter() {
    // Arrange
    TestInformationProduct product = new TestInformationProduct(
        DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, DBEERPEDIA.BREWERIES_LABEL.stringValue(),
        ResultType.GRAPH, ImmutableList.of(), templateProcessorMock);

    // Act
    Map<String, String> result = endPointRequestParameterMapper.map(product, contextMock);

    // Assert
    assertThat(result.isEmpty(), is(true));
  }

  @Test
  public void map_ReturnsRequiredParameter_WithValidData() {
    // Arrange
    MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();

    // Note there are multiple values for this parameter, to test that the first value is used only
    queryParameters.put(requiredParameter.getName(), ImmutableList.of("value", "valueB"));

    when(uriInfoMock.getQueryParameters()).thenReturn(queryParameters);

    // Act
    Map<String, String> result = endPointRequestParameterMapper.map(product, contextMock);

    // Assert
    assertThat(result.size(), is(1));
    assertThat(result, hasEntry(requiredParameter.getName(), "value"));
  }

  @Test
  public void map_NoParameter_WhenRequiredParameterIsMissing() {
    // Arrange
    MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
    when(uriInfoMock.getQueryParameters()).thenReturn(queryParameters);

    // Act
    Map<String, String> result = endPointRequestParameterMapper.map(product, contextMock);

    // Assert
    assertThat(result.size(), is(0));
  }

  @Test
  public void map_ReturnsOptionalParameter_WithValidData() {
    // Arrange
    MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();

    // Note there are multiple values for this parameter, to test that the first value is used only
    queryParameters.put(optionalParameter.getName(), ImmutableList.of("value", "valueB"));

    when(uriInfoMock.getQueryParameters()).thenReturn(queryParameters);

    // Act
    Map<String, String> result = endPointRequestParameterMapper.map(product, contextMock);

    // Assert
    assertThat(result.size(), is(1));
    assertThat(result, hasEntry(optionalParameter.getName(), "value"));
  }

  @Test
  public void map_ReturnsNoParameter_WhenOptionalParameterIsMissing() {
    // Arrange
    MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();

    when(uriInfoMock.getQueryParameters()).thenReturn(queryParameters);

    // Act
    Map<String, String> result = endPointRequestParameterMapper.map(product, contextMock);

    // Assert
    assertThat(result.size(), is(0));
  }

  @Test
  public void map_ReturnsParameters_WithMultipleParameters() {
    // Arrange
    MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();

    // Note there are multiple values for this parameter, to test that the first value is used only
    queryParameters.put(requiredParameter.getName(), ImmutableList.of("value", "valueB"));
    queryParameters.put(optionalParameter.getName(), ImmutableList.of("value2"));

    when(uriInfoMock.getQueryParameters()).thenReturn(queryParameters);

    // Act
    Map<String, String> result = endPointRequestParameterMapper.map(product, contextMock);

    // Assert
    assertThat(result.size(), is(2));
    assertThat(result, hasEntry(requiredParameter.getName(), "value"));
    assertThat(result, hasEntry(optionalParameter.getName(), "value2"));
  }

}
