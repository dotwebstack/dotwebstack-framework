package org.dotwebstack.framework.frontend.openapi.entity;

import static com.google.common.collect.ImmutableMap.copyOf;
import static com.google.common.collect.Maps.newHashMap;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.Model;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.QueryResult;
import org.eclipse.rdf4j.query.QueryResults;

@Getter
public final class GraphEntity extends AbstractEntity {

  private final ImmutableMap<String, String> ldPathNamespaces;
  private final Map<String, Model> swaggerDefinitions;
  private final org.eclipse.rdf4j.model.Model model;
  private final InformationProduct informationProduct;
  private final Map<String, String> requestParameters;
  private final Map<String, String> responseParameters;
  private final LdPathExecutor ldPathExecutor;

  private GraphEntity(@NonNull Map<MediaType, Property> schemaMap,
      @NonNull ImmutableMap<String, String> ldPathNamespaces,
      @NonNull Map<String, Model> swaggerDefinitions,
      @NonNull org.eclipse.rdf4j.model.Model model,
      @NonNull Map<String, String> requestParameters,
      @NonNull InformationProduct informationProduct) {
    super(schemaMap);

    this.ldPathNamespaces = ldPathNamespaces;
    this.swaggerDefinitions = swaggerDefinitions;
    this.model = model;
    this.informationProduct = informationProduct;
    this.requestParameters = requestParameters;
    this.responseParameters = newHashMap();
    this.ldPathExecutor = new LdPathExecutor(this);
  }

  public static GraphEntity newGraphEntity(@NonNull Map<MediaType, Property> schemaMap,
      @NonNull QueryResult<Statement> queryResult,
      @NonNull Swagger definitions,
      @NonNull Map<String, String> requestParameters,
      @NonNull InformationProduct informationProduct) {

    return new GraphEntity(schemaMap, extractLdpathNamespaces(definitions),
        extractSwaggerDefinitions(definitions), QueryResults.asModel(queryResult),
        requestParameters, informationProduct);
  }

  public void addResponseParameter(@NonNull String key, String value) {
    responseParameters.put(key, value);
  }

  public Map<String, String> getResponseParameters() {
    return ImmutableMap.copyOf(responseParameters);
  }

  private static Map<String, Model> extractSwaggerDefinitions(Swagger swagger) {
    return swagger.getDefinitions() == null ? ImmutableMap.of() : copyOf(swagger.getDefinitions());
  }

  private static ImmutableMap<String, String> extractLdpathNamespaces(Swagger swagger) {
    ImmutableMap<String, Object> vendorExtensions;
    Map<String, Object> extensions = swagger.getVendorExtensions();
    vendorExtensions = extensions == null ? ImmutableMap.of() : copyOf(extensions);
    if (vendorExtensions.containsKey(OpenApiSpecificationExtensions.LDPATH_NAMESPACES)) {
      Object ldPathNamespaces =
          vendorExtensions.get(OpenApiSpecificationExtensions.LDPATH_NAMESPACES);
      try {
        @SuppressWarnings("unchecked")
        Map<String, String> namespaces = (Map<String, String>) ldPathNamespaces;
        return copyOf(namespaces);
      } catch (ClassCastException cce) {
        String jsonExample = "{ \"rdfs\": \"http://www.w3.org/2000/01/rdf-schema#\", "
            + "\"rdf\": \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"}";
        throw new LdPathExecutorRuntimeException(String.format(
            "Vendor extension '%s' should contain a map of namespaces (eg. %s)",
            OpenApiSpecificationExtensions.LDPATH_NAMESPACES, jsonExample), cce);
      }
    }
    return ImmutableMap.of();
  }

}
