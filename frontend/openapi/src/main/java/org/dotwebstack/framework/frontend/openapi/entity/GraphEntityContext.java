package org.dotwebstack.framework.frontend.openapi.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.swagger.models.Model;
import java.util.Map;
import lombok.NonNull;

public class GraphEntityContext {

  private final Map<String, Model> swaggerDefinitions;
  private final ImmutableMap<String, String> ldPathNamespaces;
  private final org.eclipse.rdf4j.model.Model model;
  private final LdPathExecutor ldPathExecutor;

  public GraphEntityContext(@NonNull ImmutableMap<String, String> ldPathNamespaces,
      @NonNull Map<String, Model> swaggerDefinitions,
      @NonNull org.eclipse.rdf4j.model.Model model) {
    this.ldPathNamespaces = ldPathNamespaces;
    this.swaggerDefinitions = Maps.newHashMap(swaggerDefinitions);
    this.model = model;
    this.ldPathExecutor = new LdPathExecutor(this);
  }

  public LdPathExecutor getLdPathExecutor() {
    return this.ldPathExecutor;
  }

  public org.eclipse.rdf4j.model.Model getModel() {
    return model;
  }

  public ImmutableMap<String, String> getLdPathNamespaces() {
    return ldPathNamespaces;
  }

  public Map<String, Model> getSwaggerDefinitions() {
    return swaggerDefinitions;
  }

}
