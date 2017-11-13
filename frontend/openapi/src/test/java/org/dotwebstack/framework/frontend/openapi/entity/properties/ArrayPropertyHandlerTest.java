package org.dotwebstack.framework.frontend.openapi.entity.properties;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilderContext;
import org.dotwebstack.framework.frontend.openapi.entity.builder.OasVendorExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ArrayPropertyHandlerTest {

  private static final String DUMMY_EXPR = "dummyExpr()";
  private static final String DUMMY_NAME = "dummyName";
  private static final Value VALUE_1 = SimpleValueFactory.getInstance().createLiteral("a");
  private static final Value VALUE_2 = SimpleValueFactory.getInstance().createLiteral("b");
  private static final Value VALUE_3 = SimpleValueFactory.getInstance().createLiteral("c");

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private PropertyHandlerRegistry registry = new PropertyHandlerRegistry();

  @Mock
  private PropertyHandler<Property> itemHandler;

  @Mock
  private EntityBuilderContext entityBuilderContext;

  @Mock
  private Value context;

  @Mock
  private LdPathExecutor ldPathExecutor;

  private PropertyHandler<?> arrayHandler;
  private ArrayProperty property;

  @Before
  public void setUp() {
    arrayHandler = new ArrayPropertyHandler();
    property = new ArrayProperty();
    when(entityBuilderContext.getLdPathExecutor()).thenReturn(ldPathExecutor);

    ObjectProperty objProperty = new ObjectProperty();
    StringProperty stringProperty = new StringProperty();
    stringProperty.getVendorExtensions().put(OasVendorExtensions.LDPATH, "name");
    objProperty.property("firstName", stringProperty);
    property.setItems(objProperty);

    /* clear extensions */
    property.setVendorExtensions(Maps.newHashMap());

    registry.setPropertyHandlers(
        Arrays.asList(new ObjectPropertyHandler(), new StringPropertyHandler(), arrayHandler));
  }

  @Test
  public void supportsArrayProperty() {
    assertThat(arrayHandler.supports(property), is(true));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void arrayOfStringsWithinBounds() {
    property.setVendorExtension(OasVendorExtensions.LDPATH, DUMMY_EXPR);
    property.setItems(new StringProperty());
    property.setMinItems(1);
    property.setMaxItems(3);
    when(ldPathExecutor.ldPathQuery(any(Value.class), eq(DUMMY_EXPR))).thenReturn(
        ImmutableList.of(VALUE_1, VALUE_2, VALUE_3));

    List<Optional<String>> result =
        (List<Optional<String>>) registry.handle(property, entityBuilderContext, context);

    assertThat(result, Matchers.hasSize(3));
    assertThat(result.get(0), is(Optional.of(VALUE_1.stringValue())));
    assertThat(result.get(1), is(Optional.of(VALUE_2.stringValue())));
    assertThat(result.get(2), is(Optional.of(VALUE_3.stringValue())));
  }

  @Test
  @SuppressWarnings({"unchecked"})
  public void collectionOfObjects() throws Exception {
    property.setVendorExtension(OasVendorExtensions.RESULT_REF, "collection");

    IRI person1Iri = SimpleValueFactory.getInstance().createIRI("http://test.org#person1");
    IRI person2Iri = SimpleValueFactory.getInstance().createIRI("http://test.org#person2");

    QueryResult queryResult = QueryResult.builder().build(ImmutableList.of(person1Iri, person2Iri));

    when(entityBuilderContext.getQueryResult()).thenReturn(queryResult);
    when(ldPathExecutor.ldPathQuery(eq(person1Iri), eq("name"))).thenReturn(
        ImmutableList.of(stringLiteral("Nick 1")));
    when(ldPathExecutor.ldPathQuery(eq(person2Iri), eq("name"))).thenReturn(
        ImmutableList.of(stringLiteral("Nick 2")));

    Collection<Map<String, Object>> collection =
        (Collection<Map<String, Object>>) registry.handle(property, entityBuilderContext, context);
    assertThat(collection, Matchers.hasSize(2));

    Map<String, Object> person1 = Maps.newHashMap();
    person1.put("firstName", Optional.of("Nick 1"));
    Map<String, Object> person2 = Maps.newHashMap();
    person2.put("firstName", Optional.of("Nick 2"));

    Iterator<Map<String, Object>> iterator = collection.iterator();
    assertThat(iterator.next(), is(person1));
    assertThat(iterator.next(), is(person2));
  }

  private Literal stringLiteral(String value) {
    return SimpleValueFactory.getInstance().createLiteral(value);
  }

  @Test
  public void ldPathOrResultRefRequired() {
    property.setName(DUMMY_NAME);

    thrown.expect(PropertyHandlerRuntimeException.class);
    thrown.expectMessage(String.format("ArrayProperty must have either a '%s', of a '%s' attribute",
        OasVendorExtensions.LDPATH, OasVendorExtensions.RESULT_REF));

    registry.handle(property, entityBuilderContext, context);
  }

  @Test
  public void arrayBoundsLowerLimitViolated() {
    property.setVendorExtension(OasVendorExtensions.LDPATH, DUMMY_EXPR);
    property.setItems(new StringProperty());
    property.setMinItems(2);
    when(ldPathExecutor.ldPathQuery(any(Value.class), eq(DUMMY_EXPR))).thenReturn(
        ImmutableList.of(VALUE_1));
    thrown.expect(PropertyHandlerRuntimeException.class);
    thrown.expectMessage(
        String.format("Mapping for property yielded 1 elements, which is less than 'minItems' (%d)"
            + " specified in the OpenAPI specification", property.getMinItems()));

    registry.handle(property, entityBuilderContext, context);
  }

  @Test
  public void arrayBoundsUpperLimitViolated() {
    property.setVendorExtension(OasVendorExtensions.LDPATH, DUMMY_EXPR);
    property.setItems(new StringProperty());
    property.setMaxItems(2);
    when(ldPathExecutor.ldPathQuery(any(Value.class), eq(DUMMY_EXPR))).thenReturn(
        ImmutableList.of(VALUE_1, VALUE_2, VALUE_3));
    thrown.expect(PropertyHandlerRuntimeException.class);
    thrown.expectMessage(
        String.format("Mapping for property yielded 3 elements, which is more than 'maxItems' (%d)"
            + " specified in the OpenAPI specification", property.getMaxItems()));

    registry.handle(property, entityBuilderContext, context);
  }

}
