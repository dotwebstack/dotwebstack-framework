package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.StringProperty;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StringSchemaMapperTest {

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private TupleEntity tupleEntityMock;

  @Mock
  private GraphEntity graphEntityMock;

  @Mock
  private LdPathExecutor ldPathExecutorMock;

  @Mock
  private Value subjectMock;

  private SchemaMapperAdapter mapperAdapter;

  private StringProperty property;

  private StringSchemaMapper schemaMapper;

  @Before
  public void setUp() {
    schemaMapper = new StringSchemaMapper();
    property = new StringProperty();
    mapperAdapter = new SchemaMapperAdapter(Arrays.asList(schemaMapper));

    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);
  }

  @Test
  public void mapTupleValue_ReturnValue_ForLiterals() {
    // Arrange & Act
    String result = schemaMapper.mapTupleValue(property, tupleEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN_NAME).build());

    // Assert
    assertThat(result, equalTo(DBEERPEDIA.BROUWTOREN_NAME.stringValue()));
  }

  @Test
  public void supports_ReturnsTrue_ForStringProperty() {
    // Arrange & Act
    Boolean supported = schemaMapper.supports(property);

    // Assert
    assertThat(supported, equalTo(true));
  }

  @Test
  public void supports_ReturnsTrue_ForNonStringProperty() {
    // Arrange & Act
    Boolean supported = schemaMapper.supports(new IntegerProperty());

    // Assert
    assertThat(supported, equalTo(false));
  }

  @Test
  public void mapGraphValue_ThrowsEx_WhenBothLdPathAndRelativeLinkVendorExtensionsAreDefined() {
    // Assert
    expectExceptionAboutMultipleVendorExtensions();

    // Arrange
    property.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH, "",
        OpenApiSpecificationExtensions.RELATIVE_LINK, ImmutableMap.of()));

    // Act
    mapperAdapter.mapGraphValue(property, graphEntityMock, ValueContext.builder().build(),
        mapperAdapter);
  }

  @Test
  public void mapGraphValue_ThrowsEx_WhenBothLdPathAndConstantValueVendorExtensionsAreDefined() {
    // Assert
    expectExceptionAboutMultipleVendorExtensions();

    // Arrange
    property.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH,
        ImmutableMap.of(), OpenApiSpecificationExtensions.CONSTANT_VALUE, ImmutableMap.of()));

    // Act
    mapperAdapter.mapGraphValue(property, graphEntityMock, ValueContext.builder().build(),
        mapperAdapter);
  }

  @Test
  public void mapGraphValue_ThrowsEx_WhenBothConstantValueAndRelativeLinkVendorExtsAreDefined() {
    // Assert
    expectExceptionAboutMultipleVendorExtensions();

    // Arrange
    property.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.RELATIVE_LINK,
        ImmutableMap.of(), OpenApiSpecificationExtensions.CONSTANT_VALUE, ImmutableMap.of()));

    // Act
    mapperAdapter.mapGraphValue(property, graphEntityMock, ValueContext.builder().build(),
        mapperAdapter);
  }

  @Test
  public void mapGraphValue_ThrowsEx_WhenConstantValueLdPathAndRelativeLinkVendorExtsAreDefined() {
    // Assert
    expectExceptionAboutMultipleVendorExtensions();

    // Arrange
    property.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.RELATIVE_LINK,
        ImmutableMap.of(), OpenApiSpecificationExtensions.CONSTANT_VALUE, ImmutableMap.of(),
        OpenApiSpecificationExtensions.LDPATH, ImmutableMap.of()));

    // Act
    mapperAdapter.mapGraphValue(property, graphEntityMock, ValueContext.builder().build(),
        mapperAdapter);
  }

  @Test
  public void mapGraphValue_ReturnsStringValue_WhenStringConstantValueIsDefined() {
    // Arrange
    property.setVendorExtensions(
        ImmutableMap.of(OpenApiSpecificationExtensions.CONSTANT_VALUE, "constant"));

    // Act
    Object result = mapperAdapter.mapGraphValue(property, graphEntityMock,
        ValueContext.builder().build(), mapperAdapter);

    // Assert
    assertThat(result, is("constant"));
  }

  @Test
  public void mapGraphValue_ReturnsStringValue_WhenSupportedLiteralConstantValueIsDefined() {
    // Arrange
    Literal literal = VALUE_FACTORY.createLiteral("constant", XMLSchema.STRING);

    property.setVendorExtensions(
        ImmutableMap.of(OpenApiSpecificationExtensions.CONSTANT_VALUE, literal));

    // Act
    Object result = mapperAdapter.mapGraphValue(property, graphEntityMock,
        ValueContext.builder().build(), mapperAdapter);

    // Assert
    assertThat(result, is("constant"));
  }

  @Test
  public void mapGraphValue_ReturnsNull_ForNullConstantValue() {
    // Arrange
    property.setVendorExtensions(
        nullableMapOf(OpenApiSpecificationExtensions.CONSTANT_VALUE, null));

    // Act
    Object result = mapperAdapter.mapGraphValue(property, graphEntityMock,
        ValueContext.builder().build(), mapperAdapter);

    // Assert
    assertNull(result);
  }

  @Test
  public void mapGraphValue_ThrowsException_ForNullConstantAndRequiredProperty() {
    // Assert
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage(
        "String property has 'x-dotwebstack-constant-value' vendor extension that is null, "
            + "but the property is required");

    // Arrange
    property.setVendorExtensions(
        nullableMapOf(OpenApiSpecificationExtensions.CONSTANT_VALUE, null));
    property.setRequired(true);

    // Act
    schemaMapper.mapGraphValue(property, graphEntityMock, ValueContext.builder().build(),
        mapperAdapter);
  }


  @Test
  public void mapGraphValue_ReturnsNull_ForNullValue() {
    // Act
    Object result = mapperAdapter.mapGraphValue(property, graphEntityMock,
        ValueContext.builder().value(null).build(), mapperAdapter);

    // Assert
    assertThat(result, nullValue());
  }

  @Test
  public void mapGraphValue_ReturnsStringValue_ForNonLiteral() {
    // Act
    Object result = mapperAdapter.mapGraphValue(property, graphEntityMock,
        ValueContext.builder().value(VALUE_FACTORY.createIRI("http://foo")).build(), mapperAdapter);

    // Assert
    assertThat(result, is("http://foo"));
  }

  @Test
  public void mapGraphValue_ThrowsException_ForNullValueAndRequiredProperty() {
    // Assert
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage("No result for required property");

    // Arrange
    property.setRequired(true);

    // Act
    mapperAdapter.mapGraphValue(property, graphEntityMock,
        ValueContext.builder().value(null).build(), mapperAdapter);
  }

  @Test
  public void mapGraphValue_ReturnsStringValue_ForXmlSchemaStringLiteral() {
    // Arrange
    Literal xmlSchemaStringLiteral = VALUE_FACTORY.createLiteral("foo", XMLSchema.STRING);

    // Act
    String result = schemaMapper.mapGraphValue(property, graphEntityMock,
        ValueContext.builder().value(xmlSchemaStringLiteral).build(), mapperAdapter);

    // Assert
    assertThat(result, is("foo"));
  }

  @Test
  public void mapGraphValue_ReturnsStringValue_ForRdfLangStringLiteral() {
    // Arrange
    Literal rdfLangStringLiteral = VALUE_FACTORY.createLiteral("foo", "nl");

    // Act
    String result = schemaMapper.mapGraphValue(property, graphEntityMock,
        ValueContext.builder().value(rdfLangStringLiteral).build(), mapperAdapter);

    // Assert
    assertThat(result, is("foo"));
  }

  @Test
  public void mapGraphValue_ReturnsStringValue_ForLdPath() {
    // Arrange
    property.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH, "ld-path"));

    Literal literal = VALUE_FACTORY.createLiteral("foo", XMLSchema.STRING);

    when(ldPathExecutorMock.ldPathQuery(subjectMock, "ld-path")).thenReturn(
        ImmutableList.of(literal));

    // Act
    String result = schemaMapper.mapGraphValue(property, graphEntityMock,
        ValueContext.builder().value(subjectMock).build(), mapperAdapter);

    // Assert
    assertThat(result, is("foo"));
  }

  @Test
  public void mapGraphValue_ReturnsNull_ForNullLdPath() {
    // Arrange
    property.setVendorExtensions(nullableMapOf(OpenApiSpecificationExtensions.LDPATH, null));

    // Act
    String result = schemaMapper.mapGraphValue(property, graphEntityMock,
        ValueContext.builder().value(subjectMock).build(), mapperAdapter);

    // Assert
    assertThat(result, nullValue());
  }

  @Test
  public void mapGraphValue_ThrowsException_ForNullLdPathAndRequiredProperty() {
    // Assert
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage(
        "String property has 'x-dotwebstack-ldpath' vendor extension that is null, "
            + "but the property is required");

    // Arrange
    property.setVendorExtensions(nullableMapOf(OpenApiSpecificationExtensions.LDPATH, null));
    property.setRequired(true);

    // Act
    schemaMapper.mapGraphValue(property, graphEntityMock,
        ValueContext.builder().value(subjectMock).build(), mapperAdapter);
  }

  @Test
  public void mapGraphValue_ReturnsNull_ForLdPathAndEmptyResult() {
    // Arrange
    property.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH, "ld-path"));

    when(ldPathExecutorMock.ldPathQuery(subjectMock, "ld-path")).thenReturn(ImmutableList.of());

    // Act
    String result = schemaMapper.mapGraphValue(property, graphEntityMock,
        ValueContext.builder().value(subjectMock).build(), mapperAdapter);

    // Assert
    assertThat(result, nullValue());
  }

  @Test
  public void mapGraphValue_ReturnsNull_ForLdPathEmptyResultAndRequiredProperty() {
    // Assert
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage("No results for LDPath query 'ld-path' for required property");

    // Arrange
    property.setVendorExtensions(nullableMapOf(OpenApiSpecificationExtensions.LDPATH, "ld-path"));
    property.setRequired(true);

    when(ldPathExecutorMock.ldPathQuery(subjectMock, "ld-path")).thenReturn(ImmutableList.of());

    // Act
    schemaMapper.mapGraphValue(property, graphEntityMock,
        ValueContext.builder().value(subjectMock).build(), mapperAdapter);
  }

  @Test
  public void mapGraphValue_ReturnsNull_ForLdPathMultipleResultsAndRequiredProperty() {
    // Assert
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage(
        "LDPath query 'ld-path' yielded multiple results (2) for a property, "
            + "which requires a single result.");

    // Arrange
    property.setVendorExtensions(nullableMapOf(OpenApiSpecificationExtensions.LDPATH, "ld-path"));
    property.setRequired(true);

    Literal foo = VALUE_FACTORY.createLiteral("foo", XMLSchema.STRING);
    Literal bar = VALUE_FACTORY.createLiteral("bar", XMLSchema.STRING);

    when(ldPathExecutorMock.ldPathQuery(subjectMock, "ld-path")).thenReturn(
        ImmutableList.of(foo, bar));

    // Act
    schemaMapper.mapGraphValue(property, graphEntityMock,
        ValueContext.builder().value(subjectMock).build(), mapperAdapter);
  }

  private static Map<String, Object> nullableMapOf(String key, Object val) {
    Map<String, Object> result = new HashMap<>();

    result.put(key, val);

    return result;
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
