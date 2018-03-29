package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.atlassian.oai.validator.model.ApiOperation;
import com.atlassian.oai.validator.model.NormalisedPath;
import io.swagger.models.Operation;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import java.net.URI;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
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
  private TupleEntity tupleEntityMock;

  @Mock
  private GraphEntity graphEntityMock;

  @Mock
  private ValueContext valueContextMock;

  @Mock
  private RequestContext requestContextMock;

  @Mock
  private SchemaMapperAdapter schemaMapperAdapterMock;

  @Mock
  private ApiOperation apiOperationMock;

  @Mock
  private Operation operationMock;

  @Mock
  private NormalisedPath requestPathMock;

  private SelfLinkSchemaMapper schemaMapper;

  private ObjectProperty schema;

  @Before
  public void setUp() {
    schemaMapper = new SelfLinkSchemaMapper();

    schema = new ObjectProperty();
    schema.setVendorExtension(OpenApiSpecificationExtensions.TYPE,
        OpenApiSpecificationExtensions.TYPE_SELF_LINK);

    when(apiOperationMock.getRequestPath()).thenReturn(requestPathMock);
    when(apiOperationMock.getOperation()).thenReturn(operationMock);

    when(requestContextMock.getBaseUri()).thenReturn(baseUri);
    when(requestContextMock.getApiOperation()).thenReturn(apiOperationMock);

    when(tupleEntityMock.getRequestContext()).thenReturn(requestContextMock);
    when(graphEntityMock.getRequestContext()).thenReturn(requestContextMock);
  }

  @Test
  public void mapTupleValue_ReturnsLink_WhenInvoked() {
    // Arrange
    when(requestPathMock.normalised()).thenReturn("/breweries");

    // Act
    Object result = schemaMapper.mapTupleValue(schema, tupleEntityMock, valueContextMock);

    // Assert
    assertThat(result, equalTo(SchemaMapperUtils.createLink(URI.create(baseUri + "/breweries"))));
  }

  @Test
  public void mapGraphValue_ReturnsLink_WhenInvoked() {
    // Arrange
    when(requestPathMock.normalised()).thenReturn("/breweries");

    // Act
    Object result = schemaMapper.mapGraphValue(schema, graphEntityMock, valueContextMock,
        schemaMapperAdapterMock);

    // Assert
    assertThat(result, equalTo(SchemaMapperUtils.createLink(URI.create(baseUri + "/breweries"))));
  }

  @Test
  public void supports_ReturnsTrue_ForObjectPropertyWithRequiredVendorExtension() {
    // Act
    boolean result = schemaMapper.supports(schema);

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
