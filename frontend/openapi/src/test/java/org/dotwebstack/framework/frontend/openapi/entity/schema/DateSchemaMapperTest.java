package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.DateProperty;
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
  private static final Literal VALUE_3 = SimpleValueFactory.getInstance().createLiteral("foo");

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private GraphEntity entityMock;

  @Mock
  private SchemaMapperAdapter schemaMapperAdapter;

  @Mock
  private Value context;

  @Mock
  private LdPathExecutor ldPathExecutor;

  private DateSchemaMapper schemaMapper;
  private DateProperty property;

  @Before
  public void setUp() {
    schemaMapper = new DateSchemaMapper();
    property = new DateProperty();
    when(entityMock.getLdPathExecutor()).thenReturn(ldPathExecutor);
  }

  // XXX: En als je een niet-DateProperty meegeeft?
  @Test
  public void supports_ReturnsTrue_ForDateProperty() {
    // Act
    boolean result = schemaMapper.supports(property);

    // Arrange
    assertThat(result, is(true));
  }

  // XXX: Hier missen nog een paar testen voor de andere methoden in de klasse

  @Test
  public void mapGraphValue_ThrowsException_ForUnsupportedType() {
    // Assert
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage(String.format(
        "LDPath query '%s' yielded a value which is not a literal of supported type: <%s>",
        DUMMY_EXPR, XMLSchema.DATE.stringValue()));

    // Arrange
    property.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutor.ldPathQuery(eq(context), anyString())).thenReturn(
        ImmutableList.of(VALUE_3));

    // Act
    schemaMapper.mapGraphValue(property, entityMock, ValueContext.builder().value(context).build(),
        schemaMapperAdapter);
  }
}
