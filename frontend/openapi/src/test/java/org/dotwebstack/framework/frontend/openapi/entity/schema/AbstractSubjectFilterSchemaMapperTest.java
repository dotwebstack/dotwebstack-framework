package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.swagger.models.properties.Property;
import java.util.Map;
import java.util.Set;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractSubjectFilterSchemaMapperTest {

  @Mock
  private GraphEntity graphEntityMock;

  @Mock
  private Property propertyMock;

  private AbstractSubjectFilterSchemaMapper mapper = new TestSubjectFilterSchemaMapper();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void before() {
    Model model =
        new ModelBuilder().subject("http://www.test.nl#subj").add("http://www.test.nl#subj",
            "http://www.test.nl#is", "http://www.test.nl#obj").build();
    when(graphEntityMock.getModel()).thenReturn(model);
  }

  @Test
  public void filterSubjects_ReturnsNoResult_WhenFilterDoesNotMatch() {
    // Arrange
    Map<String, Object> vendorExtensions =
        Maps.newHashMap(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER,
            Maps.newHashMap(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER_PREDICATE,
                "http://www.test.nl#is", OpenApiSpecificationExtensions.SUBJECT_FILTER_OBJECT,
                "http://www.test.nl#obj3"))));
    when(propertyMock.getVendorExtensions()).thenReturn(vendorExtensions);
    // Act
    Set<Resource> results = mapper.getSubjects(propertyMock, graphEntityMock);
    // Assert
    assertThat(results, hasSize(is(0)));
  }

  @Test
  public void filterSubjects_ReturnsResults_WhenFilterDoesMatch() {
    // Arrange
    Model model = new ModelBuilder().add("http://www.test.nl#subj", "http://www.test.nl#is",
        SimpleValueFactory.getInstance().createIRI("http://www.test.nl#obj1")).add(
            "http://www.test.nl#subj2", "http://www.test.nl#is",
            SimpleValueFactory.getInstance().createIRI("http://www.test.nl#obj1")).build();
    when(graphEntityMock.getModel()).thenReturn(model);

    Map<String, Object> vendorExtensions =
        Maps.newHashMap(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER,
            Maps.newHashMap(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER_PREDICATE,
                "http://www.test.nl#is", OpenApiSpecificationExtensions.SUBJECT_FILTER_OBJECT,
                "http://www.test.nl#obj1"))));
    when(propertyMock.getVendorExtensions()).thenReturn(vendorExtensions);
    // Act
    Set<Resource> results = mapper.getSubjects(propertyMock, graphEntityMock);
    // Assert
    assertThat(results, hasSize(is(2)));
  }

  @Test
  public void filterSubjects_ThrowsException_WhenNoPredicateOrObjectHaveBeenDefined() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    // Arrange
    Map<String, Object> vendorExtensions =
        Maps.newHashMap(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER,
            Maps.newHashMap(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER_PREDICATE,
                "http://www.test.nl#is"))));
    when(propertyMock.getVendorExtensions()).thenReturn(vendorExtensions);
    // Act
    mapper.getSubjects(propertyMock, graphEntityMock);
  }

  @Test
  public void hasSubjectFilterVendorExt_ReturnsFalse_WhenPropDoesNotHaveSubjectFilterVendorExt() {
    // Arrange
    when(propertyMock.getVendorExtensions()).thenReturn(ImmutableMap.of());

    // Act
    boolean result = mapper.hasSubjectFilterVendorExtension(propertyMock);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void hasSubjectFilterVendorExt_ReturnsTrue_WhenPropDoesHaveSubjectFilterVendorExt() {
    // Arrange
    when(propertyMock.getVendorExtensions()).thenReturn(
        ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER, ImmutableMap.of()));

    // Act
    boolean result = mapper.hasSubjectFilterVendorExtension(propertyMock);

    // Assert
    assertThat(result, is(true));
  }

  private static class TestSubjectFilterSchemaMapper extends AbstractSubjectFilterSchemaMapper {

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
