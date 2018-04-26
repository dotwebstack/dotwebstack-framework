package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
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
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ObjectSchemaMapperTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

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

  @Mock
  private GraphEntity graphEntityMock;
  @Mock
  private Value value1Mock;
  @Mock
  private Value value2Mock;
  @Mock
  private LdPathExecutor ldPathExecutorMock;

  private SchemaMapperAdapter schemaMapperAdapter;
  private ObjectSchemaMapper objectSchemaMapper;
  private ObjectProperty objectProperty;

  @Before
  public void setUp() {
    objectSchemaMapper = new ObjectSchemaMapper();

    objectProperty = new ObjectProperty();
    objectProperty.setProperties(ImmutableMap.of(KEY_1, STR_PROPERTY_1, KEY_2, STR_PROPERTY_2,
        KEY_3, ARRAY_PROPERTY, KEY_4, STR_PROPERTY_3));

    SchemaMapper stringSchemaMapper = new StringSchemaMapper();
    SchemaMapper arraySchemaMapper = new ArraySchemaMapper();

    schemaMapperAdapter = new SchemaMapperAdapter(
        Arrays.asList(stringSchemaMapper, objectSchemaMapper, arraySchemaMapper));

    ARRAY_PROPERTY.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, ARRAY_LD_EXP);
    STR_PROPERTY_1.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, STR1_LD_EXP);
    STR_PROPERTY_2.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, STR2_LD_EXP);
    STR_PROPERTY_3.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, STR3_LD_EXP);

    SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

    when(ldPathExecutorMock.ldPathQuery(any(), eq(STR1_LD_EXP))).thenReturn(
        Collections.singleton(valueFactory.createLiteral(STR_VALUE_1)));
    when(ldPathExecutorMock.ldPathQuery(any(), eq(STR2_LD_EXP))).thenReturn(
        Collections.singleton(valueFactory.createLiteral(STR_VALUE_2)));

    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);
  }

  @Test
  public void mapGraphValue_ReturnsEmptyMap_WhenObjectHasNoProperties() {
    // Arrange
    objectProperty.setProperties(ImmutableMap.of());

    // Act
    Map<String, Object> result =
        (Map<String, Object>) schemaMapperAdapter.mapGraphValue(objectProperty, graphEntityMock,
            ValueContext.builder().value(value1Mock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result.keySet(), hasSize(0));
  }

  @Test
  public void mapGraphValue_ReturnsPropertyMap_WhenObjectHasProperties() {
    // Arrange
    objectProperty.setProperties(ImmutableMap.of(KEY_1, STR_PROPERTY_1, KEY_2, STR_PROPERTY_2,
        KEY_3, ARRAY_PROPERTY, KEY_4, STR_PROPERTY_3));
    when(ldPathExecutorMock.ldPathQuery(any(), eq(STR3_LD_EXP))).thenReturn(ImmutableList.of());

    Map<String, Object> result =
        (Map<String, Object>) schemaMapperAdapter.mapGraphValue(objectProperty, graphEntityMock,
            ValueContext.builder().value(value1Mock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result.keySet(), hasSize(4));
    assertThat(result, hasEntry(KEY_1, Optional.of(STR_VALUE_1)));
    assertThat(result, hasEntry(KEY_2, Optional.of(STR_VALUE_2)));
    assertThat(result, hasEntry(KEY_3, Optional.of(VALUE_3)));
    assertThat(result, hasEntry(KEY_4, Optional.absent()));
  }

  @Test
  public void mapGraphValue_ReturnsNull_WhenLdPathYieldsNoResults() {
    objectProperty.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR_1);

    when(ldPathExecutorMock.ldPathQuery(value1Mock, DUMMY_EXPR_1)).thenReturn(ImmutableSet.of());

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(objectProperty, graphEntityMock,
        ValueContext.builder().value(value1Mock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, nullValue());
  }

  @Test
  public void mapGraphValue_ThrowsException_WhenLdPathYieldsNoResultsForRequiredProperty() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(
        String.format("LDPath expression for a required object property ('%s') yielded no result.",
            DUMMY_EXPR_1));

    // Arrange
    objectProperty.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR_1);
    objectProperty.setRequired(true);

    when(ldPathExecutorMock.ldPathQuery(value1Mock, DUMMY_EXPR_1)).thenReturn(ImmutableSet.of());

    // Act
    schemaMapperAdapter.mapGraphValue(objectProperty, graphEntityMock,
        ValueContext.builder().value(value1Mock).build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ThrowsException_WhenLdPathYieldsMultipleResults() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(String.format(
        "LDPath expression for object property ('%s') yielded multiple elements.", DUMMY_EXPR_1));

    // Arrange
    objectProperty.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR_1);
    when(ldPathExecutorMock.ldPathQuery(value1Mock, DUMMY_EXPR_1)).thenReturn(
        ImmutableSet.of(value1Mock, value2Mock));

    // Act
    schemaMapperAdapter.mapGraphValue(objectProperty, graphEntityMock,
        ValueContext.builder().value(value1Mock).build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_SwitchesContext_WhenSubjectExtEnabled() {
    // Arrange
    objectProperty.setVendorExtension(OpenApiSpecificationExtensions.SUBJECT, true);
    objectProperty.setProperties(ImmutableMap.of("name", new StringProperty().vendorExtension(
        OpenApiSpecificationExtensions.LDPATH, DBEERPEDIA.NAME.stringValue())));

    when(graphEntityMock.getSubjects()).thenReturn(ImmutableSet.of(DBEERPEDIA.BROUWTOREN));

    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.BROUWTOREN,
        DBEERPEDIA.NAME.stringValue())).thenReturn(ImmutableList.of(DBEERPEDIA.BROUWTOREN_NAME));

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(objectProperty, graphEntityMock,
        ValueContext.builder().value(null).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, instanceOf(Map.class));

    Map map = (Map) result;

    assertThat(map,
        is(ImmutableMap.of("name", Optional.of(DBEERPEDIA.BROUWTOREN_NAME.stringValue()))));
  }

  @Test
  public void mapGraphValue_ReturnsNull_WhenSubjectQueryYieldsNoResultAndPropertyIsOptional() {
    // Arrange
    objectProperty.setVendorExtension(OpenApiSpecificationExtensions.SUBJECT, true);
    objectProperty.setProperties(ImmutableMap.of("name", new StringProperty().vendorExtension(
        OpenApiSpecificationExtensions.LDPATH, DBEERPEDIA.NAME.stringValue())));

    when(graphEntityMock.getSubjects()).thenReturn(ImmutableSet.of());

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(objectProperty, graphEntityMock,
        ValueContext.builder().value(null).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, is(nullValue()));
  }

  @Test
  public void supports_ReturnsTrue_ForObjectProperty() {
    // Act
    boolean result = objectSchemaMapper.supports(objectProperty);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void supports_ReturnsFalse_ForNonObjectProperty() {
    // Act
    boolean result = objectSchemaMapper.supports(new ArrayProperty());

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void supports_ReturnsFalse_ForObjectPropertyWithVendorExtension() {
    // Arrange
    objectProperty.setVendorExtension(OpenApiSpecificationExtensions.TYPE, "dummy");

    // Act
    boolean result = objectSchemaMapper.supports(objectProperty);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void getSupportedDataTypes_ReturnsEmptySet() {
    // Act
    Set<IRI> result = objectSchemaMapper.getSupportedDataTypes();

    // Assert
    assertThat(result, empty());
  }

  @Test
  public void mapGraphValue_ExcludesStringProperty_WhenVendorExtIsSetAndValueIsNull() {
    // Arrange
    StringProperty childProperty = new StringProperty();

    childProperty.setVendorExtension(OpenApiSpecificationExtensions.LDPATH,
        DBEERPEDIA.NAME.stringValue());

    objectProperty.setProperties(ImmutableMap.of(DBEERPEDIA.NAME.stringValue(), childProperty));
    objectProperty.setVendorExtension(
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);

    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.BROUWTOREN,
        DBEERPEDIA.NAME.stringValue())).thenReturn(ImmutableSet.of());

    // Act
    Map result = (ImmutableMap) schemaMapperAdapter.mapGraphValue(objectProperty, graphEntityMock,
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

    objectProperty.setProperties(ImmutableMap.of(DBEERPEDIA.NAME.stringValue(), childProperty));
    objectProperty.setVendorExtension(
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);

    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.BROUWTOREN,
        DBEERPEDIA.NAME.stringValue())).thenReturn(ImmutableSet.of(DBEERPEDIA.BROUWTOREN_NAME));

    // Act
    Map result = (ImmutableMap) schemaMapperAdapter.mapGraphValue(objectProperty, graphEntityMock,
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

    objectProperty.setProperties(ImmutableMap.of(DBEERPEDIA.NAME.stringValue(), childProperty));
    objectProperty.setVendorExtension(
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);

    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.BROUWTOREN,
        DBEERPEDIA.NAME.stringValue())).thenReturn(ImmutableSet.of());

    // Act
    Map result = (ImmutableMap) schemaMapperAdapter.mapGraphValue(objectProperty, graphEntityMock,
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

    objectProperty.setProperties(ImmutableMap.of(DBEERPEDIA.NAME.stringValue(), childProperty));
    objectProperty.setVendorExtension(
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);

    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.BROUWTOREN,
        DBEERPEDIA.NAME.stringValue())).thenReturn(ImmutableSet.of(DBEERPEDIA.BROUWTOREN_NAME));

    // Act
    Map result = (ImmutableMap) schemaMapperAdapter.mapGraphValue(objectProperty, graphEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, is(ImmutableMap.of(DBEERPEDIA.NAME.stringValue(),
        Optional.of(ImmutableList.of(Optional.of(DBEERPEDIA.BROUWTOREN_NAME.stringValue()))))));
  }

}
