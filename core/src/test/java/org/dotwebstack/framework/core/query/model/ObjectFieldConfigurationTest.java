package org.dotwebstack.framework.core.query.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Set;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.query.model.filter.EqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.origin.Origin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ObjectFieldConfigurationTest {

  @Test
  void hasNestedFilteringOrigin_returnsTrue_forOneLevelDeepNestedFilteringOrigin() {
    var filterCriteria = EqualsFilterCriteria.builder()
        .build();
    var scalarField = ScalarField.builder()
        .origins(Set.of(Origin.filtering(filterCriteria)))
        .build();
    var objectRequest = ObjectRequest.builder()
        .scalarFields(List.of(scalarField))
        .typeConfiguration(mock(TypeConfiguration.class))
        .build();
    var objectFieldConfiguration = ObjectFieldConfiguration.builder()
        .objectRequest(objectRequest)
        .build();

    boolean result = objectFieldConfiguration.hasNestedFilteringOrigin();

    assertThat(result, is(true));
  }

  @Test
  void hasNestedFilteringOrigin_returnsTrue_forTwoLevelDeepNestedFilteringOrigin() {
    var filterCriteria = EqualsFilterCriteria.builder()
        .build();
    var nestedScalarField = ScalarField.builder()
        .origins(Set.of(Origin.filtering(filterCriteria)))
        .build();
    var nestedObjectRequest = ObjectRequest.builder()
        .scalarFields(List.of(nestedScalarField))
        .typeConfiguration(mock(TypeConfiguration.class))
        .build();
    var nestedObjectField = ObjectFieldConfiguration.builder()
        .objectRequest(nestedObjectRequest)
        .build();

    var scalarField = ScalarField.builder()
        .build();
    var objectRequest = ObjectRequest.builder()
        .scalarFields(List.of(scalarField))
        .objectFields(List.of(nestedObjectField))
        .typeConfiguration(mock(TypeConfiguration.class))
        .build();
    var objectFieldConfiguration = ObjectFieldConfiguration.builder()
        .objectRequest(objectRequest)
        .build();

    boolean result = objectFieldConfiguration.hasNestedFilteringOrigin();

    assertThat(result, is(true));
  }

  @Test
  void hasNestedFilteringOrigin_returnsFalse_forNoneNestedFilteringOrigin() {
    var scalarField = ScalarField.builder()
        .build();
    var objectRequest = ObjectRequest.builder()
        .scalarFields(List.of(scalarField))
        .typeConfiguration(mock(TypeConfiguration.class))
        .build();
    var objectFieldConfiguration = ObjectFieldConfiguration.builder()
        .objectRequest(objectRequest)
        .build();

    boolean result = objectFieldConfiguration.hasNestedFilteringOrigin();

    assertThat(result, is(false));
  }
}
