package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.atlassian.oai.validator.model.ApiOperation;
import com.atlassian.oai.validator.model.NormalisedPath;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.swagger.models.Operation;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import java.net.URI;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestContext;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.term.IntegerTermParameter;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("static-access")
public class AbstractLinkSchemaMapperTest {

  private final String baseUri = "http://localhost:8080/api/v2";

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private RequestContext requestContextMock;

  @Mock
  private InformationProduct informationProductMock;

  @Mock
  private ApiOperation apiOperationMock;

  @Mock
  private Operation operationMock;

  @Mock
  private NormalisedPath requestPathMock;

  private ObjectProperty objectProperty;
  private AbstractLinkSchemaMapper abstractLinkSchemaMapper;

  @Before
  public void setUp() {
    abstractLinkSchemaMapper = new TestLinkSchemaMapper();

    objectProperty = new ObjectProperty();
    objectProperty.setVendorExtension(OpenApiSpecificationExtensions.TYPE,
        OpenApiSpecificationExtensions.TYPE_SELF_LINK);

    when(apiOperationMock.getRequestPath()).thenReturn(requestPathMock);
    when(apiOperationMock.getOperation()).thenReturn(operationMock);

    when(requestContextMock.getBaseUri()).thenReturn(baseUri);
    when(requestContextMock.getApiOperation()).thenReturn(apiOperationMock);
    when(requestContextMock.getInformationProduct()).thenReturn(informationProductMock);
  }

  @Test
  public void buildUri_ReturnsSimpleLink_WithNoRequestParameters() {
    // Arrange
    when(requestPathMock.normalised()).thenReturn("/breweries");

    // Act
    URI result = abstractLinkSchemaMapper.buildUri(requestContextMock, null);

    // Assert
    assertThat(result, equalTo(URI.create(baseUri + "/breweries")));
  }

  @Test
  public void buildUri_ReturnsPopulatedLink_WithPathParameter() {
    // Arrange
    when(requestPathMock.normalised()).thenReturn("/breweries/{id}");
    when(requestContextMock.getParameters()).thenReturn(ImmutableMap.of("id", "123"));

    // Act
    URI result = abstractLinkSchemaMapper.buildUri(requestContextMock, null);

    // Assert
    assertThat(result, equalTo(URI.create(baseUri + "/breweries/123")));
  }

  @Test
  public void buildUri_ReturnsLinkWithQueryParameters_WhenParametersAreSent() {
    // Arrange
    when(requestPathMock.normalised()).thenReturn("/breweries");
    QueryParameter param = new QueryParameter().name("a");
    param.setDefault("789");
    when(operationMock.getParameters()).thenReturn(
        ImmutableList.of(param, new QueryParameter().name("b")));
    when(requestContextMock.getParameters()).thenReturn(ImmutableMap.of("a", "123", "b", "456"));

    // Act
    URI result = abstractLinkSchemaMapper.buildUri(requestContextMock, null);

    // Assert
    assertThat(result, equalTo(URI.create(baseUri + "/breweries?a=123&b=456")));
  }

  @Test
  public void buildUri_ReturnsLinkWithExcludedParameter_WhenParameterEqualDefault() {
    // Arrange
    when(requestPathMock.normalised()).thenReturn("/breweries");
    QueryParameter param = new QueryParameter().name("a");
    param.setDefault("123");
    when(operationMock.getParameters()).thenReturn(
        ImmutableList.of(param, new QueryParameter().name("b")));
    when(requestContextMock.getParameters()).thenReturn(ImmutableMap.of("a", "123", "b", "456"));

    // Act
    URI result = abstractLinkSchemaMapper.buildUri(requestContextMock, null);

    // Assert
    assertThat(result, equalTo(URI.create(baseUri + "/breweries?b=456")));
  }

  @Test
  public void buildUri_IgnoresQueryParameter_ForUnknownParameter() {
    // Arrange
    when(requestPathMock.normalised()).thenReturn("/breweries");
    when(operationMock.getParameters()).thenReturn(
        ImmutableList.of(new QueryParameter().name("a"), new QueryParameter().name("b")));
    when(requestContextMock.getParameters()).thenReturn(ImmutableMap.of("a", "123", "c", "456"));

    // Act
    URI result = abstractLinkSchemaMapper.buildUri(requestContextMock, null);

    // Assert
    assertThat(result, equalTo(URI.create(baseUri + "/breweries?a=123")));
  }

