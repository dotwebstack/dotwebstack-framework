package org.dotwebstack.framework.frontend.openapi.entity;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.Model;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.eclipse.rdf4j.query.GraphQueryResult;

public final class GraphEntity extends QueryEntity<GraphQueryResult> {


  private final Map<String, Model> swaggerDefitions;
  private final ImmutableMap<String, String> ldpathNamespaces;

  public GraphEntity(Property schemaProperty, QueryResult queryResult,
                     ImmutableMap<String, String> ldpathNamespaces, Map<String, Model> swaggerDefinitions) {
    super(schemaProperty, queryResult);
    this.swaggerDefitions = swaggerDefinitions;
    this.ldpathNamespaces = ldpathNamespaces;
  }


  public static Builder builder() {
    return new Builder();
  }

  @Override
  public GraphQueryResult getResult() {
    return null;
  }

  @Override
  public Map<MediaType, Property> getSchemaMap() {
    return null;
  }

  public Map<String, Model> getSwaggerDefitions() {
    return swaggerDefitions;
  }

  public ImmutableMap<String, String> getLdpathNamespaces() {
    return ldpathNamespaces;
  }


  public static class Builder extends QueryEntity.Builder {

    private Map<String, Model> swaggerDefinitions;
    private ImmutableMap<String, String> ldpathNamespaces;

    public Builder withSchemaProperty(Property schemaProperty) {
      this.schemaProperty = schemaProperty;
      return this;
    }

    public Builder withQueryResult(QueryResult queryResult) {
      this.queryResult = queryResult;
      return this;
    }

    public Builder withApiDefinitions(Swagger definitions) {
      this.swaggerDefinitions = extractSwaggerDefinitions(definitions);
      return this;
    }

    public Builder withLdPathNamespaces(Swagger definitions) {
      this.ldpathNamespaces = extractLdpathNamespaces(definitions);
      return this;
    }

    private static Map<String, Model> extractSwaggerDefinitions(Swagger swagger) {
      if (swagger.getDefinitions() != null) {
        return ImmutableMap.copyOf(swagger.getDefinitions());
      }
      return ImmutableMap.of();
    }

    private ImmutableMap<String, String> extractLdpathNamespaces(Swagger swagger) {
      Map<String, Object> extensions = swagger.getVendorExtensions();
      ImmutableMap<String, Object> vendorExtensions =
          extensions == null ? ImmutableMap.of() : ImmutableMap.copyOf(extensions);
      if (vendorExtensions.containsKey(OpenApiSpecificationExtensions.LDPATH_NAMESPACES)) {
        Object ldPathNamespaces =
            vendorExtensions.get(OpenApiSpecificationExtensions.LDPATH_NAMESPACES);
        try {
          return ImmutableMap.copyOf((Map<String, String>) ldPathNamespaces);
        } catch (ClassCastException cce) {
          throw new LdPathExecutorRuntimeException(String.format(
              "Vendor extension '%s' should contain a map of namespaces (eg. "
                  + "{ \"rdfs\": \"http://www.w3.org/2000/01/rdf-schema#\", "
                  + "\"rdf\": \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"})",
              OpenApiSpecificationExtensions.LDPATH_NAMESPACES), cce);
        }
      }

      return null;
    }

    public Entity build() {
      return new GraphEntity(schemaProperty, queryResult, ldpathNamespaces, swaggerDefinitions);
    }
  }

}
