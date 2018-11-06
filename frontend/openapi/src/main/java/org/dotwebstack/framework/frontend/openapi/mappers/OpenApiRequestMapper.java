package org.dotwebstack.framework.frontend.openapi.mappers;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.ws.rs.HttpMethod;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecUtils;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.SpecEnvironmentResolver;
import org.dotwebstack.framework.frontend.openapi.handlers.OpenApiSpecHandler;
import org.dotwebstack.framework.frontend.openapi.handlers.OptionsRequestHandler;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.Resource.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OpenApiRequestMapper {

  @NonNull
  private final OpenAPIV3Parser openApiParser;

  @NonNull
  private final ApplicationProperties applicationProperties;

  @NonNull
  private final List<RequestMapper> requestMappers;

  @Setter
  @NonNull
  private Environment environment = null;

  private static final ParseOptions OPTIONS = new ParseOptions();

  public void map(@NonNull HttpConfiguration httpConfiguration) throws IOException {
    SpecEnvironmentResolver resolver = new SpecEnvironmentResolver(environment);
    OPTIONS.setResolveFully(true);
    System.out.println("Looking for files in " + applicationProperties.getOpenApiResourcePath());
    List<Path> openApiFiles = Files.find(Paths.get(applicationProperties.getOpenApiResourcePath()),
        2, (path, bfa) -> path.getFileName().toString().endsWith(".oas3.yml")) //
        .collect(Collectors.toList());

    for (Path path : openApiFiles) {
      try (BufferedReader reader = Files.newBufferedReader(path)) {
        String result =
            reader.lines()
                .filter(Objects::nonNull)
                .map(resolver::replaceWithEnvVar)
                .collect(Collectors.joining("\n"));
        OpenAPI openApi = getOpenApi(result);
        mapOpenApiDefinition(openApi, httpConfiguration);
        addSpecResource(result, openApi, httpConfiguration);
      }
    }
  }

  private OpenAPI getOpenApi(String yamlContent) {
    SwaggerParseResult result = openApiParser.readContents(yamlContent, new ArrayList<>(), OPTIONS);
    result.getMessages().forEach(LOG::debug);
    return result.getOpenAPI();
  }

  private void mapOpenApiDefinition(OpenAPI openApi, HttpConfiguration httpConfiguration) {
    String basePath = createBasePath(openApi);

    openApi.getPaths().forEach(
        (path, pathItem) -> OpenApiSpecUtils.extractApiOperations(openApi, path, pathItem));

    openApi.getPaths().forEach((path, pathItem) -> {
      Collection<ApiOperation> apiOperations =
          OpenApiSpecUtils.extractApiOperations(openApi, path, pathItem);
      String absolutePath = basePath.concat(path);

      Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);

      apiOperations.forEach(apiOperation -> {
        LOG.debug("Mapping {} operation for request path {}", apiOperation.getMethod().name(),
            absolutePath);
        Operation operation = apiOperation.getOperation();
        Optional.ofNullable(operation.getRequestBody()).ifPresent(
            this::verifySchemasHaveTypeObject);

        requestMappers.stream() //
            .filter(mapper1 -> mapper1.supportsVendorExtension(operation.getExtensions())) //
            .findFirst() //
            .ifPresent(
                mapper -> mapper.map(resourceBuilder, openApi, apiOperation, operation,
                    absolutePath));

        if (requestMappers.stream() //
            .noneMatch(mapper1 -> mapper1.supportsVendorExtension(operation.getExtensions()))) {
          LOG.warn("Path '{}' is not mapped to an information product or transaction.",
              absolutePath);
        }
      });

      if (!resourceBuilder.build().getAllMethods().isEmpty()) {
        resourceBuilder.addMethod(HttpMethod.OPTIONS) //
            .handledBy(new OptionsRequestHandler(pathItem));
        httpConfiguration.registerResources(resourceBuilder.build());
      }
    });
  }

  private void addSpecResource(String yaml, OpenAPI openApi, HttpConfiguration httpConfiguration)
      throws IOException {
    Builder specResourceBuilder =
        Resource.builder().path(createBasePath(openApi) + getSpecEndpoint(openApi));
    specResourceBuilder.addMethod(HttpMethod.GET)//
        .produces("text/yaml")//
        .handledBy(new OpenApiSpecHandler(yaml));
    specResourceBuilder //
        .addMethod(HttpMethod.OPTIONS) //
        .handledBy(new OptionsRequestHandler(new PathItem().get(new Operation())));
    httpConfiguration.registerResources(specResourceBuilder.build());
  }

  private String getSpecEndpoint(OpenAPI openApi) {
    return Optional.ofNullable(openApi.getExtensions()).map(
        map -> (String) map.get(OpenApiSpecificationExtensions.SPEC_ENDPOINT)) //
        .orElse("/");
  }

  /**
   * @throws ConfigurationException If the supplied operation has a body parameter, and it does not
   *         have a schema of type Object (because a schema of type Object is the only type we are
   *         currently supporting).
   */
  private void verifySchemasHaveTypeObject(@Nullable RequestBody requestBody) {
    String mediaType = Stream.of(requestBody) //
        .filter(Objects::nonNull) //
        .map(RequestBody::getContent) //
        .filter(Objects::nonNull) //
        .map(Map::values) //
        .flatMap(Collection::stream) //
        .map(MediaType::getSchema) //
        .filter(Objects::nonNull) //
        .map(Schema::getType) //
        .filter("object"::equalsIgnoreCase) //
        .findAny() //
        .orElseThrow(() -> new ConfigurationException("No object property in body parameter."));
    LOG.debug("Found {} in requestBody", mediaType);
  }

  private String createBasePath(OpenAPI openApi) {
    String oas =
        "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md#schema";

    Optional<String> optionalBasePath = Stream.of(openApi) //
        .map(OpenAPI::getServers) //
        .filter(Objects::nonNull) //
        .flatMap(Collection::stream) //
        .map(Server::getUrl) //
        .filter(anotherString -> !"/".equalsIgnoreCase(anotherString)) //
        .findAny();
    String url = optionalBasePath //
        .orElseThrow(() -> new ConfigurationException(String.format(
            "Expecting at least one server definition on the OpenAPI spec '%s'. See: %s",
            openApi.getInfo().getDescription(), oas)));
    return url.substring(url.indexOf("://") + 2);
  }

}