  @Test
  public void buildUri_AppendsParams_WhenExtraParamsGiven() {
    // Arrange
    when(requestPathMock.normalised()).thenReturn("/breweries");
    when(operationMock.getParameters()).thenReturn(
        ImmutableList.of(new QueryParameter().name("a"), new QueryParameter().name("b")));
    when(requestContextMock.getParameters()).thenReturn(ImmutableMap.of("a", "123"));

    // Act
    URI result = abstractLinkSchemaMapper.buildUri(requestContextMock, ImmutableMap.of("b", "456"));

    // Assert
    assertThat(result, equalTo(URI.create(baseUri + "/breweries?a=123&b=456")));
  }

  @Test
  public void buildUri_OverwritesParams_WhenExtraParamsOverlapExistingParams() {
    // Arrange
    when(requestPathMock.normalised()).thenReturn("/breweries");
    when(operationMock.getParameters()).thenReturn(
        ImmutableList.of(new QueryParameter().name("a"), new QueryParameter().name("b")));
    when(requestContextMock.getParameters()).thenReturn(ImmutableMap.of("a", "123"));

    // Act
    URI result = abstractLinkSchemaMapper.buildUri(requestContextMock, ImmutableMap.of("a", "456"));

    // Assert
    assertThat(result, equalTo(URI.create(baseUri + "/breweries?a=456")));
  }

  @Test
  public void getPageTermParameter_ReturnsTermParameter_WhenFound() {
    // Arrange
    IntegerTermParameter parameter = new IntegerTermParameter(ELMO.PAGE_PARAMETER, "page", false);
    when(informationProductMock.getParameters()).thenReturn(ImmutableSet.of(parameter));

    // Act
    IntegerTermParameter result = abstractLinkSchemaMapper.getPageTermParameter(requestContextMock);

    // Assert
    assertThat(result, sameInstance(parameter));
  }

  @Test
  public void getPageTermParameter_ThrowsException_WhenNotFound() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);

    // Arrange
    when(informationProductMock.getParameters()).thenReturn(ImmutableSet.of());

    // Act
    abstractLinkSchemaMapper.getPageTermParameter(requestContextMock);
  }

  @Test
  public void getPageSizeTermParameter_ReturnsTermParameter_WhenFound() {
    // Arrange
    IntegerTermParameter parameter =
        new IntegerTermParameter(ELMO.PAGE_SIZE_PARAMETER, "pageSize", false);
    when(informationProductMock.getParameters()).thenReturn(ImmutableSet.of(parameter));

    // Act
    IntegerTermParameter result =
        abstractLinkSchemaMapper.getPageSizeTermParameter(requestContextMock);

    // Assert
    assertThat(result, sameInstance(parameter));
  }

  @Test
  public void getPageSizeTermParameter_ThrowsException_WhenNotFound() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);

    // Arrange
    when(informationProductMock.getParameters()).thenReturn(ImmutableSet.of());

    // Act
    abstractLinkSchemaMapper.getPageSizeTermParameter(requestContextMock);
  }

  @Test
  public void getPageQueryParameter_ReturnsQueryParameter_WhenFound() {
    // Arrange
    QueryParameter parameter = new QueryParameter();
    parameter.setVendorExtension(OpenApiSpecificationExtensions.PARAMETER,
        ELMO.PAGE_PARAMETER.stringValue());
    when(operationMock.getParameters()).thenReturn(ImmutableList.of(parameter));

    // Act
    QueryParameter result = abstractLinkSchemaMapper.getPageQueryParameter(requestContextMock);

    // Assert
    assertThat(result, sameInstance(parameter));
  }

  @Test
  public void getPageQueryParameter_ThrowsException_WhenNotFound() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);

    // Arrange
    when(operationMock.getParameters()).thenReturn(ImmutableList.of());

    // Act
    abstractLinkSchemaMapper.getPageQueryParameter(requestContextMock);
  }

  private static class TestLinkSchemaMapper extends AbstractLinkSchemaMapper {

    @Override
    public Object mapTupleValue(ObjectProperty schema, TupleEntity entity,
        ValueContext valueContext) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object mapGraphValue(ObjectProperty schema, GraphEntity entity,
        ValueContext valueContext, SchemaMapperAdapter schemaMapperAdapter) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean supports(Property schema) {
      throw new UnsupportedOperationException();
    }

  }

}
