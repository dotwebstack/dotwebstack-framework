package org.dotwebstack.framework.frontend.openapi.schema;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.IntegerProperty;
import java.util.Arrays;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
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
public class IntegerPropertyHandlerTest {

  private static final String DUMMY_EXPR = "dummyExpr()";
  private static final Literal VALUE_1 = SimpleValueFactory.getInstance().createLiteral(123);
  private static final IRI VALUE_3 = SimpleValueFactory.getInstance().createIRI("http://foo");

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private GraphEntityContext entityBuilderContext;

  @Mock
  private Value context;

  private SchemaMapperAdapter registry;

  @Mock
  private LdPathExecutor ldPathExecutor;

  private SchemaMapper handler;
  private IntegerProperty property;

  @Before
  public void setUp() {
    handler = new IntegerSchemaMapper();
    property = new IntegerProperty();
    when(entityBuilderContext.getLdPathExecutor()).thenReturn(ldPathExecutor);
    registry = new SchemaMapperAdapter(Arrays.asList(handler));

  }

  @Test
  public void supportsIntegerProperty() {
    assertThat(handler.supports(property), is(true));
  }

  @Test
  public void handleValidContextWithoutLdPathQuery() {
    Object result = registry.mapGraphValue(property, entityBuilderContext, registry, VALUE_1);

    assertThat(result, is(VALUE_1.integerValue().intValue()));
    verifyZeroInteractions(ldPathExecutor);
  }

  @Test
  public void handleValidLdPathQuery() {
    property.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutor.ldPathQuery(eq(context), anyString())).thenReturn(
        ImmutableList.of(VALUE_1));

    Integer result = (Integer)registry.mapGraphValue(property, entityBuilderContext, registry, context);

    assertThat(result, is(VALUE_1.integerValue().intValue()));
  }

  @Test
  public void handleUnsupportedLiteralDataType() {
    property.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutor.ldPathQuery(eq(context), anyString())).thenReturn(
        ImmutableList.of(VALUE_3));
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage(String.format(
        "LDPath query '%s' yielded a value which is not a literal of supported type: <%s>",
        DUMMY_EXPR, Joiner.on(", ").join(XMLSchema.INTEGER, XMLSchema.INT)));

    registry.mapGraphValue(property, entityBuilderContext, registry, context);
  }

  @Test
  public void testEmptyLdPath() {
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage(String.format("Property '%s' must have a '%s' attribute",
        property.getName(), OpenApiSpecificationExtensions.LDPATH));
    registry.mapGraphValue(property, entityBuilderContext, registry, context);
  }

}
