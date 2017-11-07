package org.dotwebstack.framework.frontend.openapi.entity.builder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.swagger.models.Model;
import io.swagger.models.Swagger;
import java.util.Collections;
import java.util.Map;
import org.dotwebstack.framework.frontend.openapi.entity.properties.LdPathExecutor;

public class EntityBuilderContext {

  private final Swagger swagger;
  private final Map<String, Model> swaggerDefinitions;
  private final QueryResult queryResult;
  private final String baseUri;
  private final RequestParameters requestParameters;
  private String endpoint;
  private final LdPathExecutor ldPathExecutor;

  private EntityBuilderContext(Swagger swagger, Map<String, Model> swaggerDefinitions,
      QueryResult queryResult, String baseUri, RequestParameters requestParameters,
      String endpoint) {
    this.swagger = swagger;
    this.swaggerDefinitions = Maps.newHashMap(swaggerDefinitions);
    this.queryResult = queryResult;
    this.baseUri = baseUri;
    this.requestParameters = requestParameters;
    this.endpoint = endpoint;
    this.ldPathExecutor = new LdPathExecutor(this);
  }

  public LdPathExecutor getLdPathExecutor() {
    return this.ldPathExecutor;
  }

  public String getEndpoint() {
    return this.endpoint;
  }

  public Swagger getSwagger() {
    return swagger;
  }

  public Map<String, Model> getSwaggerDefinitions() {
    return Collections.unmodifiableMap(swaggerDefinitions);
  }

  public QueryResult getQueryResult() {
    return queryResult;
  }

  public String getBaseUri() {
    return baseUri;
  }

  public RequestParameters getRequestParameters() {
    return this.requestParameters;
  }

  public static class Builder {

    private Swagger swagger;
    private final Map<String, Model> swaggerDefinitions = Maps.newHashMap();
    private QueryResult queryResult;
    private String baseUri;
    private final String endpoint;
    private final RequestParameters requestParameters = new RequestParameters();

    public Builder(String endpoint) {

      this.endpoint = endpoint;
    }

    public Builder swagger(Swagger swagger) {
      this.swagger = swagger;
      this.swaggerDefinitions.putAll(extractSwaggerDefinitions(swagger));

      return this;
    }

    private static Map<String, Model> extractSwaggerDefinitions(Swagger swagger) {
      if (swagger.getDefinitions() != null) {
        return ImmutableMap.copyOf(swagger.getDefinitions());
      }
      return ImmutableMap.of();
    }

    public Builder queryResult(QueryResult queryResult) {
      this.queryResult = queryResult;
      return this;
    }

    public Builder baseUri(String baseUri) {
      this.baseUri = baseUri;
      return this;
    }

    public Builder requestParameters(RequestParameters requestParameters) {
      this.requestParameters.putAll(requestParameters);
      return this;
    }

    public EntityBuilderContext build() {
      return new EntityBuilderContext(swagger, swaggerDefinitions, queryResult, baseUri,
          requestParameters, this.endpoint);
    }
  }

}
