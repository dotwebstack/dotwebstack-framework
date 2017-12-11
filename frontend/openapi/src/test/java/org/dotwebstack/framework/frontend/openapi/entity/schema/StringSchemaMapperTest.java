package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.StringProperty;
import java.util.Arrays;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.dotwebstack.framework.frontend.openapi.entity.SchemaMapperContextImpl;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Value;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StringSchemaMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private StringSchemaMapper schemaMapper;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private GraphEntityContext entityBuilderContextMock;

  @Mock
  private Value contextMock;

  private SchemaMapperAdapter registry;

  private SchemaMapper handler;
  private StringProperty stringProperty;

  @Before
  public void setUp() {
    schemaMapper = new StringSchemaMapper();
    entityBuilderContextMock = mock(GraphEntityContext.class);
    handler = new StringSchemaMapper();
    stringProperty = new StringProperty();
    registry = new SchemaMapperAdapter(Arrays.asList(handler));
  }

  @Test
  public void mapTupleValue_ReturnValue_ForLiterals() {
    // Arrange & Act
    String result = schemaMapper.mapTupleValue(stringProperty,
        SchemaMapperContextImpl.builder().value(DBEERPEDIA.BROUWTOREN_NAME).build());

    // Assert
    assertThat(result, equalTo(DBEERPEDIA.BROUWTOREN_NAME.stringValue()));
  }

  @Test
  public void supports_ReturnsTrue_ForStringSchema() {
    // Arrange & Act
    Boolean supported = schemaMapper.supports(stringProperty);

    // Assert
    assertThat(supported, equalTo(true));
  }

  @Test
  public void supports_ReturnsTrue_ForNonStringSchema() {
    // Arrange & Act
    Boolean supported = schemaMapper.supports(new IntegerProperty());

    // Assert
    assertThat(supported, equalTo(false));
  }

  @Test
  public void supportsStringProperty() {
    assertThat(handler.supports(stringProperty), Matchers.equalTo(true));
  }

  @Test
  public void handleNoVendorExtensions() {
    Object result = registry.mapGraphValue(stringProperty, entityBuilderContextMock,
        SchemaMapperContextImpl.builder().value(contextMock).build(), registry);

    assertThat(result, nullValue());
  }

  @Test
  public void handleMultipleVendorExtensionsLdPathQueryAndRelativeLinkThrowsException() {
    stringProperty.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH, "",
        OpenApiSpecificationExtensions.RELATIVE_LINK, ImmutableMap.of()));

    expectExceptionAboutMultipleVendorExtensions();

    registry.mapGraphValue(stringProperty, entityBuilderContextMock,
        SchemaMapperContextImpl.builder().value(contextMock).build(), registry);
  }

  @Test
  public void handleMultipleVendorExtensionsLdPathQueryAndConstValueThrowsException() {
    stringProperty.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH,
        ImmutableMap.of(), OpenApiSpecificationExtensions.CONSTANT_VALUE, ImmutableMap.of()));

    expectExceptionAboutMultipleVendorExtensions();

    registry.mapGraphValue(stringProperty, entityBuilderContextMock,
        SchemaMapperContextImpl.builder().value(contextMock).build(), registry);
  }

  @Test
  public void handleMultipleVendorExtensionsRelativeLinkAndConstValueThrowsException() {
    stringProperty.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.RELATIVE_LINK,
        ImmutableMap.of(), OpenApiSpecificationExtensions.CONSTANT_VALUE, ImmutableMap.of()));

    expectExceptionAboutMultipleVendorExtensions();

    registry.mapGraphValue(stringProperty, entityBuilderContextMock,
        SchemaMapperContextImpl.builder().value(contextMock).build(), registry);
  }

  @Test
  public void handleThreeVendorExtensionsThrowsException() {
    stringProperty.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.RELATIVE_LINK,
        ImmutableMap.of(), OpenApiSpecificationExtensions.CONSTANT_VALUE, ImmutableMap.of(),
        OpenApiSpecificationExtensions.LDPATH, ImmutableMap.of()));

    expectExceptionAboutMultipleVendorExtensions();

    registry.mapGraphValue(stringProperty, entityBuilderContextMock,
        SchemaMapperContextImpl.builder().value(contextMock).build(), registry);
  }

  private void expectExceptionAboutMultipleVendorExtensions() {
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage(String.format(
        "A string object must have either no, a '%s', '%s' or '%s' property. "
            + "A string object cannot have a combination of these.",
        OpenApiSpecificationExtensions.LDPATH, OpenApiSpecificationExtensions.RELATIVE_LINK,
        OpenApiSpecificationExtensions.CONSTANT_VALUE));
  }

}
