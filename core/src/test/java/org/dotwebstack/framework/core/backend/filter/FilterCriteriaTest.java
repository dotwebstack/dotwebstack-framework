package org.dotwebstack.framework.core.backend.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.model.ObjectField;
import org.junit.jupiter.api.Test;

class FilterCriteriaTest {

  @Test
  void initScalarFieldFilterCriteriaObject() {
    List<ObjectField> fieldPath = new ArrayList<>();
    fieldPath.add(mock(ObjectField.class));
    Map<String, Object> value = new HashMap<>();
    value.put("a", "bbb");

    var result = ScalarFieldFilterCriteria.builder()
        .fieldPath(fieldPath)
        .value(value)
        .build();

    assertThat(result.getFieldPath(), is(fieldPath));
    assertThat(result.getValue(), is(value));
  }
}
