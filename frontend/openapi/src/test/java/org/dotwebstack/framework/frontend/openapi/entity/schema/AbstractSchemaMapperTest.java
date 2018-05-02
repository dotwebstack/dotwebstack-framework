package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.eclipse.rdf4j.model.vocabulary.RDF.VALUE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractSchemaMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private GraphEntity graphEntityMock;
  @Mock
  private Property propertyMock;
  @Mock
  private LdPathExecutor ldPathExecutorMock;

  private SchemaMapperAdapter schemaMapperAdapter;
  private final AbstractSchemaMapper abstractSchemaMapper =
      new AbstractSchemaMapperTest.TestSchemaMapper();

  @Before
  public void setUp() {
    schemaMapperAdapter = new SchemaMapperAdapter(Collections.singletonList(abstractSchemaMapper));
    when(propertyMock.getVendorExtensions()).thenReturn(new HashMap<>());
  }

  @Test
  public void hasVendorExtension_ReturnsTrue_WhenPropertyHasExtension() {
    // Arrange
    StringProperty schema = new StringProperty();

    schema.setVendorExtension(OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL,
        true);

    // Act
    boolean result = AbstractSchemaMapper.hasVendorExtension(schema,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void hasVendorExtension_ReturnsFalse_WhenPropertyHasExtension() {
    // Arrange
    StringProperty schema = new StringProperty();

    schema.setVendorExtension(OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL,
        true);

    // Act
    boolean result =
        AbstractSchemaMapper.hasVendorExtension(schema, OpenApiSpecificationExtensions.LDPATH);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void hasVendorExtensionWithValue_ReturnsTrue_WhenPropertyHasExtensionWithValue() {
    // Arrange
    StringProperty schema = new StringProperty();

    schema.setVendorExtension(OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL,
        true);

    // Act
    boolean result = AbstractSchemaMapper.hasVendorExtensionWithValue(schema,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void hasVendorExtensionWithValue_ReturnsFalse_WhenPropertyHasExtensionWithoutValue() {
    // Arrange
    StringProperty schema = new StringProperty();

    schema.setVendorExtension(OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL,
        true);

    // Act
    boolean result = AbstractSchemaMapper.hasVendorExtensionWithValue(schema,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, false);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void hasVendorExtensionWithValue_ReturnsFalse_WhenPropertyDoesNotHaveExtension() {
    // Arrange
    StringProperty schema = new StringProperty();

    schema.setVendorExtension(OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL,
        true);

    // Act
    boolean result = AbstractSchemaMapper.hasVendorExtensionWithValue(schema,
        OpenApiSpecificationExtensions.LDPATH, false);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void mapGraphValue_ThrowsException_WhenNoLdPathHasBeenSupplied() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("object must have one of:");
    thrown.expectMessage("This object cannot have a combination of these.");

    // Act
    abstractSchemaMapper.mapGraphValue(propertyMock, graphEntityMock,
        ValueContext.builder().value(VALUE).build(), schemaMapperAdapter);

    // Assert
    verifyZeroInteractions(ldPathExecutorMock);
  }

  @Test
  public void mapGraphValue_ThrowsException_ForNullConstantAndRequiredProperty() {
    // Arrange
    when(propertyMock.getVendorExtensions()).thenReturn(nullableMapOfConstantValue());
    when(propertyMock.getRequired()).thenReturn(true);

    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("x-dotwebstack-constant-value");
    thrown.expectMessage("is null");
    thrown.expectMessage("required");

    // Act
    abstractSchemaMapper.mapGraphValue(propertyMock, graphEntityMock,
        ValueContext.builder().build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ReturnsNull_ForNullConstantValue() {
    // Arrange
    when(propertyMock.getVendorExtensions()).thenReturn(nullableMapOfConstantValue());

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(propertyMock, graphEntityMock,
        ValueContext.builder().build(), schemaMapperAdapter);

    // Assert
    assertNull(result);
  }

  private static Map<String, Object> nullableMapOfConstantValue() {
    String constantValue = OpenApiSpecificationExtensions.CONSTANT_VALUE;
    Map<String, Object> result = new HashMap<>();

    result.put(constantValue, null);

    return result;
  }

  private static class TestSchemaMapper extends AbstractSchemaMapper {

    @Override
    protected Set<String> getSupportedVendorExtensions() {
      return ImmutableSet.of(OpenApiSpecificationExtensions.CONSTANT_VALUE,
          OpenApiSpecificationExtensions.LDPATH);
    }

    @Override
    protected Object convertLiteralToType(Literal literal) {
      return null;
    }

    @Override
    protected Set<IRI> getSupportedDataTypes() {
      return null;
    }

    @Override
    public boolean supports(Property schema) {
      return true;
    }
  }

}
