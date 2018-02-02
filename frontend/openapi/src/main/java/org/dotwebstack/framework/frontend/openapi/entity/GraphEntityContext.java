package org.dotwebstack.framework.frontend.openapi.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.swagger.models.Model;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.informationproduct.InformationProduct;

public class GraphEntityContext {

  private final Map<String, Model> swaggerDefinitions;
  private final ImmutableMap<String, String> ldPathNamespaces;
  private final org.eclipse.rdf4j.model.Model model;
  private final LdPathExecutor ldPathExecutor;
  private final Map<String, String> requestParameters;
  private final Map<String, String> responseParameters;
  private final InformationProduct informationProduct;

  public GraphEntityContext(@NonNull ImmutableMap<String, String> ldPathNamespaces,
      @NonNull Map<String, Model> swaggerDefinitions, @NonNull org.eclipse.rdf4j.model.Model model,
      @NonNull Map<String, String> requestParameters,
      @NonNull InformationProduct informationProduct) {
    this.requestParameters = requestParameters;
    this.responseParameters = new HashMap<>();
    this.ldPathNamespaces = ldPathNamespaces;
    this.swaggerDefinitions = Maps.newHashMap(swaggerDefinitions);
    this.model = model;
    this.ldPathExecutor = new LdPathExecutor(this);
    this.informationProduct = informationProduct;
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

  /**
   * @param key Overwrites any existing keys.
   * @param value Can be {@code null}.
   */
  public void addResponseParameter(@NonNull String key, String value) {
    responseParameters.put(key, value);
  }

  /**
   * @return The response parameters as an immutable Map.
   */
  public Map<String, String> getResponseParameters() {
    return ImmutableMap.copyOf(responseParameters);
  }

  public InformationProduct getInformationProduct() {
    return informationProduct;
  }

}
