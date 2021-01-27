package org.dotwebstack.framework.backend.postgres.config;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.datafetchers.filters.FieldFilter;
import org.dotwebstack.framework.core.datafetchers.filters.Filter;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostgresFieldConfiguration extends AbstractFieldConfiguration {

  @Valid
  private List<JoinColumn> joinColumns;

  @Valid
  private JoinTable joinTable;

  private String sqlColumnName;

  @Override
  public Filter createMappedByFilter(Map<String, Object> referenceData) {
    if (joinColumns.size() == 0) {
      throw invalidConfigurationException("Unable to create mappedBy Filter without joinColumns");
    }

    List<Filter> filters = joinColumns.stream()
        .map(joinColumn -> createFieldFilter(joinColumn, referenceData))
        .collect(Collectors.toList());

    return Filter.wrap(filters);
  }

  private FieldFilter createFieldFilter(JoinColumn joinColumn, Map<String, Object> referenceData) {
    if (!referenceData.containsKey(joinColumn.getReferencedField())) {
      throw illegalArgumentException("Reference field '{}' for joinColumn '{}' not found in reference data",
          joinColumn.getReferencedField(), joinColumn.getName());
    }
    Object keyValue = referenceData.get(joinColumn.getReferencedField());

    return FieldFilter.builder()
        .field(joinColumn.getName())
        .value(keyValue)
        .build();
  }
}
