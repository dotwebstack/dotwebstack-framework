package org.dotwebstack.framework.frontend.openapi.schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.DateProperty;
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
public class DatePropertyHandlerTest {

  private static final String DUMMY_EXPR = "dummyExpr()";
  private static final Literal VALUE_1 =
      SimpleValueFactory.getInstance().createLiteral("2016-12-24", XMLSchema.DATE);
  private static final Literal VALUE_3 = SimpleValueFactory.getInstance().createLiteral("foo");

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private GraphEntityContext entityBuilderContext;

  @Mock
  private SchemaMapperAdapter registry;

  @Mock
  private Value context;

  @Mock
  private LdPathExecutor ldPathExecutor;

  private DateSchemaMapper handler;
  private DateProperty property;

  @Before
  public void setUp() {
    handler = new DateSchemaMapper();
    property = new DateProperty();
    when(entityBuilderContext.getLdPathExecutor()).thenReturn(ldPathExecutor);
  }

  @Test
  public void supportsStringProperty() {
    assertThat(handler.supports(property), is(true));
  }

  @Test
  public void handleValidContextWithoutLdPathQuery() {
    LocalDate result = handler.mapGraphValue(property, entityBuilderContext, registry, VALUE_1);

    assertThat(result.toString(), is(VALUE_1.calendarValue().toString()));
    verifyZeroInteractions(ldPathExecutor);
  }

  @Test
  public void handleValidLdPathQuery() {
    property.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutor.ldPathQuery(eq(context), anyString())).thenReturn(
        ImmutableList.of(VALUE_1));

    LocalDate result = handler.mapGraphValue(property, entityBuilderContext, registry, context);

    assertThat(result.toString(), is(VALUE_1.calendarValue().toString()));
  }

  @Test
  public void handleUnsupportedLiteralDataType() {
    property.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutor.ldPathQuery(eq(context), anyString())).thenReturn(
        ImmutableList.of(VALUE_3));
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage(String.format(
        "LDPath query '%s' yielded a value which is not a literal of supported type: <%s>",
        DUMMY_EXPR, XMLSchema.DATE.stringValue()));

    handler.mapGraphValue(property, entityBuilderContext, registry, context);
  }

}
