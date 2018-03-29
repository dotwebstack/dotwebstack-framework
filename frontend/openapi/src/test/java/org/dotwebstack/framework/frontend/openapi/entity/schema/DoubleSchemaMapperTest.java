package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.StringProperty;
import java.util.Arrays;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
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
public class DoubleSchemaMapperTest {

  private static final String DUMMY_EXPR = "dummyExpr()";
  private static final Literal VALUE_1 = SimpleValueFactory.getInstance().createLiteral(12.3);
  private static final IRI VALUE_3 = SimpleValueFactory.getInstance().createIRI("http://foo");

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private TupleEntity tupleEntityMock;

  @Mock
  private GraphEntity graphEntityMock;

  @Mock
  private Value context;

  private SchemaMapperAdapter schemaMapperAdapter;

  @Mock
  private LdPathExecutor ldPathExecutor;

  private DoubleSchemaMapper schemaMapper;

  private DoubleProperty schema;

  @Before
  public void setUp() {
    schemaMapper = new DoubleSchemaMapper();
    schema = new DoubleProperty();

    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutor);
    schemaMapperAdapter = new SchemaMapperAdapter(Arrays.asList(schemaMapper));
  }

  @Test
  public void mapTupleValue_ThrowsException_ForNonLiterals() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("Value is not a literal value.");

    // Arrange & Act
    schemaMapper.mapTupleValue(schema, tupleEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN).build());
  }

  @Test
  public void mapTupleValue_ReturnValue_ForLiterals() {
    // Arrange & Act
    Double result = (Double) schemaMapper.mapTupleValue(schema, tupleEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION).build());

    // Assert
    assertThat(result, equalTo(DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION.doubleValue()));
  }

  @Test
  public void supports_ReturnsTrue_ForIntegerProperty() {
    // Arrange & Act
    Boolean supported = schemaMapper.supports(schema);

    // Assert
    assertThat(supported, equalTo(true));
  }

  @Test
  public void supports_ReturnsTrue_ForNonIntegerProperty() {
    // Arrange & Act
    Boolean supported = schemaMapper.supports(new StringProperty());

    // Assert
    assertThat(supported, equalTo(false));
  }

  @Test
  public void mapGraphValue_ReturnsValue_WhenNoLdPathHasBeenSupplied() {
    // Act
    Object result = schemaMapperAdapter.mapGraphValue(schema, graphEntityMock,
        ValueContext.builder().value(VALUE_1).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, is(VALUE_1.doubleValue()));
    verifyZeroInteractions(ldPathExecutor);
  }

  @Test
  public void mapGraphValue_ReturnsValue_ForLdPath() {
    // Arrange
    schema.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutor.ldPathQuery(eq(context), anyString())).thenReturn(
        ImmutableList.of(VALUE_1));

    // Act
    Double result = (Double) schemaMapperAdapter.mapGraphValue(schema, graphEntityMock,
        ValueContext.builder().value(context).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, is(VALUE_1.doubleValue()));
  }

  @Test
  public void mapGraphValue_ThrowsException_ForUnsupportedType() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(String.format(
        "LDPath query '%s' yielded a value which is not a literal of supported type: <%s>",
        DUMMY_EXPR, XMLSchema.DOUBLE));

    // Arrange
    schema.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutor.ldPathQuery(eq(context), anyString())).thenReturn(
        ImmutableList.of(VALUE_3));

    // Act
    schemaMapperAdapter.mapGraphValue(schema, graphEntityMock,
        ValueContext.builder().value(context).build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ThrowsException_ForEmptyLdPath() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(String.format("Property '%s' must have a '%s' attribute", schema.getName(),
        OpenApiSpecificationExtensions.LDPATH));

    // Act
    schemaMapperAdapter.mapGraphValue(schema, graphEntityMock,
        ValueContext.builder().value(context).build(), schemaMapperAdapter);
  }

}
