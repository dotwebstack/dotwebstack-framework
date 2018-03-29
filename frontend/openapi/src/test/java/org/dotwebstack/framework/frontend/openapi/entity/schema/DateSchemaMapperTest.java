package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.DateProperty;
import java.time.LocalDate;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
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
public class DateSchemaMapperTest {

  private static final String DUMMY_EXPR = "dummyExpr()";
  private static final Literal VALUE_1 =
      SimpleValueFactory.getInstance().createLiteral("2016-12-24", XMLSchema.DATE);
  private static final Literal VALUE_3 = SimpleValueFactory.getInstance().createLiteral("foo");

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private GraphEntity graphEntityMock;

  @Mock
  private SchemaMapperAdapter schemaMapperAdapter;

  @Mock
  private Value context;

  @Mock
  private LdPathExecutor ldPathExecutor;

  private DateSchemaMapper schemaMapper;

  private DateProperty schema;

  @Before
  public void setUp() {
    schemaMapper = new DateSchemaMapper();
    schema = new DateProperty();
    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutor);
  }

  @Test
  public void supports_ReturnsTrue_ForDateProperty() {
    // Act
    boolean result = schemaMapper.supports(schema);

    // Arrange
    assertThat(result, is(true));
  }

  @Test
  public void mapGraphValue_ReturnsValue_WhenNoLdPathHasBeenSupplied() {
    // Act
    LocalDate result = schemaMapper.mapGraphValue(schema, graphEntityMock,
        ValueContext.builder().value(VALUE_1).build(), schemaMapperAdapter);

    // Assert
    assertThat(result.toString(), is(VALUE_1.calendarValue().toString()));
    verifyZeroInteractions(ldPathExecutor);
  }


  @Test
  public void mapGraphValue_ReturnsValue_ForLdPath() {
    // Arrange
    schema.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutor.ldPathQuery(eq(context), anyString())).thenReturn(
        ImmutableList.of(VALUE_1));

    LocalDate result = schemaMapper.mapGraphValue(schema, graphEntityMock,
        ValueContext.builder().value(context).build(), schemaMapperAdapter);

    // Assert
    assertThat(result.toString(), is(VALUE_1.calendarValue().toString()));
  }

  @Test
  public void mapGraphValue_ThrowsException_ForUnsupportedType() {
    // Assert
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage(String.format(
        "LDPath query '%s' yielded a value which is not a literal of supported type: <%s>",
        DUMMY_EXPR, XMLSchema.DATE.stringValue()));
    // Arrange
    schema.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutor.ldPathQuery(eq(context), anyString())).thenReturn(
        ImmutableList.of(VALUE_3));

    // Act

    schemaMapper.mapGraphValue(schema, graphEntityMock,
        ValueContext.builder().value(context).build(), schemaMapperAdapter);
  }

}
