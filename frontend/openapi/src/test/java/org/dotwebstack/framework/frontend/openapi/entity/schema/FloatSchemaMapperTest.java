package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.Collections;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
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
public class FloatSchemaMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private static final String DUMMY_EXPR = "dummyExpr()";
  private static final Literal VALUE_1 = SimpleValueFactory.getInstance().createLiteral(12.3f);
  private static final IRI VALUE_2 = SimpleValueFactory.getInstance().createIRI("http://foo");

  @Mock
  private GraphEntity graphEntityMock;
  @Mock
  private Value valueMock;
  @Mock
  private LdPathExecutor ldPathExecutorMock;
  @Mock
  private TupleEntity tupleEntityMock;

  private SchemaMapperAdapter schemaMapperAdapter;
  private FloatSchemaMapper floatSchemaMapper;
  private NumberSchema floatProperty;

  @Before
  public void setUp() {
    floatSchemaMapper = new FloatSchemaMapper();
    floatProperty = new NumberSchema();
    floatProperty.setFormat("float");

    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);
    schemaMapperAdapter = new SchemaMapperAdapter(Collections.singletonList(floatSchemaMapper));
  }

  @Test
  public void mapTupleValue_ThrowsException_ForNonLiterals() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("Value is not a literal value.");

    // Arrange & Act
    floatSchemaMapper.mapTupleValue(floatProperty, tupleEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN).build());
  }

  @Test
  public void mapTupleValue_ReturnValue_ForLiterals() {
    // Arrange & Act
    Float result = floatSchemaMapper.mapTupleValue(floatProperty, tupleEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN_HOP_USAGE_PER_YEAR).build());

    // Assert
    assertThat(result, is(DBEERPEDIA.BROUWTOREN_HOP_USAGE_PER_YEAR.floatValue()));
  }

  @Test
  public void supports_ReturnsTrue_ForFloatProperty() {
    // Arrange & Act
    Boolean supported = floatSchemaMapper.supports(floatProperty);

    // Assert
    assertThat(supported, is(true));
  }

  @Test
  public void supports_ReturnsFalse_ForStringProperty() {
    // Arrange & Act
    Boolean supported = floatSchemaMapper.supports(new StringSchema());

    // Assert
    assertThat(supported, is(false));
  }

  @Test
  public void supports_ReturnsFalse_ForDoubleProperty() {
    // Arrange
    Schema doubleProperty = new NumberSchema().format("double");

    // Act
    Boolean supported = floatSchemaMapper.supports(doubleProperty);

    // Assert
    assertThat(supported, is(false));
  }

  @Test
  public void mapGraphValue_ReturnsValue_ForLdPath() {
    // Arrange
    floatProperty.addExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutorMock.ldPathQuery(valueMock, DUMMY_EXPR)).thenReturn(
        ImmutableList.of(VALUE_1));

    // Act
    Float result = (Float) schemaMapperAdapter.mapGraphValue(floatProperty, false, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, is(VALUE_1.floatValue()));
  }

  @Test
  public void mapGraphValue_ThrowsException_ForUnsupportedType() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(String.format(
        "LDPathQuery '%s' yielded a value which is not a literal of supported type", DUMMY_EXPR));

    // Arrange
    floatProperty.addExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutorMock.ldPathQuery(eq(valueMock), anyString())).thenReturn(
        ImmutableList.of(VALUE_2));

    // Act
    schemaMapperAdapter.mapGraphValue(floatProperty, false, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);
  }
}
