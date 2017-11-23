package org.dotwebstack.framework.frontend.openapi.schema;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
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

  private SchemaMapperAdapter registry;

  private StringSchemaMapper stringPropertyHandler;
  private ArraySchemaMapper arrayPropertyHandler;

  @Mock
  private GraphEntityContext entityBuilderContextMock;

  @Mock
  private Value context1Mock;

  @Mock
  private Value context2Mock;

  @Mock
  private LdPathExecutor ldPathExecutorMock;

  private ObjectSchemaMapper objectPropertyHandler;
  private ObjectProperty objectProperty;

  @Before
  public void setUp() {
    objectPropertyHandler = new ObjectSchemaMapper();
    objectProperty = new ObjectProperty();
    stringPropertyHandler = new StringSchemaMapper();
    arrayPropertyHandler = new ArraySchemaMapper();

    registry = new SchemaMapperAdapter(
        Arrays.asList(stringPropertyHandler, objectPropertyHandler, arrayPropertyHandler));
    ARRAY_PROPERTY.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, ARRAY_LD_EXP);
    STR_PROPERTY_1.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, STR1_LD_EXP);
    STR_PROPERTY_2.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, STR2_LD_EXP);
    STR_PROPERTY_3.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, STR3_LD_EXP);

    SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
    when(ldPathExecutorMock.ldPathQuery(any(), eq(STR1_LD_EXP))).thenReturn(
        Collections.singleton(valueFactory.createLiteral(STR_VALUE_1)));
    when(ldPathExecutorMock.ldPathQuery(any(), eq(STR2_LD_EXP))).thenReturn(
        Collections.singleton(valueFactory.createLiteral(STR_VALUE_2)));

    objectProperty.setProperties(ImmutableMap.of(KEY_1, STR_PROPERTY_1, KEY_2, STR_PROPERTY_2,
        KEY_3, ARRAY_PROPERTY, KEY_4, STR_PROPERTY_3));


    when(entityBuilderContextMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);
  }

  @Test
  public void supportsObjectProperty() {
    assertTrue(objectPropertyHandler.supports(objectProperty));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void handleObjectWithoutProperties() {
    objectProperty.setProperties(ImmutableMap.of());

    Map<String, Object> result = (Map<String, Object>) registry.mapGraphValue(objectProperty,
            entityBuilderContextMock, registry, context1Mock);

    assertThat(result.keySet(), hasSize(0));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void handleObjectProperties() {
    objectProperty.setProperties(ImmutableMap.of(KEY_1, STR_PROPERTY_1, KEY_2, STR_PROPERTY_2,
        KEY_3, ARRAY_PROPERTY, KEY_4, STR_PROPERTY_3));
    when(ldPathExecutorMock.ldPathQuery(any(), eq(STR3_LD_EXP))).thenReturn(ImmutableList.of());

    Map<String, Object> result = (Map<String, Object>) registry.mapGraphValue(objectProperty,
            entityBuilderContextMock, registry, context1Mock);

    assertThat(result.keySet(), hasSize(4));
    assertThat(result, hasEntry(KEY_1, com.google.common.base.Optional.of(STR_VALUE_1)));
    assertThat(result, hasEntry(KEY_2, com.google.common.base.Optional.of(STR_VALUE_2)));
    assertThat(result, hasEntry(KEY_3, com.google.common.base.Optional.of(VALUE_3)));
    assertThat(result, hasEntry(KEY_4, com.google.common.base.Optional.absent()));
  }


  @Test
  public void handleObjectWithLdPathWithoutResult() {
    objectProperty.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR_1);

    when(ldPathExecutorMock.ldPathQuery(context1Mock, DUMMY_EXPR_1)).thenReturn(ImmutableSet.of());

    Object result =
        registry.mapGraphValue(objectProperty, entityBuilderContextMock, registry, context1Mock);

    assertThat(result, nullValue());
  }

  @Test
  public void handleObjectWithLdPathWithoutResultWhenRequiredThrowsException() {
    objectProperty.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR_1);
    objectProperty.setRequired(true);

    when(ldPathExecutorMock.ldPathQuery(context1Mock, DUMMY_EXPR_1)).thenReturn(ImmutableSet.of());

    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage(
        String.format("LDPath expression for a required object property ('%s') yielded no result.",
            DUMMY_EXPR_1));

    registry.mapGraphValue(objectProperty, entityBuilderContextMock, registry, context1Mock);
  }

  @Test
  public void handleObjectWithLdPathWithMultipleResultsThrowsException() {
    objectProperty.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR_1);

    when(ldPathExecutorMock.ldPathQuery(context1Mock, DUMMY_EXPR_1)).thenReturn(
        ImmutableSet.of(context1Mock, context2Mock));

    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage(String.format(
        "LDPath expression for object property ('%s') yielded multiple elements.", DUMMY_EXPR_1));

    registry.mapGraphValue(objectProperty, entityBuilderContextMock, registry, context1Mock);
  }

}
