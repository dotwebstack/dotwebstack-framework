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
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
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
public class AbstractLdPathSchemaMapperTest {

  @Mock
  private GraphEntityContext graphEntityContextMock;

  @Mock
  private Property propertyMock;

  private AbstractLdPathSchemaMapper ldPathSchemaMapper = new TestLdPathSchemaMapper();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void before() {
    Model model =
        new ModelBuilder().subject("http://www.test.nl#subj").add("http://www.test.nl#subj",
            "http://www.test.nl#is", "http://www.test.nl#obj").build();
    when(graphEntityContextMock.getModel()).thenReturn(model);
  }

  @Test
  public void when_No_Obj_exists__then_no_filter_results() {
    // Arrange
    Map<String, Object> vendorExtensions =
        Maps.newHashMap(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER,
            Maps.newHashMap(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER_PREDICATE,
                "http://www.test.nl#is", OpenApiSpecificationExtensions.SUBJECT_FILTER_OBJECT,
                "http://www.test.nl#obj3"))));
    when(propertyMock.getVendorExtensions()).thenReturn(vendorExtensions);
    // Act
    Set<Resource> results =
        ldPathSchemaMapper.applySubjectFilterIfPossible(propertyMock, graphEntityContextMock);
    // Assert
    assertThat(results, hasSize(is(0)));
  }

  @Test
  public void when_Pred_and_Obj__not_matched__then_more_results() {
    // Arrange
    Model model = new ModelBuilder().add("http://www.test.nl#subj", "http://www.test.nl#is",
        SimpleValueFactory.getInstance().createIRI("http://www.test.nl#obj1")).add(
            "http://www.test.nl#subj2", "http://www.test.nl#is",
            SimpleValueFactory.getInstance().createIRI("http://www.test.nl#obj1")).build();
    when(graphEntityContextMock.getModel()).thenReturn(model);

    Map<String, Object> vendorExtensions =
        Maps.newHashMap(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER,
            Maps.newHashMap(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER_PREDICATE,
                "http://www.test.nl#is", OpenApiSpecificationExtensions.SUBJECT_FILTER_OBJECT,
                "http://www.test.nl#obj1"))));
    when(propertyMock.getVendorExtensions()).thenReturn(vendorExtensions);
    // Act
    Set<Resource> results =
        ldPathSchemaMapper.applySubjectFilterIfPossible(propertyMock, graphEntityContextMock);
    // Assert
    assertThat(results, hasSize(is(2)));
  }

  @Test
  public void when_Pred_and_IRIObj__matched__then_one_results() {
    // Arrange
    Model model =
        new ModelBuilder().add("http://www.test.nl#subj", "http://www.test.nl#is", "obj1").add(
            "http://www.test.nl#subj2", "http://www.test.nl#is",
            SimpleValueFactory.getInstance().createIRI("http://www.test.nl#obj1")).build();
    when(graphEntityContextMock.getModel()).thenReturn(model);

    Map<String, Object> vendorExtensions =
        Maps.newHashMap(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER,
            Maps.newHashMap(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER_PREDICATE,
                "http://www.test.nl#is", OpenApiSpecificationExtensions.SUBJECT_FILTER_OBJECT,
                "http://www.test.nl#obj1"))));
    when(propertyMock.getVendorExtensions()).thenReturn(vendorExtensions);
    // Act
    Set<Resource> results =
        ldPathSchemaMapper.applySubjectFilterIfPossible(propertyMock, graphEntityContextMock);
    // Assert
    assertThat(results, hasSize(is(1)));
  }


  @Test
  public void when_No_Object_Or_Predicate_Specified_Then_Exception() {
    // Arrange
    Map<String, Object> vendorExtensions =
        Maps.newHashMap(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER,
            Maps.newHashMap(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER_PREDICATE,
                "http://www.test.nl#is"))));
    when(propertyMock.getVendorExtensions()).thenReturn(vendorExtensions);
    thrown.expect(SchemaMapperRuntimeException.class);
    // Act
    ldPathSchemaMapper.applySubjectFilterIfPossible(propertyMock, graphEntityContextMock);
  }

  private static class TestLdPathSchemaMapper extends AbstractLdPathSchemaMapper {

    @Override
    public Object mapTupleValue(Property schema, Value value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object mapGraphValue(Property schema, GraphEntityContext entityContext,
        SchemaMapperAdapter schemaMapperAdapter, Value value) {
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
