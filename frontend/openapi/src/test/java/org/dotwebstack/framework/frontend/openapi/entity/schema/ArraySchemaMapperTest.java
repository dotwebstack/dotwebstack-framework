package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ArraySchemaMapperTest {

  private static final String DUMMY_EXPR = "dummyExpr()";

  private static final String DUMMY_NAME = "dummyName";

  private static final Value VALUE_1 = SimpleValueFactory.getInstance().createLiteral("a");

  private static final Value VALUE_2 = SimpleValueFactory.getInstance().createLiteral("b");

  private static final Value VALUE_3 = SimpleValueFactory.getInstance().createLiteral("c");

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private SchemaMapperAdapter registry;

  @Mock
  private GraphEntityContext entityBuilderContext;

  @Mock
  private Value context;

  @Mock
  private LdPathExecutor ldPathExecutor;

  private SchemaMapper arrayHandler;
  private ArrayProperty property;

  @Before
  public void setUp() {
    List<SchemaMapper<? extends Property, ?>> schemaMappers = new ArrayList<>();

    arrayHandler = new ArraySchemaMapper();
    schemaMappers.add(new ObjectSchemaMapper());
    schemaMappers.add(new StringSchemaMapper());
    schemaMappers.add(arrayHandler);
    registry = new SchemaMapperAdapter(schemaMappers);

    property = new ArrayProperty();
    when(entityBuilderContext.getLdPathExecutor()).thenReturn(ldPathExecutor);

    ObjectProperty objProperty = new ObjectProperty();
    StringProperty stringProperty = new StringProperty();
    stringProperty.getVendorExtensions().put(OpenApiSpecificationExtensions.LDPATH, "name");
    objProperty.property("firstName", stringProperty);
    property.setItems(objProperty);

    /* clear extensions */
    property.setVendorExtensions(Maps.newHashMap());

  }

  @Test
  public void supportsArrayProperty() {
    assertThat(arrayHandler.supports(property), is(true));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void arrayOfStringsWithinBounds() {
    property.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    property.setItems(new StringProperty());
    property.setMinItems(1);
    property.setMaxItems(3);
    when(ldPathExecutor.ldPathQuery(any(Value.class), eq(DUMMY_EXPR))).thenReturn(
        ImmutableList.of(VALUE_1, VALUE_2, VALUE_3));

    List<Optional<String>> result = (List<Optional<String>>) registry.mapGraphValue(property,
        entityBuilderContext, registry, context);

    assertThat(result, Matchers.hasSize(3));
    assertThat(result.get(0), is(com.google.common.base.Optional.of(VALUE_1.stringValue())));
    assertThat(result.get(1), is(com.google.common.base.Optional.of(VALUE_2.stringValue())));
    assertThat(result.get(2), is(com.google.common.base.Optional.of(VALUE_3.stringValue())));
  }

  @Test
  public void ldPathOrResultRefRequired() {
    property.setName(DUMMY_NAME);

    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(String.format("ArrayProperty must have either a '%s', of a '%s' attribute",
        OpenApiSpecificationExtensions.LDPATH, OpenApiSpecificationExtensions.RESULT_REF));

    registry.mapGraphValue(property, entityBuilderContext, registry, context);
  }

  @Test
  public void arrayBoundsLowerLimitViolated() {
    property.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    property.setItems(new StringProperty());
    property.setMinItems(2);
    when(ldPathExecutor.ldPathQuery(any(Value.class), eq(DUMMY_EXPR))).thenReturn(
        ImmutableList.of(VALUE_1));
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(
        String.format("Mapping for property yielded 1 elements, which is less than 'minItems' (%d)"
            + " specified in the OpenAPI specification", property.getMinItems()));

    registry.mapGraphValue(property, entityBuilderContext, registry, context);
  }

  @Test
  public void arrayBoundsUpperLimitViolated() {
    property.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    property.setItems(new StringProperty());
    property.setMaxItems(2);
    when(ldPathExecutor.ldPathQuery(any(Value.class), eq(DUMMY_EXPR))).thenReturn(
        ImmutableList.of(VALUE_1, VALUE_2, VALUE_3));
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(
        String.format("Mapping for property yielded 3 elements, which is more than 'maxItems' (%d)"
            + " specified in the OpenAPI specification", property.getMaxItems()));

    registry.mapGraphValue(property, entityBuilderContext, registry, context);
  }

}
