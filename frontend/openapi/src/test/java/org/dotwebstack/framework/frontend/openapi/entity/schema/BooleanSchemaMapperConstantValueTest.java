package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.media.BooleanSchema;
import java.util.Collections;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.eclipse.rdf4j.model.Literal;
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
public class BooleanSchemaMapperConstantValueTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final String CONSTANT_VALUE = OpenApiSpecificationExtensions.CONSTANT_VALUE;
  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  @Mock
  private GraphEntity graphEntityMock;

  private SchemaMapperAdapter schemaMapperAdapter;
  private BooleanSchema booleanSchema;
  private ValueContext valueContext;

  @Before
  public void setUp() {
    booleanSchema = new BooleanSchema();
    schemaMapperAdapter =
        new SchemaMapperAdapter(Collections.singletonList(new BooleanSchemaMapper()));
    valueContext = ValueContext.builder().build();
  }

  @Test
  public void mapGraphValue_ReturnsBooleanValue_WhenStringConstantValueIsDefined() {
    // Arrange
    booleanSchema.setExtensions(ImmutableMap.of(CONSTANT_VALUE, "true"));

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(booleanSchema, false, graphEntityMock,
        valueContext, schemaMapperAdapter);

    // Assert
    assertBooleanTrue(result);
  }

  @Test
  public void mapGraphValue_ReturnsBooleanValue_WhenBooleanConstantValueIsDefined() {
    // Arrange
    booleanSchema.setExtensions(ImmutableMap.of(CONSTANT_VALUE, true));

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(booleanSchema, false, graphEntityMock,
        valueContext, schemaMapperAdapter);

    // Assert
    assertBooleanTrue(result);
  }

  @Test
  public void mapGraphValue_ReturnsBooleanValue_WhenSupportedLiteralConstantValueIsDefined() {
    // Arrange
    Literal literal = VALUE_FACTORY.createLiteral("true", XMLSchema.BOOLEAN);

    booleanSchema.setExtensions(ImmutableMap.of(CONSTANT_VALUE, literal));

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(booleanSchema, false, graphEntityMock,
        valueContext, schemaMapperAdapter);

    // Assert
    assertBooleanTrue(result);
  }

  private static void assertBooleanTrue(Object result) {
    assertThat(result, instanceOf(Boolean.class));
    assertThat(result, is(true));
  }
}
