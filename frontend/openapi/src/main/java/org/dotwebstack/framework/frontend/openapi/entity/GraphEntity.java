package org.dotwebstack.framework.frontend.openapi.entity;

import static com.google.common.collect.ImmutableMap.copyOf;

import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestContext;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.Repository;

@Getter
public final class GraphEntity extends AbstractEntity {

  private final ImmutableMap<String, String> ldPathNamespaces;

  private final Components openApiComponents;

  private final Repository repository;

  private final Set<Resource> subjects;

  private final LdPathExecutor ldPathExecutor;

  private GraphEntity(@NonNull ApiResponse response,
      @NonNull ImmutableMap<String, String> ldPathNamespaces, @NonNull Components openApiComponents,
      @NonNull Repository repository, @NonNull Set<Resource> subjects,
      @NonNull RequestContext requestContext) {
    super(response, requestContext);

    this.ldPathNamespaces = ldPathNamespaces;
    this.openApiComponents = openApiComponents;
    this.repository = repository;
    this.subjects = subjects;
    this.ldPathExecutor = new LdPathExecutor(this);
  }

  public static GraphEntity newGraphEntity(@NonNull ApiResponse response,
      @NonNull Repository repository, @NonNull Set<Resource> subjects, @NonNull OpenAPI openApi,
      @NonNull RequestContext requestContext) {
    return new GraphEntity(response, extractLdpathNamespaces(openApi),
        openApi.getComponents() == null ? new Components() : openApi.getComponents(), repository,
        subjects, requestContext);
  }

  private static ImmutableMap<String, String> extractLdpathNamespaces(OpenAPI openApi) {
    ImmutableMap<String, Object> vendorExtensions;
    Map<String, Object> extensions = openApi.getExtensions();
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

}
