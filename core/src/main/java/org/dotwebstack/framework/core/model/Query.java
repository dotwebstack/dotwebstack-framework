package org.dotwebstack.framework.core.model;

import static org.dotwebstack.framework.core.helpers.FieldPathHelper.getFieldKey;

import jakarta.validation.constraints.NotBlank;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import org.dotwebstack.framework.core.config.SortableByConfiguration;

@Data
public class Query {

  @NotBlank
  private String type;

  private List<Object> keys = new ArrayList<>();

  private boolean list = false;

  private boolean pageable = false;

  private boolean batch = false;

  private String context;

  private Map<String, List<SortableByConfiguration>> sortableBy = new HashMap<>();

  public Map<String, String> getKeyMap() {
    return keys.stream()
        .map(key -> {
          if (key instanceof String stringKey) {
            return new AbstractMap.SimpleEntry<>(getFieldKey(stringKey), (String) key);
          }

          return ((HashMap<String, String>) key).entrySet()
              .stream()
              .map(mapEntry -> new AbstractMap.SimpleEntry<>(mapEntry.getKey(), mapEntry.getValue()))
              .findFirst()
              .orElseThrow();

        })
        .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
  }
}
