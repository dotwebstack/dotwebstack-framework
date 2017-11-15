package org.dotwebstack.framework.frontend.openapi.entity.builder.properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.DateProperty;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilderContext;
import org.dotwebstack.framework.frontend.openapi.entity.builder.OasVendorExtensions;
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
  private EntityBuilderContext entityBuilderContext;

  @Mock
  private PropertyHandlerRegistry registry;

  @Mock
  private Value context;

  @Mock
  private LdPathExecutor ldPathExecutor;

  private DatePropertyHandler handler;
  private DateProperty property;

  @Before
  public void setUp() {
    handler = new DatePropertyHandler();
    property = new DateProperty();
    when(entityBuilderContext.getLdPathExecutor()).thenReturn(ldPathExecutor);
  }

  @Test
  public void supportsStringProperty() {
    assertThat(handler.supports(property), is(true));
  }

  @Test
  public void handleValidContextWithoutLdPathQuery() {
    Object result = handler.handle(property, entityBuilderContext, registry, VALUE_1);

    assertThat(result, is(VALUE_1.calendarValue()));
    verifyZeroInteractions(ldPathExecutor);
  }

  @Test
  public void handleValidLdPathQuery() {
    property.setVendorExtension(OasVendorExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutor.ldPathQuery(eq(context), anyString())).thenReturn(
        ImmutableList.of(VALUE_1));

    Object result = handler.handle(property, entityBuilderContext, registry, context);

    assertThat(result, is(VALUE_1.calendarValue()));
  }

  @Test
  public void handleUnsupportedLiteralDataType() {
    property.setVendorExtension(OasVendorExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutor.ldPathQuery(eq(context), anyString())).thenReturn(
        ImmutableList.of(VALUE_3));
    expectedException.expect(PropertyHandlerRuntimeException.class);
    expectedException.expectMessage(String.format(
        "LDPath query '%s' yielded a value which is not a literal of supported type: <%s>",
        DUMMY_EXPR, XMLSchema.DATE.stringValue()));

    handler.handle(property, entityBuilderContext, registry, context);
  }

}
