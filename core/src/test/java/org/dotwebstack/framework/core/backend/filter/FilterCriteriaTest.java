package org.dotwebstack.framework.core.backend.filter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.config.FilterType;
import org.junit.jupiter.api.Test;

class FilterCriteriaTest {

  @Test
  void isGroupFilter_returnsTrue_forGroupFilter() {
    var filterCriteria = GroupFilterCriteria.builder()
        .logicalOperator(GroupFilterOperator.AND)
        .filterCriterias(List.of())
        .build();

    var result = filterCriteria.isGroupFilter();

    assertThat(result, equalTo(true));
  }

  @Test
  void isGroupFilter_returnsFalse_forNonGroupFilter() {
    var filterCriteria = mock(FilterCriteria.class);

    var result = filterCriteria.isGroupFilter();

    assertThat(result, equalTo(false));
  }

  @Test
  void asGroupFilter_returnsGroupFilter_forGroupFilter() {
    var filterCriteria = (FilterCriteria) GroupFilterCriteria.builder()
        .logicalOperator(GroupFilterOperator.AND)
        .filterCriterias(List.of())
        .build();

    var result = filterCriteria.asGroupFilter();

    assertThat(result, instanceOf(GroupFilterCriteria.class));
  }

  @Test
  void asGroupFilter_throwsException_forNonGroupFilter() {
    var filterCriteria = (FilterCriteria) ObjectFieldFilterCriteria.builder()
        .filterType(FilterType.EXACT)
        .fieldPath(List.of())
        .value(Map.of())
        .build();

    var thrown = assertThrows(IllegalArgumentException.class, filterCriteria::asGroupFilter);

    assertThat(thrown.getMessage(), equalTo("Not a group filter!"));
  }

  @Test
  void isScalarFieldFilter_returnsTrue_forScalarFieldFilter() {
    var filterCriteria = ObjectFieldFilterCriteria.builder()
        .filterType(FilterType.EXACT)
        .fieldPath(List.of())
        .value(Map.of())
        .build();

    var result = filterCriteria.isObjectFieldFilter();

    assertThat(result, equalTo(true));
  }

  @Test
  void isScalarFieldFilter_returnsFalse_forNonScalarFieldFilter() {
    var filterCriteria = mock(FilterCriteria.class);

    var result = filterCriteria.isObjectFieldFilter();

    assertThat(result, equalTo(false));
  }

  @Test
  void asScalarFieldFilter_returnsScalarFieldFilter_forScalarFieldFilter() {
    var filterCriteria = (FilterCriteria) ObjectFieldFilterCriteria.builder()
        .filterType(FilterType.EXACT)
        .fieldPath(List.of())
        .value(Map.of())
        .build();

    var result = filterCriteria.asObjectFieldFilter();

    assertThat(result, instanceOf(ObjectFieldFilterCriteria.class));
  }

  @Test
  void asScalarFieldFilter_throwsException_forNonScalarFieldFilter() {
    var filterCriteria = (FilterCriteria) GroupFilterCriteria.builder()
        .logicalOperator(GroupFilterOperator.AND)
        .filterCriterias(List.of())
        .build();

    var thrown = assertThrows(IllegalArgumentException.class, filterCriteria::asObjectFieldFilter);

    assertThat(thrown.getMessage(), equalTo("Not a scalar field filter!"));
  }

}
