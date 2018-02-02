package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.StringProperty;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ObjectSchemaMapperTest {

  private static final String KEY_1 = "key1";
  private static final String KEY_2 = "key2";
  private static final String KEY_3 = "key3";
  private static final String KEY_4 = "key4";
  private static final StringProperty STR_PROPERTY_1 = new StringProperty();
  private static final StringProperty STR_PROPERTY_2 = new StringProperty();
  private static final ArrayProperty ARRAY_PROPERTY = new ArrayProperty(new StringProperty("3"));
  private static final StringProperty STR_PROPERTY_3 = new StringProperty();
  private static final String STR_VALUE_1 = "val1";
  private static final String STR_VALUE_2 = "val2";
  private static final List<Value> VALUE_3 = ImmutableList.of();
  private static final String DUMMY_EXPR_1 = "dummyExpr1()";
  private static final String ARRAY_LD_EXP = "arrayLdPath";
  private static final String STR1_LD_EXP = "stringLdPath1";
  private static final String STR2_LD_EXP = "stringLdPath2";
  private static final String STR3_LD_EXP = "stringLdPath3";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private GraphEntity entityMock;

  @Mock
  private Value value1Mock;

  @Mock
  private Value value2Mock;

  @Mock
  private LdPathExecutor ldPathExecutorMock;

  private SchemaMapperAdapter schemaMapperAdapter;

  private ObjectSchemaMapper schemaMapper;

  private ObjectProperty property;

  @Before
  public void setUp() {
    schemaMapper = new ObjectSchemaMapper();

    property = new ObjectProperty();
    property.setProperties(ImmutableMap.of(KEY_1, STR_PROPERTY_1, KEY_2, STR_PROPERTY_2, KEY_3,
        ARRAY_PROPERTY, KEY_4, STR_PROPERTY_3));

    SchemaMapper stringSchemaMapper = new StringSchemaMapper();
    SchemaMapper arraySchemaMapper = new ArraySchemaMapper();

    schemaMapperAdapter =
        new SchemaMapperAdapter(Arrays.asList(stringSchemaMapper, schemaMapper, arraySchemaMapper));

    ARRAY_PROPERTY.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, ARRAY_LD_EXP);
    STR_PROPERTY_1.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, STR1_LD_EXP);
    STR_PROPERTY_2.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, STR2_LD_EXP);
    STR_PROPERTY_3.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, STR3_LD_EXP);

    SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

    when(ldPathExecutorMock.ldPathQuery(any(), eq(STR1_LD_EXP))).thenReturn(
        Collections.singleton(valueFactory.createLiteral(STR_VALUE_1)));
    when(ldPathExecutorMock.ldPathQuery(any(), eq(STR2_LD_EXP))).thenReturn(
        Collections.singleton(valueFactory.createLiteral(STR_VALUE_2)));

    when(entityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);
  }

  @Test
  public void mapGraphValue_ReturnsEmptyMap_WhenObjectHasNoProperties() {
    // Arrange
    property.setProperties(ImmutableMap.of());

    // Act
    Map<String, Object> result = (Map<String, Object>) schemaMapperAdapter.mapGraphValue(property,
        entityMock, ValueContext.builder().value(value1Mock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result.keySet(), hasSize(0));
  }

  @Test
  public void mapGraphValue_ReturnsPropertyMap_WhenObjectHasProperties() {
    // Arrange
    property.setProperties(ImmutableMap.of(KEY_1, STR_PROPERTY_1, KEY_2, STR_PROPERTY_2, KEY_3,
        ARRAY_PROPERTY, KEY_4, STR_PROPERTY_3));
    when(ldPathExecutorMock.ldPathQuery(any(), eq(STR3_LD_EXP))).thenReturn(ImmutableList.of());

    Map<String, Object> result = (Map<String, Object>) schemaMapperAdapter.mapGraphValue(property,
        entityMock, ValueContext.builder().value(value1Mock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result.keySet(), hasSize(4));
    assertThat(result, hasEntry(KEY_1, com.google.common.base.Optional.of(STR_VALUE_1)));
    assertThat(result, hasEntry(KEY_2, com.google.common.base.Optional.of(STR_VALUE_2)));
    assertThat(result, hasEntry(KEY_3, com.google.common.base.Optional.of(VALUE_3)));
    assertThat(result, hasEntry(KEY_4, com.google.common.base.Optional.absent()));
  }

  @Test
  public void mapGraphValue_ReturnsNull_WhenLdPathYieldsNoResults() {
    property.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR_1);

    when(ldPathExecutorMock.ldPathQuery(value1Mock, DUMMY_EXPR_1)).thenReturn(ImmutableSet.of());

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(property, entityMock,
        ValueContext.builder().value(value1Mock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, nullValue());
  }

  @Test
  public void mapGraphValue_ThrowsException_WhenLdPathYieldsNoResultsForRequiredProperty() {
    // Assert
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage(
        String.format("LDPath expression for a required object property ('%s') yielded no result.",
            DUMMY_EXPR_1));

    // Arrange
    property.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR_1);
    property.setRequired(true);

    when(ldPathExecutorMock.ldPathQuery(value1Mock, DUMMY_EXPR_1)).thenReturn(ImmutableSet.of());

    // Act
    schemaMapperAdapter.mapGraphValue(property, entityMock,
        ValueContext.builder().value(value1Mock).build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ThrowsException_WhenLdPathYieldsMultipleResults() {
    // Assert
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage(String.format(
        "LDPath expression for object property ('%s') yielded multiple elements.", DUMMY_EXPR_1));

    // Arrange
    property.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR_1);
    when(ldPathExecutorMock.ldPathQuery(value1Mock, DUMMY_EXPR_1)).thenReturn(
        ImmutableSet.of(value1Mock, value2Mock));

    // Act
    schemaMapperAdapter.mapGraphValue(property, entityMock,
        ValueContext.builder().value(value1Mock).build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_SwitchesContext_WhenSubjectFilterHasBeenDefined() {
    // Arrange
    property.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER,
        ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER_PREDICATE,
            RDF.TYPE.stringValue(), OpenApiSpecificationExtensions.SUBJECT_FILTER_OBJECT,
            DBEERPEDIA.BREWERY_TYPE.stringValue()),
        OpenApiSpecificationExtensions.LDPATH, DBEERPEDIA.NAME.stringValue()));
    property.setProperties(ImmutableMap.of(DBEERPEDIA.NAME.stringValue(), new StringProperty()));

    Model model = new ModelBuilder().subject(DBEERPEDIA.BROUWTOREN).add(RDF.TYPE,
        DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME, DBEERPEDIA.BROUWTOREN_NAME).build();
    when(entityMock.getModel()).thenReturn(model);

    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.BROUWTOREN,
        DBEERPEDIA.NAME.stringValue())).thenReturn(ImmutableSet.of(DBEERPEDIA.BROUWTOREN_NAME));

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(property, entityMock,
        ValueContext.builder().value(null).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, instanceOf(Map.class));

    Map map = (Map) result;

    assertThat(map, is(ImmutableMap.of(DBEERPEDIA.NAME.stringValue(),
        Optional.of(DBEERPEDIA.BROUWTOREN_NAME.stringValue()))));
  }

  @Test
  public void mapGraphValue_ReturnsNull_WhenSubjectFilterYieldsNoResultAndPropertyIsOptional() {
    // Arrange
    property.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER,
        ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER_PREDICATE,
            RDF.TYPE.stringValue(), OpenApiSpecificationExtensions.SUBJECT_FILTER_OBJECT,
            DBEERPEDIA.BREWERY_TYPE.stringValue()),
        OpenApiSpecificationExtensions.LDPATH, DBEERPEDIA.NAME.stringValue()));
    property.setProperties(ImmutableMap.of(DBEERPEDIA.NAME.stringValue(), new StringProperty()));

    Model model = new ModelBuilder().build();
    when(entityMock.getModel()).thenReturn(model);

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(property, entityMock,
        ValueContext.builder().value(null).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, nullValue());
  }

  @Test
  public void mapGraphValue_ThrowsException_WhenSubjectFilterYieldsNoResultAndPropertyIsRequired() {
    // Assert
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage(
        "Subject filter for a required object property yielded no result");

    // Arrange
    property.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER,
        ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER_PREDICATE,
            RDF.TYPE.stringValue(), OpenApiSpecificationExtensions.SUBJECT_FILTER_OBJECT,
            DBEERPEDIA.BREWERY_TYPE.stringValue()),
        OpenApiSpecificationExtensions.LDPATH, DBEERPEDIA.NAME.stringValue()));
    property.setProperties(ImmutableMap.of(DBEERPEDIA.NAME.stringValue(), new StringProperty()));
    property.setRequired(true);

    Model model = new ModelBuilder().build();
    when(entityMock.getModel()).thenReturn(model);

    // Act
    schemaMapperAdapter.mapGraphValue(property, entityMock,
        ValueContext.builder().value(null).build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ThrowsException_WhenSubjectFilterYieldsMultipleResults() {
    // Assert
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage("More entrypoint subjects found. Only one is required");

    // Arrange
    property.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER,
        ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER_PREDICATE,
            RDF.TYPE.stringValue(), OpenApiSpecificationExtensions.SUBJECT_FILTER_OBJECT,
            DBEERPEDIA.BREWERY_TYPE.stringValue()),
        OpenApiSpecificationExtensions.LDPATH, DBEERPEDIA.NAME.stringValue()));
    property.setProperties(ImmutableMap.of(DBEERPEDIA.NAME.stringValue(), new StringProperty()));
    property.setRequired(true);

    Model model = new ModelBuilder().subject(DBEERPEDIA.BROUWTOREN).add(RDF.TYPE,
        DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME, DBEERPEDIA.BROUWTOREN_NAME).subject(
            DBEERPEDIA.MAXIMUS).add(RDF.TYPE, DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME,
                DBEERPEDIA.MAXIMUS_NAME).build();
    when(entityMock.getModel()).thenReturn(model);

    // Act
    schemaMapperAdapter.mapGraphValue(property, entityMock,
        ValueContext.builder().value(null).build(), schemaMapperAdapter);
  }

  @Test
  public void supports_ReturnsTrue_ForObjectProperty() {
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
  public void supports_ReturnsFalse_ForObjectPropertyWithVendorExtension() {
    // Arrange
    property.setVendorExtension(OpenApiSpecificationExtensions.TYPE, "dummy");

    // Act
    boolean result = schemaMapper.supports(property);

    // Assert
    assertThat(result, is(false));
  }


  @Test
  public void getSupportedDataTypes_ReturnsEmptySet() {
    // Act
    Set<IRI> result = schemaMapper.getSupportedDataTypes();

    // Assert
    assertThat(result, empty());
  }

  @Test
  public void mapGraphValue_ExcludesStringProperty_WhenVendorExtIsSetAndValueIsNull() {
    // Arrange
    StringProperty childProperty = new StringProperty();

    childProperty.setVendorExtension(OpenApiSpecificationExtensions.LDPATH,
        DBEERPEDIA.NAME.stringValue());

    property.setProperties(ImmutableMap.of(DBEERPEDIA.NAME.stringValue(), childProperty));
    property.setVendorExtension(
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);

    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.BROUWTOREN,
        DBEERPEDIA.NAME.stringValue())).thenReturn(ImmutableSet.of());

    // Act
    Map result = (ImmutableMap) schemaMapperAdapter.mapGraphValue(property, entityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN).build(), schemaMapperAdapter);

    // Assert
    assertThat(result.isEmpty(), is(true));
  }

  @Test
  public void mapGraphValue_IncludesStringProperty_WhenVendorExtIsSetAndValueIsNotNull() {
    // Arrange
    StringProperty childProperty = new StringProperty();

    childProperty.setVendorExtension(OpenApiSpecificationExtensions.LDPATH,
        DBEERPEDIA.NAME.stringValue());

    property.setProperties(ImmutableMap.of(DBEERPEDIA.NAME.stringValue(), childProperty));
    property.setVendorExtension(
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);

    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.BROUWTOREN,
        DBEERPEDIA.NAME.stringValue())).thenReturn(ImmutableSet.of(DBEERPEDIA.BROUWTOREN_NAME));

    // Act
    Map result = (ImmutableMap) schemaMapperAdapter.mapGraphValue(property, entityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, is(ImmutableMap.of(DBEERPEDIA.NAME.stringValue(),
        Optional.of(DBEERPEDIA.BROUWTOREN_NAME.stringValue()))));
  }

  @Test
  public void mapGraphValue_ExcludesArrayProperty_WhenVendorExtIsSetAndArrayIsEmpty() {
    // Arrange
    ArrayProperty childProperty = new ArrayProperty(new StringProperty());

    childProperty.setVendorExtension(OpenApiSpecificationExtensions.LDPATH,
        DBEERPEDIA.NAME.stringValue());

    property.setProperties(ImmutableMap.of(DBEERPEDIA.NAME.stringValue(), childProperty));
    property.setVendorExtension(
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);

    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.BROUWTOREN,
        DBEERPEDIA.NAME.stringValue())).thenReturn(ImmutableSet.of());

    // Act
    Map result = (ImmutableMap) schemaMapperAdapter.mapGraphValue(property, entityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN).build(), schemaMapperAdapter);

    // Assert
    assertThat(result.isEmpty(), is(true));
  }

  @Test
  public void mapGraphValue_IncludesArrayProperty_WhenVendorExtIsSetAndArrayIsNotEmpty() {
    // Arrange
    ArrayProperty childProperty = new ArrayProperty(new StringProperty());

    childProperty.setVendorExtension(OpenApiSpecificationExtensions.LDPATH,
        DBEERPEDIA.NAME.stringValue());

    property.setProperties(ImmutableMap.of(DBEERPEDIA.NAME.stringValue(), childProperty));
    property.setVendorExtension(
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);

    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.BROUWTOREN,
        DBEERPEDIA.NAME.stringValue())).thenReturn(ImmutableSet.of(DBEERPEDIA.BROUWTOREN_NAME));

    // Act
    Map result = (ImmutableMap) schemaMapperAdapter.mapGraphValue(property, entityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, is(ImmutableMap.of(DBEERPEDIA.NAME.stringValue(),
        Optional.of(ImmutableList.of(Optional.of(DBEERPEDIA.BROUWTOREN_NAME.stringValue()))))));
  }

}
