package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.Property;
import java.util.Map;
import java.util.Set;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.Rdf4jUtils;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractSubjectQuerySchemaMapperTest {

  @Mock
  private GraphEntity entityMock;

  @Mock
  private Property propertyMock;

  private AbstractSubjectQuerySchemaMapper mapper = new TestSubjectQuerySchemaMapper();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void before() {
    Model model = new ModelBuilder().subject(DBEERPEDIA.BROUWTOREN).add(RDF.TYPE,
        DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.FTE, "42").subject(DBEERPEDIA.MAXIMUS).add(RDF.TYPE,
            DBEERPEDIA.BREWERY_TYPE).build();

    when(entityMock.getRepository()).thenReturn(Rdf4jUtils.asRepository(model));
  }

  @Test
  public void getSubjects_ReturnsNoResults_ForSparqlQueryWithNoResults() {
    // Arrange
    Map<String, Object> vendorExtensions =
        ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_QUERY,
            String.format("SELECT ?s WHERE { ?s <%s> <%s>}", RDF.TYPE, "foo://bar"));
    when(propertyMock.getVendorExtensions()).thenReturn(vendorExtensions);

    // Act
    Set<Resource> results = mapper.getSubjects(propertyMock, entityMock);

    // Assert
    assertThat(results, hasSize(is(0)));
  }

  @Test
  public void getSubjects_ReturnsResults_ForSparqlQueryWithResults() {
    // Arrange
    Map<String, Object> vendorExtensions =
        ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_QUERY,
            String.format("SELECT ?s WHERE { ?s <%s> <%s>}", RDF.TYPE, DBEERPEDIA.BREWERY_TYPE));
    when(propertyMock.getVendorExtensions()).thenReturn(vendorExtensions);

    // Act
    Set<Resource> results = mapper.getSubjects(propertyMock, entityMock);

    // Assert
    assertThat(results, hasSize(is(2)));
  }

  @Test
  public void getSubjects_ThrowsException_WhenSparqlQueryHasMultipleBindingsDefined() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("Query must define exactly 1 binding");

    // Arrange
    Map<String, Object> vendorExtensions =
        ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_QUERY,
            String.format("SELECT ?s ?p WHERE { ?s ?p <%s>}", DBEERPEDIA.BREWERY_TYPE));
    when(propertyMock.getVendorExtensions()).thenReturn(vendorExtensions);

    // Act
    mapper.getSubjects(propertyMock, entityMock);
  }

  @Test
  public void getSubjects_ThrowsException_WhenSparqlQueryReturnsNonResource() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("Query must return RDF resources (IRIs and blank nodes) only");

    // Arrange
    Map<String, Object> vendorExtensions =
        ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_QUERY,
            String.format("SELECT ?o WHERE { ?s <%s> ?o}", DBEERPEDIA.FTE));
    when(propertyMock.getVendorExtensions()).thenReturn(vendorExtensions);

    // Act
    mapper.getSubjects(propertyMock, entityMock);
  }

  @Test
  public void hasSubjectQueryVendorExt_ReturnsFalse_WhenPropDoesNotHaveSubjectQueryVendorExt() {
    // Arrange
    when(propertyMock.getVendorExtensions()).thenReturn(ImmutableMap.of());

    // Act
    boolean result = mapper.hasSubjectQueryVendorExtension(propertyMock);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void hasSubjectQueryVendorExt_ReturnsTrue_WhenPropDoesHaveSubjectQueryVendorExt() {
    // Arrange
    when(propertyMock.getVendorExtensions()).thenReturn(
        ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_QUERY, "queryString"));

    // Act
    boolean result = mapper.hasSubjectQueryVendorExtension(propertyMock);

    // Assert
    assertThat(result, is(true));
  }

  private static class TestSubjectQuerySchemaMapper extends AbstractSubjectQuerySchemaMapper {

    @Override
    public Object mapTupleValue(Property schema, ValueContext valueContext) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object mapGraphValue(Property schema, GraphEntity entityContext,
        ValueContext valueContext, SchemaMapperAdapter schemaMapperAdapter) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean supports(Property schema) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected Set getSupportedDataTypes() {
      throw new UnsupportedOperationException();
    }

  }

}
