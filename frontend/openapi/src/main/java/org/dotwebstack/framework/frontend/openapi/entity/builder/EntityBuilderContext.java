package org.dotwebstack.framework.frontend.openapi.entity.builder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.swagger.models.Model;
import io.swagger.models.Swagger;
import java.util.Map;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;

public class EntityBuilderContext {

  private final Swagger swagger;
  private final Map<String, Model> swaggerDefinitions;
  private final QueryResult queryResult;
  private final LdPathExecutor ldPathExecutor;

  private EntityBuilderContext(Swagger swagger, Map<String, Model> swaggerDefinitions,
      QueryResult queryResult) {
    this.swagger = swagger;
    this.swaggerDefinitions = Maps.newHashMap(swaggerDefinitions);
    this.queryResult = queryResult;
    this.ldPathExecutor = new LdPathExecutor(this);
  }

  public LdPathExecutor getLdPathExecutor() {
    return this.ldPathExecutor;
  }

  public Swagger getSwagger() {
    return swagger;
  }

  public QueryResult getQueryResult() {
    return queryResult;
  }

  public static class Builder {

    private Swagger swagger;
    private final Map<String, Model> swaggerDefinitions = Maps.newHashMap();
    private QueryResult queryResult;

    public Builder() {

    }

    public Builder swagger(Swagger swagger) {
      this.swagger = swagger;
      this.swaggerDefinitions.putAll(extractSwaggerDefinitions(swagger));

      return this;
    }

    private static Map<String, Model> extractSwaggerDefinitions(Swagger swagger) {
      if (swagger != null && swagger.getDefinitions() != null) {
        return ImmutableMap.copyOf(swagger.getDefinitions());
      }
      return ImmutableMap.of();
    }

    public Builder queryResult(QueryResult queryResult) {
      this.queryResult = queryResult;
      return this;
    }

    public EntityBuilderContext build() {
      return new EntityBuilderContext(swagger, swaggerDefinitions, queryResult);
    }
  }

}
