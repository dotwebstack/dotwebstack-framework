package org.dotwebstack.framework.core.datafetchers;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import lombok.Builder;
import org.dotwebstack.framework.core.config.TypeConfiguration;

@Builder
public class LocalDataFetcherContext {

  private final BiFunction<String, Map<String, Object>, KeyCondition> keyConditionFn;

  public KeyCondition getKeyCondition(String fieldName, TypeConfiguration<?> typeConfiguration,
      Map<String, Object> source) {
    return Optional.of(keyConditionFn.apply(fieldName, source))
        .map(kc -> {
          if (kc instanceof MappedByKeyCondition) {
            return typeConfiguration.invertKeyCondition((MappedByKeyCondition) kc, source);
          }
          return kc;
        })
        .orElseThrow(() -> illegalStateException("Unable to get keyCondition for fieldName '{}'", fieldName));
  }
}
