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
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
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

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final String DUMMY_EXPR = "dummyExpr()";
  private static final String DUMMY_NAME = "dummyName";
  private static final Value VALUE_1 = SimpleValueFactory.getInstance().createLiteral("a");
  private static final Value VALUE_2 = SimpleValueFactory.getInstance().createLiteral("b");
  private static final Value VALUE_3 = SimpleValueFactory.getInstance().createLiteral("c");

  @Mock
  private GraphEntity graphEntityMock;
  @Mock
  private Value valueMock;
  @Mock
  private LdPathExecutor ldPathExecutorMock;

  private SchemaMapperAdapter schemaMapperAdapter;
  private ArraySchemaMapper arraySchemaMapper;
  private ObjectSchema objectSchema;
  private ArraySchema arraySchema;

  @Before
  public void setUp() {
    arraySchemaMapper = new ArraySchemaMapper();

    List<SchemaMapper<? extends Schema, ?>> schemaMappers = new ArrayList<>();

    schemaMappers.add(arraySchemaMapper);
    schemaMappers.add(new ObjectSchemaMapper());
    schemaMappers.add(new StringSchemaMapper());

    schemaMapperAdapter = new SchemaMapperAdapter(schemaMappers);

    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);
    StringSchema stringSchema = new StringSchema();
    stringSchema.addExtension(OpenApiSpecificationExtensions.LDPATH, "name");

    objectSchema = new ObjectSchema();
    objectSchema.addProperties("name", stringSchema);

    arraySchema = new ArraySchema();
  }

  @Test
  public void supports_ReturnsTrue_ForArraySchema() {
    // Act
    boolean result = arraySchemaMapper.supports(arraySchema);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void supports_ReturnsFalse_ForNonArraySchema() {
    // Act
    boolean result = arraySchemaMapper.supports(objectSchema);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void getSupportedDataTypes_ReturnsEmptySet() {
    // Act
    Set<IRI> result = arraySchemaMapper.getSupportedDataTypes();

    // Assert
    assertThat(result, empty());
  }

  @Test
  public void mapGraphValue_ReturnsEmptyResult_WhenNoValueHasBeenDefined() {
    // Act
    Object result = schemaMapperAdapter.mapGraphValue(arraySchema, false, graphEntityMock,
        ValueContext.builder().value(null).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, instanceOf(List.class));

    List<Optional<String>> list = (List) result;

    assertThat(list, empty());
  }

  @Test
  public void mapGraphValue_ReturnsArrayOfStrings_WhenNoSubjectQueryHasBeenDefined() {
    // Arrange
    arraySchema.addExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    arraySchema.setItems(new StringSchema());

    when(ldPathExecutorMock.ldPathQuery(any(Value.class), eq(DUMMY_EXPR))).thenReturn(
        ImmutableList.of(VALUE_1, VALUE_2, VALUE_3));

    // Act
    List<Optional<String>> result =
        (List<Optional<String>>) schemaMapperAdapter.mapGraphValue(arraySchema, false,
            graphEntityMock, ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, Matchers.hasSize(3));
    assertThat(result.get(0), is(Optional.of(VALUE_1.stringValue())));
    assertThat(result.get(1), is(Optional.of(VALUE_2.stringValue())));
    assertThat(result.get(2), is(Optional.of(VALUE_3.stringValue())));
  }

  // @Test
  public void mapGraphValue_ReturnsArrayOfStrings_WhenSubjectQueryHasBeenDefined() {
    // Arrange
    arraySchema.setExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_QUERY,
        String.format("SELECT ?s WHERE { ?s <%s> <%s>}", RDF.TYPE, DBEERPEDIA.BREWERY_TYPE),
        OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR));
    arraySchema.setItems(new StringSchema());

    Model model = new ModelBuilder().subject(DBEERPEDIA.BROUWTOREN).add(RDF.TYPE,
        DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME, DBEERPEDIA.BROUWTOREN_NAME).subject(
            DBEERPEDIA.MAXIMUS).add(RDF.TYPE, DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME,
                DBEERPEDIA.MAXIMUS_NAME).build();
    when(graphEntityMock.getRepository()).thenReturn(Rdf4jUtils.asRepository(model));

    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.BROUWTOREN, DUMMY_EXPR)).thenReturn(
        ImmutableList.of(DBEERPEDIA.BROUWTOREN_NAME));
    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.MAXIMUS, DUMMY_EXPR)).thenReturn(
        ImmutableList.of(DBEERPEDIA.MAXIMUS_NAME));

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(arraySchema, false, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, instanceOf(List.class));

    List<Optional<String>> list = (List<Optional<String>>) result;

    assertThat(list, contains(Optional.of(DBEERPEDIA.BROUWTOREN_NAME.stringValue()),
        Optional.of(DBEERPEDIA.MAXIMUS_NAME.stringValue())));
  }

  @Test
  public void mapGraphValue_ReturnsArrayOfObjects_WhenSubjectExtEnabled() {
    // Arrange
    arraySchema.setItems(objectSchema);
    arraySchema.setExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT, true));

    when(graphEntityMock.getSubjects()).thenReturn(
        ImmutableSet.of(DBEERPEDIA.BROUWTOREN, DBEERPEDIA.MAXIMUS));

    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.BROUWTOREN, "name")).thenReturn(
        ImmutableList.of(DBEERPEDIA.BROUWTOREN_NAME));
    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.MAXIMUS, "name")).thenReturn(
        ImmutableList.of(DBEERPEDIA.MAXIMUS_NAME));

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(arraySchema, false, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, instanceOf(List.class));

    List<Optional<String>> list = (List<Optional<String>>) result;

    assertThat(list,
        containsInAnyOrder(
            ImmutableMap.of("name", Optional.of(DBEERPEDIA.BROUWTOREN_NAME.stringValue())),
            ImmutableMap.of("name", Optional.of(DBEERPEDIA.MAXIMUS_NAME.stringValue()))));
  }

  @Test
  public void mapGraphValue_ThrowsException_ForMissingLdPathOrResultRef() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(String.format("ArraySchema must have a '%s' attribute",
        OpenApiSpecificationExtensions.LDPATH));

    // Arrange
    arraySchema.setName(DUMMY_NAME);

    // Act
    schemaMapperAdapter.mapGraphValue(arraySchema, false, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ThrowsException_WhenArrayBoundsLowerLimitViolated() {
    // Arrange
    arraySchema.addExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    arraySchema.setItems(new StringSchema());
    arraySchema.setMinItems(2);

    when(ldPathExecutorMock.ldPathQuery(any(Value.class), eq(DUMMY_EXPR))).thenReturn(
        ImmutableList.of(VALUE_1));

    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(
        String.format("Mapping for property yielded 1 elements, which is less than 'minItems' (%d)"
            + " specified in the OpenAPI specification", arraySchema.getMinItems()));

    // Act
    schemaMapperAdapter.mapGraphValue(arraySchema, false, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ThrowsException_WhenArrayBoundsUpperLimitViolated() {
    // Arrange
    arraySchema.addExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    arraySchema.setItems(new StringSchema());
    arraySchema.setMaxItems(2);

    when(ldPathExecutorMock.ldPathQuery(any(Value.class), eq(DUMMY_EXPR))).thenReturn(
        ImmutableList.of(VALUE_1, VALUE_2, VALUE_3));

    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(
        String.format("Mapping for property yielded 3 elements, which is more than 'maxItems' (%d)"
            + " specified in the OpenAPI specification", arraySchema.getMaxItems()));

    // Act
    schemaMapperAdapter.mapGraphValue(arraySchema, false, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);
  }

}
