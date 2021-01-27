package org.dotwebstack.framework.backend.json.config;

import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.datafetchers.filters.Filter;

@Data
@EqualsAndHashCode(callSuper = true)
public class JsonFieldConfiguration extends AbstractFieldConfiguration {

  @Override
  public Filter createMappedByFilter(Map<String, Object> referenceData) {
    return null;
  }
}
