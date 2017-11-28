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
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LdPathSchemaMapperTest {
  @Mock
  private GraphEntityContext graphEntityContextMock;

  @Mock
  private Property propertyMock;

  private LdPathSchemaMapper ldPathSchemaMapper = new LdPathSchemaMapper() {
    // XXX (PvH) Waarom override je de method? (code kan weg)
    @Override
    Set<Resource> applySubjectFilterIfPossible(Property property,
        GraphEntityContext graphEntityContext) {
      return super.applySubjectFilterIfPossible(property, graphEntityContext);
    }
  };

  @Before
  public void before() {
    ValueFactory vf = SimpleValueFactory.getInstance();

    // XXX (PvH) Miss kan je gebruiken van de dbeerpedia test data
    Model model =
        new ModelBuilder().subject("http://www.test.nl#subj").add("http://www.test.nl#subj",
            "http://www.test.nl#is", vf.createIRI("http://www.test.nl#obj")).build();
    when(graphEntityContextMock.getModel()).thenReturn(model);
  }

  @Test
  public void when_Pred_and_Obj_exists__then_given_filter_results() {
    // Arrange
    // XXX (PvH) Maak je bewust gebruik van een LinkedHashMap? (en geen normale HashMap)
    Map<String, Object> vendorExtensions =
        Maps.newLinkedHashMap(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER,
            Maps.newLinkedHashMap(ImmutableMap.of(
                OpenApiSpecificationExtensions.SUBJECT_FILTER_PREDICATE, "http://www.test.nl#is",
                OpenApiSpecificationExtensions.SUBJECT_FILTER_OBJECT, "http://www.test.nl#obj"))));
    when(propertyMock.getVendorExtensions()).thenReturn(vendorExtensions);
    // Act
    Set<Resource> results =
        ldPathSchemaMapper.applySubjectFilterIfPossible(propertyMock, graphEntityContextMock);
    // Assert
    assertThat(results, hasSize(is(1)));
  }

  // XXX (PvH) Omdat je een Set terug geeft, mis ik een test voor meerdere results. Persoonlijk zou
  // ik niet testen op 1 resultaat, en enkel op 0 en meerdere
  @Test
  public void when_Pred_and_Obj__not_matched__then_no_results() {
    // Arrange
    Map<String, Object> vendorExtensions =
        Maps.newLinkedHashMap(ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_FILTER,
            Maps.newLinkedHashMap(ImmutableMap.of(
                OpenApiSpecificationExtensions.SUBJECT_FILTER_PREDICATE, "http://www.test.nl#is2",
                OpenApiSpecificationExtensions.SUBJECT_FILTER_OBJECT, "http://www.test.nl#obj"))));
    when(propertyMock.getVendorExtensions()).thenReturn(vendorExtensions);
    // Act
    Set<Resource> results =
        ldPathSchemaMapper.applySubjectFilterIfPossible(propertyMock, graphEntityContextMock);
    // Assert
    assertThat(results, hasSize(is(0)));
  }

}
