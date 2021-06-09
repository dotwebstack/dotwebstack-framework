package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.core.scalars.DateSupplier;
import org.junit.jupiter.api.Test;

class MapHelperTest {

  @Test
  void getNestedMap_returnsValue_forSupplier() {
    LocalDate date = LocalDate.of(2020, 1, 1);
    Map<String, Object> data = new HashMap<>();
    data.put("key", new DateSupplier(false, date));
    Map<String, Object> arguments = Map.of("arg", data);

    Map<String, Object> result = getNestedMap(arguments, "arg");

    assertThat(result, is(Map.of("key", date)));
  }

  @Test
  void getNestedMap_returnsValue_forNestedSupplier() {
    LocalDate date = LocalDate.of(2020, 1, 1);
    Map<String, Object> data = new HashMap<>();
    data.put("key", new DateSupplier(false, date));
    Map<String, Object> arguments = Map.of("arg", Map.of("arg1", data));

    Map<String, Object> result = getNestedMap(arguments, "arg");

    assertThat(result, is(Map.of("arg1", Map.of("key", date))));
  }
}
