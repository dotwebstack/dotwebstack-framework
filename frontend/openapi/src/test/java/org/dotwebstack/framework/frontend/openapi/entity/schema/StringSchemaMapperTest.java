package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.Collections;
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
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private TupleEntity tupleEntityMock;

  @Mock
  private GraphEntity graphEntityMock;

  @Mock
  private LdPathExecutor ldPathExecutorMock;

  @Mock
  private Value valueMock;

  private SchemaMapperAdapter schemaMapperAdapter;
  private StringSchema stringSchema;
  private StringSchemaMapper stringSchemaMapper;

  @Before
  public void setUp() {
    stringSchemaMapper = new StringSchemaMapper();
    stringSchema = new StringSchema();
    schemaMapperAdapter = new SchemaMapperAdapter(Collections.singletonList(stringSchemaMapper));

    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);
  }

  @Test
  public void mapTupleValue_ReturnValue_ForLiterals() {
    // Arrange & Act
    String result = stringSchemaMapper.mapTupleValue(stringSchema, tupleEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN_NAME).build());

    // Assert
    assertThat(result, equalTo(DBEERPEDIA.BROUWTOREN_NAME.stringValue()));
  }

  @Test
  public void supports_ReturnsTrue_ForStringSchema() {
    // Arrange & Act
    Boolean supported = stringSchemaMapper.supports(stringSchema);

    // Assert
    assertThat(supported, equalTo(true));
  }

  @Test
  public void supports_ReturnsTrue_ForNonStringSchema() {
    // Arrange & Act
    Boolean supported = stringSchemaMapper.supports(new IntegerSchema());

    // Assert
    assertThat(supported, equalTo(false));
  }

  @Test
  public void mapGraphValue_ThrowsEx_WhenBothLdPathAndRelativeLinkVendorExtensionsAreDefined() {
    // Assert
    expectExceptionAboutMultipleVendorExtensions();

    // Arrange
    stringSchema.setExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH, "",
        OpenApiSpecificationExtensions.RELATIVE_LINK, ImmutableMap.of()));

    // Act
    schemaMapperAdapter.mapGraphValue(stringSchema, false, graphEntityMock,
        ValueContext.builder().build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ThrowsEx_WhenBothLdPathAndConstantValueVendorExtensionsAreDefined() {
    // Assert
    expectExceptionAboutMultipleVendorExtensions();

    // Arrange
    stringSchema.setExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH,
        ImmutableMap.of(), OpenApiSpecificationExtensions.CONSTANT_VALUE, ImmutableMap.of()));

    // Act
    schemaMapperAdapter.mapGraphValue(stringSchema, false, graphEntityMock,
        ValueContext.builder().build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ThrowsEx_WhenBothConstantValueAndRelativeLinkVendorExtsAreDefined() {
    // Assert
    expectExceptionAboutMultipleVendorExtensions();

    // Arrange
    stringSchema.setExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.RELATIVE_LINK,
        ImmutableMap.of(), OpenApiSpecificationExtensions.CONSTANT_VALUE, ImmutableMap.of()));

    // Act
    schemaMapperAdapter.mapGraphValue(stringSchema, false, graphEntityMock,
        ValueContext.builder().build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ThrowsEx_WhenConstantValueLdPathAndRelativeLinkVendorExtsAreDefined() {
    // Assert
    expectExceptionAboutMultipleVendorExtensions();

    // Arrange
    stringSchema.setExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.RELATIVE_LINK,
        ImmutableMap.of(), OpenApiSpecificationExtensions.CONSTANT_VALUE, ImmutableMap.of(),
        OpenApiSpecificationExtensions.LDPATH, ImmutableMap.of()));

    // Act
    schemaMapperAdapter.mapGraphValue(stringSchema, false, graphEntityMock,
        ValueContext.builder().build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ReturnsStringValue_WhenStringConstantValueIsDefined() {
    // Arrange
    stringSchema.setExtensions(
        ImmutableMap.of(OpenApiSpecificationExtensions.CONSTANT_VALUE, "constant"));

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(stringSchema, false, graphEntityMock,
        ValueContext.builder().build(), schemaMapperAdapter);

    // Assert
    assertThat(result, is("constant"));
  }

  @Test
  public void mapGraphValue_ReturnsStringValue_WhenSupportedLiteralConstantValueIsDefined() {
    // Arrange
    Literal literal = VALUE_FACTORY.createLiteral("constant", XMLSchema.STRING);

    stringSchema.setExtensions(
        ImmutableMap.of(OpenApiSpecificationExtensions.CONSTANT_VALUE, literal));

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(stringSchema, false, graphEntityMock,
        ValueContext.builder().build(), schemaMapperAdapter);

    // Assert
    assertThat(result, is("constant"));
  }

  @Test
  public void mapGraphValue_ReturnsNull_ForNullConstantValue() {
    // Arrange
    stringSchema.setExtensions(nullableMapOf(OpenApiSpecificationExtensions.CONSTANT_VALUE, null));

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(stringSchema, false, graphEntityMock,
        ValueContext.builder().build(), schemaMapperAdapter);

    // Assert
    assertNull(result);
  }

  @Test
  public void mapGraphValue_ThrowsException_ForNullConstantAndRequiredSchema() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("StringSchema has 'x-dotwebstack-constant-value' "
        + "vendor extension that is null, but the property is required.");

    // Arrange
    stringSchema.setExtensions(nullableMapOf(OpenApiSpecificationExtensions.CONSTANT_VALUE, null));

    // Act
    stringSchemaMapper.mapGraphValue(stringSchema, true, graphEntityMock,
        ValueContext.builder().build(), schemaMapperAdapter);
  }


  @Test
  public void mapGraphValue_ReturnsNull_ForNullValue() {
    // Act
    Object result = schemaMapperAdapter.mapGraphValue(stringSchema, false, graphEntityMock,
        ValueContext.builder().value(null).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, nullValue());
  }

  @Test
  public void mapGraphValue_ReturnsStringValue_ForNonLiteral() {
    // Act
    Object result = schemaMapperAdapter.mapGraphValue(stringSchema, false, graphEntityMock,
        ValueContext.builder().value(VALUE_FACTORY.createIRI("http://foo")).build(),
        schemaMapperAdapter);

    // Assert
    assertThat(result, is("http://foo"));
  }

  @Test
  public void mapGraphValue_ThrowsException_ForNullValueAndRequiredSchema() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("No result for required schema");

    // Arrange

    // Act
    schemaMapperAdapter.mapGraphValue(stringSchema, true, graphEntityMock,
        ValueContext.builder().value(null).build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ReturnsStringValue_ForXmlSchemaStringLiteral() {
    // Arrange
    Literal xmlSchemaStringLiteral = VALUE_FACTORY.createLiteral("foo", XMLSchema.STRING);

    // Act
    String result = stringSchemaMapper.mapGraphValue(stringSchema, false, graphEntityMock,
        ValueContext.builder().value(xmlSchemaStringLiteral).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, is("foo"));
  }

  @Test
  public void mapGraphValue_ReturnsStringValue_ForRdfLangStringLiteral() {
    // Arrange
    Literal rdfLangStringLiteral = VALUE_FACTORY.createLiteral("foo", "nl");

    // Act
    String result = stringSchemaMapper.mapGraphValue(stringSchema, false, graphEntityMock,
        ValueContext.builder().value(rdfLangStringLiteral).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, is("foo"));
  }

  @Test
  public void mapGraphValue_ReturnsStringValue_ForLdPath() {
    // Arrange
    stringSchema.setExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH, "ld-path"));

    Literal literal = VALUE_FACTORY.createLiteral("foo", XMLSchema.STRING);

    when(ldPathExecutorMock.ldPathQuery(valueMock, "ld-path")).thenReturn(
        ImmutableList.of(literal));

    // Act
    String result = stringSchemaMapper.mapGraphValue(stringSchema, false, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, is("foo"));
  }

  @Test
  public void mapGraphValue_ReturnsNull_ForNullLdPath() {
    // Arrange
    stringSchema.setExtensions(nullableMapOf(OpenApiSpecificationExtensions.LDPATH, null));

    // Act
    String result = stringSchemaMapper.mapGraphValue(stringSchema, false, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, nullValue());
  }

  @Test
  public void mapGraphValue_ThrowsException_ForNullLdPathAndRequiredSchema() {

    // TODO: moet er een value mee worden gegeven? Klopt deze test wel?

    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("No result for required schema.");

    // Arrange

    // Act
    stringSchemaMapper.mapGraphValue(stringSchema, true, graphEntityMock,
        ValueContext.builder().build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ReturnsNull_ForLdPathAndEmptyResult() {
    // Arrange
    stringSchema.setExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH, "ld-path"));

    when(ldPathExecutorMock.ldPathQuery(valueMock, "ld-path")).thenReturn(ImmutableList.of());

    // Act
    String result = stringSchemaMapper.mapGraphValue(stringSchema, false, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, nullValue());
  }

  @Test
  public void mapGraphValue_ReturnsNull_ForLdPathEmptyResultAndRequiredSchema() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("No results for LDPath query 'ld-path' for required schema");

    // Arrange
    stringSchema.setExtensions(nullableMapOf(OpenApiSpecificationExtensions.LDPATH, "ld-path"));

    when(ldPathExecutorMock.ldPathQuery(valueMock, "ld-path")).thenReturn(ImmutableList.of());

    // Act
    stringSchemaMapper.mapGraphValue(stringSchema, true, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ReturnsNull_ForLdPathMultipleResultsAndRequiredSchema() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("LDPath query 'ld-path' yielded multiple results (2) for a property, "
        + "which requires a single result.");

    // Arrange
    stringSchema.setExtensions(nullableMapOf(OpenApiSpecificationExtensions.LDPATH, "ld-path"));

    Literal foo = VALUE_FACTORY.createLiteral("foo", XMLSchema.STRING);
    Literal bar = VALUE_FACTORY.createLiteral("bar", XMLSchema.STRING);

    when(ldPathExecutorMock.ldPathQuery(valueMock, "ld-path")).thenReturn(
        ImmutableList.of(foo, bar));

    // Act
    stringSchemaMapper.mapGraphValue(stringSchema, true, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ReturnsStringValue_LdPathWithIri() {

    // Arrange
    stringSchema.setExtensions(nullableMapOf(OpenApiSpecificationExtensions.LDPATH, "ld-path"));

    Value stringValue = VALUE_FACTORY.createIRI(
        "http://www.ruimtelijkeplannen.nl/documents/NL.IMRO.0345.Lunenburg1-vg01/",
        "vb_NL.IMRO.0345.Lunenburg1-vg01.pdf");

    when(ldPathExecutorMock.ldPathQuery(valueMock, "ld-path")).thenReturn(
        ImmutableList.of(stringValue));

    // Act
    String response = stringSchemaMapper.mapGraphValue(stringSchema, true, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertEquals("http://www.ruimtelijkeplannen.nl/documents/NL.IMRO.0345.Lunenburg1-vg01/"
        + "vb_NL.IMRO.0345.Lunenburg1-vg01.pdf", response);
  }

  private static Map<String, Object> nullableMapOf(String key, Object val) {
    Map<String, Object> result = new HashMap<>();

    result.put(key, val);

    return result;
  }

  private void expectExceptionAboutMultipleVendorExtensions() {
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(String.format(
        "A string object must have either no, a '%s', '%s' or '%s' property. "
            + "A string object cannot have a combination of these.",
        OpenApiSpecificationExtensions.LDPATH, OpenApiSpecificationExtensions.RELATIVE_LINK,
        OpenApiSpecificationExtensions.CONSTANT_VALUE));
  }

}
