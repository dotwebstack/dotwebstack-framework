package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.swagger.models.Response;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.StringProperty;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.Rdf4jUtils;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.hamcrest.Matchers.instanceOf;
import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class ResponseSchemaMapperTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Captor
  private ArgumentCaptor<ValueContext> valueContextCaptor;

  @Mock
  private GraphEntity graphEntityMock;
  @Mock
  private LdPathExecutor ldPathExecutorMock;

  private SchemaMapperAdapter schemaMapperAdapter;
  private ResponseSchemaMapper responseSchemaMapper;
  private ObjectProperty objectProperty;
  private Response response;

  @Before
  public void setUp() {
    objectProperty = new ObjectProperty();
    response = new Response().schema(objectProperty);

    responseSchemaMapper = new ResponseSchemaMapper();

    schemaMapperAdapter = new SchemaMapperAdapter(
        Arrays.asList(new StringSchemaMapper(), responseSchemaMapper, new ObjectSchemaMapper()));

    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);
  }

  @Test
  public void mapGraphValue_DoesNotSwitchContext_WhenNoSubjectQueryHasBeenDefined() {
    // Arrange
    objectProperty.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_QUERY,
        String.format("SELECT ?s WHERE { ?s <%s> <%s>}", RDF.TYPE, DBEERPEDIA.BREWERY_TYPE),
        OpenApiSpecificationExtensions.LDPATH, DBEERPEDIA.NAME.stringValue()));
    objectProperty.setProperties(
        ImmutableMap.of(DBEERPEDIA.NAME.stringValue(), new StringProperty()));

    Model model = new ModelBuilder().subject(DBEERPEDIA.BROUWTOREN).add(RDF.TYPE,
        DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME, DBEERPEDIA.BROUWTOREN_NAME).build();
    when(graphEntityMock.getRepository()).thenReturn(Rdf4jUtils.asRepository(model));

    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.BROUWTOREN,
        DBEERPEDIA.NAME.stringValue())).thenReturn(ImmutableSet.of(DBEERPEDIA.BROUWTOREN_NAME));

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(new ResponseProperty(response),
        graphEntityMock, ValueContext.builder().value(null).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, instanceOf(Map.class));

    Map map = (Map) result;

    assertThat(map, is(ImmutableMap.of(DBEERPEDIA.NAME.stringValue(),
        Optional.of(DBEERPEDIA.BROUWTOREN_NAME.stringValue()))));
  }

  @Test
  public void mapGraphValue_DoesNotSwitchContext_WhenSubjectExtIsNotEnabled() {
    // Arrange
    response.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_QUERY,
        String.format("SELECT ?s WHERE { ?s <%s> <%s>}", RDF.TYPE, DBEERPEDIA.BREWERY_TYPE)));

    objectProperty.setVendorExtensions(
        ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH, DBEERPEDIA.NAME.stringValue()));
    objectProperty.setProperties(
        ImmutableMap.of(DBEERPEDIA.NAME.stringValue(), new StringProperty()));

    Model model = new ModelBuilder().subject(DBEERPEDIA.BROUWTOREN).add(RDF.TYPE,
        DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME, DBEERPEDIA.BROUWTOREN_NAME).build();
    when(graphEntityMock.getRepository()).thenReturn(Rdf4jUtils.asRepository(model));

    when(ldPathExecutorMock.ldPathQuery(DBEERPEDIA.BROUWTOREN,
        DBEERPEDIA.NAME.stringValue())).thenReturn(ImmutableSet.of(DBEERPEDIA.BROUWTOREN_NAME));

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(new ResponseProperty(response),
        graphEntityMock, ValueContext.builder().value(null).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, instanceOf(Map.class));

    Map map = (Map) result;

    assertThat(map, is(ImmutableMap.of(DBEERPEDIA.NAME.stringValue(),
        Optional.of(DBEERPEDIA.BROUWTOREN_NAME.stringValue()))));
  }

  @Test
  public void mapGraphValue_ReturnsNull_WhenSubjectQueryYieldsNoResultAndPropertyIsOptional() {
    // Arrange
    response.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_QUERY,
        String.format("SELECT ?s WHERE { ?s <%s> <%s>}", RDF.TYPE, DBEERPEDIA.BREWERY_TYPE)));

    objectProperty.setVendorExtensions(
        ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH, DBEERPEDIA.NAME.stringValue()));
    objectProperty.setProperties(
        ImmutableMap.of(DBEERPEDIA.NAME.stringValue(), new StringProperty()));

    Model model = new ModelBuilder().build();
    when(graphEntityMock.getRepository()).thenReturn(Rdf4jUtils.asRepository(model));

    // Act
    Object result = schemaMapperAdapter.mapGraphValue(new ResponseProperty(response),
        graphEntityMock, ValueContext.builder().value(null).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, nullValue());
  }

  @Test
  public void mapGraphValue_ThrowsException_WhenSubjectQueryYieldsNoResultAndPropertyIsRequired() {
    // Assert
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage(
        "Subject query for a required object property yielded no result");

    // Arrange
    response.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_QUERY,
        String.format("SELECT ?s WHERE { ?s <%s> <%s>}", RDF.TYPE, DBEERPEDIA.BREWERY_TYPE)));

    objectProperty.setVendorExtensions(
        ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH, DBEERPEDIA.NAME.stringValue()));
    objectProperty.setProperties(
        ImmutableMap.of(DBEERPEDIA.NAME.stringValue(), new StringProperty()));
    objectProperty.setRequired(true);

    Model model = new ModelBuilder().build();
    when(graphEntityMock.getRepository()).thenReturn(Rdf4jUtils.asRepository(model));

    // Act
    schemaMapperAdapter.mapGraphValue(new ResponseProperty(response), graphEntityMock,
        ValueContext.builder().value(null).build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ThrowsException_WhenSubjectQueryYieldsMultipleResults() {
    // Assert
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage("More entrypoint subjects found. Only one is required");

    // Arrange
    response.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_QUERY,
        String.format("SELECT ?s WHERE { ?s <%s> <%s>}", RDF.TYPE, DBEERPEDIA.BREWERY_TYPE)));

    objectProperty.setVendorExtensions(
        ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH, DBEERPEDIA.NAME.stringValue()));
    objectProperty.setProperties(
        ImmutableMap.of(DBEERPEDIA.NAME.stringValue(), new StringProperty()));
    objectProperty.setRequired(true);

    Model model = new ModelBuilder().subject(DBEERPEDIA.BROUWTOREN).add(RDF.TYPE,
        DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME, DBEERPEDIA.BROUWTOREN_NAME).subject(
            DBEERPEDIA.MAXIMUS).add(RDF.TYPE, DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME,
                DBEERPEDIA.MAXIMUS_NAME).build();
    when(graphEntityMock.getRepository()).thenReturn(Rdf4jUtils.asRepository(model));

    // Act
    schemaMapperAdapter.mapGraphValue(new ResponseProperty(response), graphEntityMock,
        ValueContext.builder().value(null).build(), schemaMapperAdapter);
  }

  @Test
  public void support_ReturnsTrue_ForResponseProperty() {
    // Act
    boolean result = responseSchemaMapper.supports(new ResponseProperty(response));

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void support_ReturnsFalse_ForNonResponseProperty() {
    // Act
    boolean result = responseSchemaMapper.supports(new ArrayProperty());

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void getSupportedDataTypes_ReturnsEmptySet() {
    // Act
    Set<IRI> result = responseSchemaMapper.getSupportedDataTypes();

    // Assert
    assertThat(result, empty());
  }

}
