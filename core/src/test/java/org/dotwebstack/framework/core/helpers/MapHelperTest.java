package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.MapHelper.copyAndProcessSuppliers;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;
import java.util.Map;
import org.dotwebstack.framework.core.scalars.DateSupplier;
import org.junit.jupiter.api.Test;

class MapHelperTest {

  @Test
  void copyAndProcessSuppliers_returnsNewMap_default() {
    LocalDate date = LocalDate.of(2020, 1, 1);
    Map<String, Object> data = Map.of("key", new DateSupplier(false, date));

    Map<String, Object> result = copyAndProcessSuppliers(data);

    assertThat(result, is(Map.of("key", date)));
  }

  @Test
  void copyAndProcessSuppliers_returnsNewMap_nestedMap() {
    LocalDate date = LocalDate.of(2020, 1, 1);
    Map<String, Object> data = Map.of("key", Map.of("key1", new DateSupplier(false, date)));

    Map<String, Object> result = copyAndProcessSuppliers(data);

    assertThat(result, is(Map.of("key", Map.of("key1", date))));
  }
}
