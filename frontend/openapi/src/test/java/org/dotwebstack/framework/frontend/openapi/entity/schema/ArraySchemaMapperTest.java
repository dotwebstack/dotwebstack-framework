package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.Rdf4jUtils;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ArraySchemaMapperTest {

  private static final String DUMMY_EXPR = "dummyExpr()";

  private static final String DUMMY_NAME = "dummyName";

  private static final Value VALUE_1 = SimpleValueFactory.getInstance().createLiteral("a");

  private static final Value VALUE_2 = SimpleValueFactory.getInstance().createLiteral("b");

  private static final Value VALUE_3 = SimpleValueFactory.getInstance().createLiteral("c");

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private GraphEntity entityMock;

  @Mock
  private Value valueMock;

  @Mock
  private LdPathExecutor ldPathExecutorMock;

  private SchemaMapperAdapter schemaMapperAdapter;

  private ArraySchemaMapper schemaMapper;

  private StringProperty stringSchema;

  private ObjectProperty objectSchema;

  private ArrayProperty arraySchema;

  @Before
  public void setUp() {
    schemaMapper = new ArraySchemaMapper();

    List<SchemaMapper<? extends Property, ?>> schemaMappers = new ArrayList<>();

    schemaMappers.add(schemaMapper);
    schemaMappers.add(new ObjectSchemaMapper());
    schemaMappers.add(new StringSchemaMapper());

    schemaMapperAdapter = new SchemaMapperAdapter(schemaMappers);

    when(entityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);

    stringSchema = new StringProperty();
    stringSchema.getVendorExtensions().put(OpenApiSpecificationExtensions.LDPATH, "name");

    objectSchema = new ObjectProperty();
    objectSchema.property("name", stringSchema);

    arraySchema = new ArrayProperty();
  }

  @Test
  public void supports_ReturnsTrue_ForArrayProperty() {
    // Act
    boolean result = schemaMapper.supports(arraySchema);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void supports_ReturnsFalse_ForNonArrayProperty() {
    // Act
    boolean result = schemaMapper.supports(objectSchema);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void getSupportedDataTypes_ReturnsEmptySet() {
    // Act
    Set<IRI> result = schemaMapper.getSupportedDataTypes();

    // Assert
    assertThat(result, empty());
  }

  @Test
  public void mapGraphValue_ReturnsEmptyResult_WhenNoValueHasBeenDefined() {
    // Arrange
    Value value = null;

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(arraySchema, entityMock,
        ValueContext.builder().value(value).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, instanceOf(List.class));

    List<Optional<String>> list = (List) result;

    assertThat(list, empty());
  }

  @Test
  public void mapGraphValue_ReturnsArrayOfStrings_WhenNoSubjectQueryHasBeenDefined() {
    // Arrange
    arraySchema.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    arraySchema.setItems(new StringProperty());

    when(ldPathExecutorMock.ldPathQuery(any(Value.class), eq(DUMMY_EXPR))).thenReturn(
        ImmutableList.of(VALUE_1, VALUE_2, VALUE_3));

    // Act
    List<Optional<String>> result =
        (List<Optional<String>>) schemaMapperAdapter.mapGraphValue(arraySchema, entityMock,
            ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, Matchers.hasSize(3));
    assertThat(result.get(0), is(Optional.of(VALUE_1.stringValue())));
    assertThat(result.get(1), is(Optional.of(VALUE_2.stringValue())));
    assertThat(result.get(2), is(Optional.of(VALUE_3.stringValue())));
  }

  // @Test
  public void mapGraphValue_ReturnsArrayOfStrings_WhenSubjectQueryHasBeenDefined() {
    // Arrange
    arraySchema.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_QUERY,
        String.format("SELECT ?s WHERE { ?s <%s> <%s>}", RDF.TYPE, DBEERPEDIA.BREWERY_TYPE),
        OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR));
    arraySchema.setItems(new StringProperty());

    Model model = new ModelBuilder().subject(DBEERPEDIA.BROUWTOREN).add(RDF.TYPE,
        DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME, DBEERPEDIA.BROUWTOREN_NAME).subject(
            DBEERPEDIA.MAXIMUS).add(RDF.TYPE, DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME,
                DBEERPEDIA.MAXIMUS_NAME).build();
    when(entityMock.getRepository()).thenReturn(Rdf4jUtils.asRepository(model));

    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.BROUWTOREN, DUMMY_EXPR)).thenReturn(
        ImmutableList.of(DBEERPEDIA.BROUWTOREN_NAME));
    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.MAXIMUS, DUMMY_EXPR)).thenReturn(
        ImmutableList.of(DBEERPEDIA.MAXIMUS_NAME));

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(arraySchema, entityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, instanceOf(List.class));

    List<Optional<String>> list = (List) result;

    assertThat(list, contains(Optional.of(DBEERPEDIA.BROUWTOREN_NAME.stringValue()),
        Optional.of(DBEERPEDIA.MAXIMUS_NAME.stringValue())));
  }

  @Test
  public void mapGraphValue_ReturnsArrayOfObjects_WhenSubjectExtEnabled() {
    // Arrange
    arraySchema.setItems(objectSchema);
    arraySchema.setVendorExtension(OpenApiSpecificationExtensions.SUBJECT, true);

    when(entityMock.getSubjects()).thenReturn(
        ImmutableSet.of(DBEERPEDIA.BROUWTOREN, DBEERPEDIA.MAXIMUS));

    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.BROUWTOREN, "name")).thenReturn(
        ImmutableList.of(DBEERPEDIA.BROUWTOREN_NAME));
    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.MAXIMUS, "name")).thenReturn(
        ImmutableList.of(DBEERPEDIA.MAXIMUS_NAME));

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(arraySchema, entityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, instanceOf(List.class));

    List<Optional<String>> list = (List) result;

    assertThat(list,
        containsInAnyOrder(
            ImmutableMap.of("name", Optional.of(DBEERPEDIA.BROUWTOREN_NAME.stringValue())),
            ImmutableMap.of("name", Optional.of(DBEERPEDIA.MAXIMUS_NAME.stringValue()))));
  }

  @Test
  public void mapGraphValue_ThrowsException_ForMissingLdPathOrResultRef() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(String.format("ArrayProperty must have a '%s' attribute",
        OpenApiSpecificationExtensions.LDPATH));

    // Arrange
    arraySchema.setName(DUMMY_NAME);

    // Act
    schemaMapperAdapter.mapGraphValue(arraySchema, entityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ThrowsException_WhenArrayBoundsLowerLimitViolated() {
    // Arrange
    arraySchema.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    arraySchema.setItems(new StringProperty());
    arraySchema.setMinItems(2);

    when(ldPathExecutorMock.ldPathQuery(any(Value.class), eq(DUMMY_EXPR))).thenReturn(
        ImmutableList.of(VALUE_1));

    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(
        String.format("Mapping for property yielded 1 elements, which is less than 'minItems' (%d)"
            + " specified in the OpenAPI specification", arraySchema.getMinItems()));

    // Act
    schemaMapperAdapter.mapGraphValue(arraySchema, entityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ThrowsException_WhenArrayBoundsUpperLimitViolated() {
    // Arrange
    arraySchema.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    arraySchema.setItems(new StringProperty());
    arraySchema.setMaxItems(2);

    when(ldPathExecutorMock.ldPathQuery(any(Value.class), eq(DUMMY_EXPR))).thenReturn(
        ImmutableList.of(VALUE_1, VALUE_2, VALUE_3));

    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(
        String.format("Mapping for property yielded 3 elements, which is more than 'maxItems' (%d)"
            + " specified in the OpenAPI specification", arraySchema.getMaxItems()));

    // Act
    schemaMapperAdapter.mapGraphValue(arraySchema, entityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);
  }

}
