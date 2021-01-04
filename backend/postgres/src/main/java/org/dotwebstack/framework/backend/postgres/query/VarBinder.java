package org.dotwebstack.framework.backend.postgres.query;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;

public final class VarBinder {

  private static final Logger LOG = LoggerFactory.getLogger(VarBinder.class);

  private static final String VAR_PREFIX = "$";

  private final AtomicInteger varCounter = new AtomicInteger();

  private final ImmutableMap.Builder<String, Object> bindingMapBuilder = ImmutableMap.builder();

  public String register(Object value) {
    String varName = newVarName();
    bindingMapBuilder.put(varName, value);

    return varName;
  }

  public DatabaseClient.GenericExecuteSpec bind(DatabaseClient.GenericExecuteSpec executeSpec) {
    ImmutableMap<String, Object> bindingMap = bindingMapBuilder.build();
    LOG.debug("Binding variables: {}", bindingMap);

    for (Map.Entry<String, Object> binding : bindingMap.entrySet()) {
      executeSpec = executeSpec.bind(binding.getKey(), binding.getValue());
    }

    return executeSpec;
  }

  private String newVarName() {
    return VAR_PREFIX.concat(String.valueOf(varCounter.incrementAndGet()));
  }
}
