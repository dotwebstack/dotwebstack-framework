package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.DateTimeProperty;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
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
public class DateTimeSchemaMapperTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final String DUMMY_EXPR = "dummyExpr()";
  private static final Literal VALUE = SimpleValueFactory.getInstance().createLiteral("foo");

  @Mock
  private GraphEntity graphEntityMock;
  @Mock
  private SchemaMapperAdapter schemaMapperAdapterMock;
  @Mock
  private Value valueMock;
  @Mock
  private LdPathExecutor ldPathExecutor;

  private DateTimeSchemaMapper schemaMapper;
  private DateTimeProperty property;

  @Before
  public void setUp() {
    schemaMapper = new DateTimeSchemaMapper();
    property = new DateTimeProperty();
    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutor);
  }

  @Test
  public void supports_ReturnsTrue_ForDateTimeProperty() {
    // Act
    boolean result = schemaMapper.supports(property);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void mapGraphValue_ThrowsException_ForUnsupportedType() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(String.format(
        "LDPathQuery '%s' yielded a value which is not a literal of supported type",
        DUMMY_EXPR));

    // Arrange
    property.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutor.ldPathQuery(eq(valueMock), anyString())).thenReturn(
        ImmutableList.of(VALUE));

    // Act
    schemaMapper.mapGraphValue(property, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapterMock);
  }
}
