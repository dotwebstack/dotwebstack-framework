package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.media.IntegerSchema;
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
public class LongSchemaMapperTest {
  
  private static final String DUMMY_EXPR = "dummyExpr()";
  private static final Literal VALUE_1 = SimpleValueFactory.getInstance().createLiteral(12345L);
  private static final IRI VALUE_2 = SimpleValueFactory.getInstance().createIRI("http://foo");

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private GraphEntity graphEntityMock;
  @Mock
  private Value valueMock;
  @Mock
  private LdPathExecutor ldPathExecutorMock;
  @Mock
  private TupleEntity tupleEntityMock;

  private SchemaMapperAdapter schemaMapperAdapter;
  private LongSchemaMapper longSchemaMapper;
  private IntegerSchema longProperty;

  @Before
  public void setUp() {
    longSchemaMapper = new LongSchemaMapper();
    longProperty = new IntegerSchema().format("int64");

    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);
    schemaMapperAdapter = new SchemaMapperAdapter(Collections.singletonList(longSchemaMapper));
  }

  @Test
  public void mapTupleValue_ThrowsException_ForNonLiterals() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("Value is not a literal value.");

    // Arrange & Act
    longSchemaMapper.mapTupleValue(longProperty, tupleEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN).build());
  }

  @Test
  public void mapTupleValue_ReturnValue_ForLiterals() {
    // Arrange & Act
    Long result = longSchemaMapper.mapTupleValue(longProperty, tupleEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN_LITERS_PER_YEAR).build());

    // Assert
    assertThat(result, equalTo(DBEERPEDIA.BROUWTOREN_LITERS_PER_YEAR.longValue()));
  }

  @Test
  public void supports_ReturnsTrue_ForLongProperty() {
    // Arrange & Act
    Boolean supported = longSchemaMapper.supports(longProperty);

    // Assert
    assertThat(supported, equalTo(true));
  }

  @Test
  public void supports_ReturnsFalse_ForNonIntegerProperty() {
    // Arrange & Act
    Boolean supported = longSchemaMapper.supports(new StringSchema());

    // Assert
    assertThat(supported, equalTo(false));
  }

  @Test
  public void supports_ReturnsFalse_ForIntegerProperty() {
    // Arrange
    IntegerSchema integerSchema = new IntegerSchema();

    // Act
    Boolean supported = longSchemaMapper.supports(integerSchema);

    // Assert
    assertThat(supported, equalTo(false));
  }

  @Test
  public void mapGraphValue_ReturnsValue_ForLdPath() {
    // Arrange
    longProperty.addExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutorMock.ldPathQuery(valueMock, DUMMY_EXPR)).thenReturn(
        ImmutableList.of(VALUE_1));
 
    // Act
    Long result = (Long) schemaMapperAdapter.mapGraphValue(longProperty, false, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, is(VALUE_1.longValue()));
  }

  @Test
  public void mapGraphValue_ThrowsException_ForUnsupportedType() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(String.format(
        "LDPathQuery '%s' yielded a value which is not a literal of supported type", DUMMY_EXPR));

    // Arrange
    longProperty.addExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutorMock.ldPathQuery(eq(valueMock), anyString())).thenReturn(
        ImmutableList.of(VALUE_2));

    // Act
    schemaMapperAdapter.mapGraphValue(longProperty, false, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);
  }
}
