package org.dotwebstack.framework.core.model;

import static java.util.Optional.ofNullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import lombok.Data;
import org.dotwebstack.framework.core.config.EnumerationConfiguration;
import org.dotwebstack.framework.core.query.model.Query;

@Data
public class Schema {

  @Valid
  private Settings settings;

  @Valid
  private Map<String, ObjectType<? extends ObjectField>> objectTypes;

  @Valid
  private Map<String, Context> contexts = new HashMap<>();

  @Valid
  private Map<String, Query> queries = new HashMap<>();

  @Valid
  private Map<String, Subscription> subscriptions = new HashMap<>();

  @Valid
  private Map<String, EnumerationConfiguration> enumerations = new HashMap<>();

  public Optional<ObjectType<? extends ObjectField>> getObjectType(String name) {
    return ofNullable(objectTypes.get(name));
  }

  public Optional<Context> getContext(String contextName) {
    return ofNullable(contexts.get(contextName));
  }
}
