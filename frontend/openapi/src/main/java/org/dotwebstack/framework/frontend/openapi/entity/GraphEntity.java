package org.dotwebstack.framework.frontend.openapi.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.QueryResult;
import org.eclipse.rdf4j.query.QueryResults;

public final class GraphEntity extends AbstractEntity {

  private final GraphEntityContext graphEntityContext;

  GraphEntity(@NonNull Map<MediaType, Property> schemaMap,
      @NonNull GraphEntityContext graphEntityContext) {
    super(schemaMap);
    this.graphEntityContext = graphEntityContext;
  }

  GraphEntityContext getEntityContext() {
    return graphEntityContext;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Map<String, io.swagger.models.Model> swaggerDefinitions;
    private ImmutableMap<String, String> ldpathNamespaces;
    private Model model;
    private Map<MediaType, Property> schemaMap;
    private Map<String, String> requestParameters;
    private Map<String, String> responseParameters = Maps.newHashMap();

    public Builder withSchemaMap(@NonNull Map<MediaType, Property> schemaMap) {
      this.schemaMap = schemaMap;
      return this;
    }

    public Builder withQueryResult(@NonNull QueryResult queryResult) {
      this.model = QueryResults.asModel(queryResult);
      return this;
    }

    public Builder withApiDefinitions(@NonNull Swagger definitions) {
      this.swaggerDefinitions = extractSwaggerDefinitions(definitions);
      return this;
    }

    public Builder withLdPathNamespaces(@NonNull Swagger definitions) {
      this.ldpathNamespaces = extractLdpathNamespaces(definitions);
      return this;
    }

    public Builder withRequestParameters(@NonNull Map<String, String> requestParameters) {
      this.requestParameters = requestParameters;
      return this;
    }

    private static Map<String, io.swagger.models.Model> extractSwaggerDefinitions(Swagger swagger) {
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
      return ImmutableMap.of();
    }

    public Entity build() {
      return new GraphEntity(schemaMap, new GraphEntityContext(ldpathNamespaces, swaggerDefinitions,
          model, requestParameters, responseParameters));
    }
  }

}
