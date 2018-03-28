package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.atlassian.oai.validator.model.ApiOperation;
import com.atlassian.oai.validator.model.NormalisedPath;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.Operation;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import java.net.URI;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SelfLinkSchemaMapperTest {

  private final String baseUri = "http://localhost:8080/api/v2";

  @Mock
  private GraphEntity graphEntityMock;

  @Mock
  private ValueContext valueContextMock;

  @Mock
  private RequestContext requestContextMock;

  @Mock
  private SchemaMapperAdapter schemaMapperAdapterMock;

  @Mock
  private ApiOperation apiOperation;

  @Mock
  private Operation operation;

  @Mock
  private NormalisedPath requestPath;

  private SelfLinkSchemaMapper schemaMapper;

  private ObjectProperty property;

  @Before
  public void setUp() {
    schemaMapper = new SelfLinkSchemaMapper();

    property = new ObjectProperty();
    property.setVendorExtension(OpenApiSpecificationExtensions.TYPE,
        OpenApiSpecificationExtensions.TYPE_SELF_LINK);

    when(apiOperation.getRequestPath()).thenReturn(requestPath);
    when(apiOperation.getOperation()).thenReturn(operation);

    when(requestContextMock.getBaseUri()).thenReturn(baseUri);
    when(requestContextMock.getApiOperation()).thenReturn(apiOperation);

    when(graphEntityMock.getRequestContext()).thenReturn(requestContextMock);
  }

  @Test
  public void mapGraphValue_ReturnsSimpleLink_WithNoRequestParameters() {
    // Arrange
    when(requestPath.normalised()).thenReturn("/breweries");

    // Act
    Object result = schemaMapper.mapGraphValue(property, graphEntityMock, valueContextMock,
        schemaMapperAdapterMock);

    // Assert
    assertThat(result, equalTo(SchemaMapperUtils.createLink(URI.create(baseUri + "/breweries"))));
  }

  @Test
  public void mapGraphValue_ReturnsPopulatedLink_WithPathParameter() {
    // Arrange
    when(requestPath.normalised()).thenReturn("/breweries/{id}");
    when(requestContextMock.getParameters()).thenReturn(ImmutableMap.of("id", "123"));

    // Act
    Object result = schemaMapper.mapGraphValue(property, graphEntityMock, valueContextMock,
        schemaMapperAdapterMock);

    // Assert
    assertThat(result,
        equalTo(SchemaMapperUtils.createLink(URI.create(baseUri + "/breweries/123"))));
  }

  @Test
  public void mapGraphValue_ReturnsLinkWithQueryParameters_WhenParametersAreSent() {
    // Arrange
    when(requestPath.normalised()).thenReturn("/breweries");
    QueryParameter param = new QueryParameter().name("a");
    param.setDefault("789");
    when(operation.getParameters()).thenReturn(
        ImmutableList.of(param, new QueryParameter().name("b")));
    when(requestContextMock.getParameters()).thenReturn(ImmutableMap.of("a", "123", "b", "456"));

    // Act
    Object result = schemaMapper.mapGraphValue(property, graphEntityMock, valueContextMock,
        schemaMapperAdapterMock);

    // Assert
    assertThat(result,
        equalTo(SchemaMapperUtils.createLink(URI.create(baseUri + "/breweries?a=123&b=456"))));
  }

  @Test
  public void mapGraphValue_ReturnsLinkWithExcludedParameter_WhenParameterEqualDefault() {
    // Arrange
    when(requestPath.normalised()).thenReturn("/breweries");
    QueryParameter param = new QueryParameter().name("a");
    param.setDefault("123");
    when(operation.getParameters()).thenReturn(
        ImmutableList.of(param, new QueryParameter().name("b")));
    when(requestContextMock.getParameters()).thenReturn(ImmutableMap.of("a", "123", "b", "456"));

    // Act
    Object result = schemaMapper.mapGraphValue(property, graphEntityMock, valueContextMock,
        schemaMapperAdapterMock);

    // Assert
    assertThat(result,
        equalTo(SchemaMapperUtils.createLink(URI.create(baseUri + "/breweries?b=456"))));
  }

  @Test
  public void mapGraphValue_IgnoresQueryParameter_ForUnknownParameter() {
    // Arrange
    when(requestPath.normalised()).thenReturn("/breweries");
    when(operation.getParameters()).thenReturn(
        ImmutableList.of(new QueryParameter().name("a"), new QueryParameter().name("b")));
    when(requestContextMock.getParameters()).thenReturn(ImmutableMap.of("a", "123", "c", "456"));

    // Act
    Object result = schemaMapper.mapGraphValue(property, graphEntityMock, valueContextMock,
        schemaMapperAdapterMock);

    // Assert
    assertThat(result,
        equalTo(SchemaMapperUtils.createLink(URI.create(baseUri + "/breweries?a=123"))));
  }

  @Test
  public void supports_ReturnsTrue_ForObjectPropertyWithRequiredVendorExtension() {
    // Act
    boolean result = schemaMapper.supports(property);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void supports_ReturnsFalse_ForNonObjectProperty() {
    // Act
    boolean result = schemaMapper.supports(new ArrayProperty());

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void supports_ReturnsFalse_ForObjectPropertyWithoutRequiredVendorExtension() {
    // Act
    boolean result = schemaMapper.supports(new ObjectProperty());

    // Assert
    assertThat(result, is(false));
  }

}
