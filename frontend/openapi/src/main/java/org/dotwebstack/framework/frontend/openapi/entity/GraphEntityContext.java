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
  private final Map<String, String> requestParameters;
  private final Map<String, String> responseParameters;

  public GraphEntityContext(@NonNull ImmutableMap<String, String> ldPathNamespaces,
      @NonNull Map<String, Model> swaggerDefinitions, @NonNull org.eclipse.rdf4j.model.Model model,
      @NonNull Map<String, String> requestParameters,
      @NonNull Map<String, String> responseParameters) {
    this.responseParameters = responseParameters;
    this.requestParameters = requestParameters;
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

  public Map<String, String> getRequestParameters() {
    return requestParameters;
  }

  public Map<String, String> getResponseParameters() {
    return responseParameters;
  }



}
