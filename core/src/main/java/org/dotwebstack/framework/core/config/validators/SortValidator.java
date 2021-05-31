package org.dotwebstack.framework.core.config.validators;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.SortableByConfiguration;
import org.springframework.stereotype.Component;

@Component
public class SortValidator implements DotWebStackConfigurationValidator {

  @Override
  public void validate(DotWebStackConfiguration dotWebStackConfiguration) {
    Map<String, List<SortableByConfiguration>> sortableByPerObject = dotWebStackConfiguration.getObjectTypes()
        .entrySet()
        .stream()
        .filter(entry -> !entry.getValue()
            .getSortableBy()
            .isEmpty())
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()
            .getSortableBy()
            .values()
            .stream()
            .flatMap(List::stream)
            .collect(Collectors.toList())));

    sortableByPerObject.forEach((key, value) -> value.forEach(sortableByConfiguration -> {
      var sortableByFieldName = sortableByConfiguration.getField();

      if (!sortableByFieldName.contains(".")) {
        var fields = getFields(dotWebStackConfiguration, key);

        if (!fields.containsKey(sortableByFieldName)) {
          throw new InvalidConfigurationException(
              String.format("Field %s does not exist in type %s", sortableByFieldName, key));
        }
      }
    }));


    System.out.println();
  }

  private Map<String, ?> getFields(DotWebStackConfiguration dotWebStackConfiguration, String key) {
    return dotWebStackConfiguration.getObjectTypes()
        .get(key)
        .getFields();
  }
}
