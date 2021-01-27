package org.dotwebstack.framework.backend.rdf4j.config;

import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.datafetchers.filters.Filter;

@Data
@EqualsAndHashCode(callSuper = true)
public class Rdf4jFieldConfiguration extends AbstractFieldConfiguration {

  @Valid
  private List<JoinColumn> joinColumns;

  @Override
  public Filter createMappedByFilter(Map<String, Object> referenceData) {
    return null;
  }
}
