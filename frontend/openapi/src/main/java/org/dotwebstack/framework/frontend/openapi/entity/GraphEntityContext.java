package org.dotwebstack.framework.frontend.openapi.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.swagger.models.Model;
import java.util.Map;

public class GraphEntityContext implements EntityContext {

  private final Map<String, Model> swaggerDefinitions;
  private final ImmutableMap<String, String> ldPathNamespaces;
  private final org.eclipse.rdf4j.model.Model model;
  private final LdPathExecutor ldPathExecutor;

  public GraphEntityContext(ImmutableMap<String, String> ldPathNamespaces,
      Map<String, Model> swaggerDefinitions, org.eclipse.rdf4j.model.Model model) {
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
