package org.dotwebstack.framework.core.backend.filter;

import static org.dotwebstack.framework.core.backend.filter.FilterCriteriaBuilder.newFilterCriteriaBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.config.FilterType;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.testhelpers.TestObjectField;
import org.dotwebstack.framework.core.testhelpers.TestObjectType;
import org.junit.jupiter.api.Test;

class FilterCriteriaBuilderTest {

  @Test
  public void build_throwsException_forMaxDepthIsReached() {
    var objectType = new TestObjectType();

    var objectField = new TestObjectField();
    objectField.setName("node");

    List<ObjectField> fieldPath = List.of(objectField);

    Map<String, Object> arguments = Map.of();

    var throwed = assertThrows(UnsupportedOperationException.class,
        () -> newFilterCriteriaBuilder().objectType(objectType)
            .fieldPath(fieldPath)
            .argument(arguments)
            .currentDepth(1)
            .build());

    assertThat(throwed.getMessage(), equalTo("Max depth of '0' is exceeded for filter path 'node'"));
  }

  @Test
  public void build_throwsException_forExistsFilterOnTopLevel() {
    var objectType = new TestObjectType();

    List<ObjectField> fieldPath = List.of();

    Map<String, Object> arguments = Map.of("_exists", true);

    var throwed = assertThrows(UnsupportedOperationException.class,
        () -> newFilterCriteriaBuilder().objectType(objectType)
            .fieldPath(fieldPath)
            .argument(arguments)
            .build());

    assertThat(throwed.getMessage(), equalTo("Filter operator '_exists' is only supported for nested objects"));
  }

  @Test
  public void build_returnsFilterCriteria_forExistsFilter() {
    var objectType = new TestObjectType();

    var objectField = new TestObjectField();
    objectField.setName("node");

    List<ObjectField> fieldPath = List.of(objectField);

    Map<String, Object> arguments = Map.of("_exists", true);

    var filterCriteria = newFilterCriteriaBuilder().objectType(objectType)
        .fieldPath(fieldPath)
        .argument(arguments)
        .build();

    assertThat(filterCriteria, equalTo(GroupFilterCriteria.builder()
        .logicalOperator(GroupFilterOperator.AND)
        .filterCriterias(List.of(ScalarFieldFilterCriteria.builder()
            .filterType(FilterType.EXACT)
            .fieldPath(fieldPath)
            .value(arguments)
            .build()))
        .build()));
  }

  @Test
  public void build_returnsFilterCriteria_forNestedFilter() {
    var childType = new TestObjectType();

    var parentType = new TestObjectType();

    var objectField = new TestObjectField();
    objectField.setName("child");
    objectField.setTargetType(childType);
    parentType.setFields(Map.of("child", objectField));

    var filterConfiguration = new FilterConfiguration();
    filterConfiguration.setField("child");
    parentType.setFilters(Map.of("childfilter", filterConfiguration));

    Map<String, Object> arguments = Map.of("childfilter", Map.of("_exists", true));

    var filterCriteria = newFilterCriteriaBuilder().objectType(parentType)
        .argument(arguments)
        .maxDepth(1)
        .build();

    assertThat(filterCriteria, equalTo(GroupFilterCriteria.builder()
        .logicalOperator(GroupFilterOperator.AND)
        .filterCriterias(List.of(GroupFilterCriteria.builder()
            .logicalOperator(GroupFilterOperator.AND)
            .filterCriterias(List.of(ScalarFieldFilterCriteria.builder()
                .filterType(FilterType.EXACT)
                .fieldPath(List.of(objectField))
                .value(Map.of("_exists", true))
                .build()))
            .build()))
        .build()));

  }

}
