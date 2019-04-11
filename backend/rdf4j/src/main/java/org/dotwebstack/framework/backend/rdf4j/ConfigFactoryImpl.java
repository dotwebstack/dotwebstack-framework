package org.dotwebstack.framework.backend.rdf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.NonNull;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfig;
import org.eclipse.rdf4j.repository.sparql.config.SPARQLRepositoryConfig;
import org.springframework.stereotype.Component;

@Component
final class ConfigFactoryImpl implements ConfigFactory {

  private static final String SPARQL_REPOSITORY_TYPE = "sparql";

  private static final String SPARQL_REPOSITORY_ARG_ENDPOINT_URL = "endpointUrl";

  private final HashMap<String, Function<Map<String, Object>, RepositoryImplConfig>> creators;

  ConfigFactoryImpl() {
    creators = new HashMap<>();

    registerRepositoryType(SPARQL_REPOSITORY_TYPE, args -> {
      String endpointUrl = (String) args.get(SPARQL_REPOSITORY_ARG_ENDPOINT_URL);
      return new SPARQLRepositoryConfig(endpointUrl);
    });
  }

  @Override
  public RepositoryImplConfig create(@NonNull String type, @NonNull Map<String, Object> args) {
    return creators.get(type).apply(args);
  }

  @Override
  public void registerRepositoryType(@NonNull String type,
      @NonNull Function<Map<String, Object>, RepositoryImplConfig> creator) {
    creators.put(type, creator);
  }

}
