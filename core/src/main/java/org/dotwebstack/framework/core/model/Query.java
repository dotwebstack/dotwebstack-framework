package org.dotwebstack.framework.core.model;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Query {

  @NotBlank
  private String type;

  private List<Object> keys = new ArrayList<>();

  private boolean list = false;

  private boolean pageable = false;

  private boolean batch = false;

  private String context;

  public Map<String, String> getKeyMap(){
    return keys.stream()
        .map(key -> {
          if (key instanceof String){
            return new AbstractMap.SimpleEntry<>((String) key, (String)key);
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
