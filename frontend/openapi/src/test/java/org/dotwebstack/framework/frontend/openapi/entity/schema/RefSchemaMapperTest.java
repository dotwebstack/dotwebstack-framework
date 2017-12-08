package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
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
public class RefSchemaMapperTest {

  private static final String DUMMY_REF = "#/entityBuilderContext/fooModel";
  private static final String KEY_1 = "one";
  private static final String KEY_2 = "_two";

  private static final StringProperty PROPERTY_1 = new StringProperty();
  private static final IntegerProperty PROPERTY_2 = new IntegerProperty();
  private static final Literal VALUE_1 = SimpleValueFactory.getInstance().createLiteral("CONSTANT");
  private static final Literal VALUE_2 =
      SimpleValueFactory.getInstance().createLiteral("123", XMLSchema.INTEGER);

  private static final String LD_PATH_QUERY = ".";

  static {
    PROPERTY_1.getVendorExtensions().put(OpenApiSpecificationExtensions.CONSTANT_VALUE, VALUE_1);
    PROPERTY_2.getVendorExtensions().put(OpenApiSpecificationExtensions.LDPATH, LD_PATH_QUERY);
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private final RefSchemaMapper refPropertyHandler;

  @Mock
  private GraphEntityContext entityBuilderContext;

  @Mock
  private Value context;

  @Mock
  private LdPathExecutor ldPathExecutor;

  private RefProperty property;

  private SchemaMapperAdapter propertyHandlerRegistry;

  public RefSchemaMapperTest() {
    List<SchemaMapper<? extends Property, ?>> handlers = new ArrayList<>();
    refPropertyHandler = new RefSchemaMapper();

    handlers.add(refPropertyHandler);
    handlers.add(new IntegerSchemaMapper());
    handlers.add(new StringSchemaMapper());
    handlers.add(new ObjectSchemaMapper());

    propertyHandlerRegistry = new SchemaMapperAdapter(handlers);
  }

  @Before
  public void setUp() {
    property = new RefProperty();
  }

  @Test
  public void supportsObjectProperty() {
    assertTrue(refPropertyHandler.supports(property));
  }

  @Test
  public void handleDefinitionNotFound() {
    property.set$ref(DUMMY_REF);
    when(entityBuilderContext.getSwaggerDefinitions()).thenReturn(ImmutableMap.of());
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(String.format("Unable to resolve reference to swagger model: '%s'.",
        property.getSimpleRef()));

    refPropertyHandler.mapGraphValue(property, entityBuilderContext, propertyHandlerRegistry,
        context);
  }

  @Test
  public void handleDefinitionSchemaProperties() {
    property.set$ref(DUMMY_REF);
    Model refModel = new ModelImpl();
    refModel.setProperties(ImmutableMap.of(KEY_1, PROPERTY_1, KEY_2, PROPERTY_2));

    when(entityBuilderContext.getLdPathExecutor()).thenReturn(ldPathExecutor);
    when(entityBuilderContext.getSwaggerDefinitions()).thenReturn(
        ImmutableMap.of(property.getSimpleRef(), refModel));
    when(ldPathExecutor.ldPathQuery(context, LD_PATH_QUERY)).thenReturn(ImmutableList.of(VALUE_2));

    Map<String, Object> result = (Map<String, Object>) refPropertyHandler.mapGraphValue(property,
        entityBuilderContext, propertyHandlerRegistry, context);

    assertThat(result.keySet(), hasSize(2));
    assertEquals(((Optional) result.get(KEY_1)).orNull(), VALUE_1.stringValue());
    assertEquals(((Optional) result.get(KEY_2)).orNull(), VALUE_2.intValue());
  }

}
