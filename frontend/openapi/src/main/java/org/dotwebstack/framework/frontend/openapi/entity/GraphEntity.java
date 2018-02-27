package org.dotwebstack.framework.frontend.openapi.entity;

import static com.google.common.collect.ImmutableMap.copyOf;
import static com.google.common.collect.Maps.newHashMap;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.Model;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import java.util.Collections;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.Rdf4jUtils;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.QueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;

@Getter
public final class GraphEntity extends AbstractEntity {

  private final ImmutableMap<String, String> ldPathNamespaces;
  private final Map<String, Model> swaggerDefinitions;
  private final Repository repository;
  private final InformationProduct informationProduct;

  private final Map<String, String> parameters;

  private final LdPathExecutor ldPathExecutor;
  private final String baseUri;

  private GraphEntity(@NonNull Map<MediaType, Property> schemaMap,
      @NonNull ImmutableMap<String, String> ldPathNamespaces,
      @NonNull Map<String, Model> swaggerDefinitions, @NonNull Repository repository,
      @NonNull Map<String, String> requestParameters, @NonNull String baseUri,
      @NonNull InformationProduct informationProduct) {
    super(schemaMap);

    this.ldPathNamespaces = ldPathNamespaces;
    this.swaggerDefinitions = swaggerDefinitions;
    this.repository = repository;
    this.informationProduct = informationProduct;
    this.parameters = newHashMap(requestParameters);
    this.baseUri = baseUri;
    this.ldPathExecutor = new LdPathExecutor(this);
  }

  public static GraphEntity newGraphEntity(@NonNull Map<MediaType, Property> schemaMap,
      @NonNull QueryResult<Statement> queryResult, @NonNull Swagger definitions,
      @NonNull Map<String, String> requestParameters,
      @NonNull InformationProduct informationProduct, @NonNull String baseUri) {

    return new GraphEntity(schemaMap, extractLdpathNamespaces(definitions),
        extractSwaggerDefinitions(definitions),
        Rdf4jUtils.asRepository(QueryResults.asModel(queryResult)), requestParameters, baseUri,
        informationProduct);
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
        throw new LdPathExecutorRuntimeException(
            String.format("Vendor extension '%s' should contain a map of namespaces (eg. %s)",
                OpenApiSpecificationExtensions.LDPATH_NAMESPACES, jsonExample),
            cce);
      }
    }

    return ImmutableMap.of();
  }

  public void addParameter(@NonNull String key, String value) {
    parameters.put(key, value);
  }

  public Map<String, String> getParameters() {
    return Collections.unmodifiableMap(parameters);
  }

}
