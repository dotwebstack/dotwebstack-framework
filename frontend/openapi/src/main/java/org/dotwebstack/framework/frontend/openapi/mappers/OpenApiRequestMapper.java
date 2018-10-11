package org.dotwebstack.framework.frontend.openapi.mappers;

import static javax.ws.rs.HttpMethod.GET;

import com.atlassian.oai.validator.model.ApiOperation;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.ws.rs.HttpMethod;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.EnvironmentAwareResource;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecUtils;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.handlers.OpenApiSpecHandler;
import org.dotwebstack.framework.frontend.openapi.handlers.OptionsRequestHandler;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.Resource.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;

@Service
public class OpenApiRequestMapper implements ResourceLoaderAware, EnvironmentAware {

  private static final Logger LOG = LoggerFactory.getLogger(OpenApiRequestMapper.class);

  private static final PathItem specPath;

  private final OpenAPIV3Parser openApiParser;

  private ApplicationProperties applicationProperties;

  private ResourceLoader resourceLoader;

  private Environment environment;

  private List<RequestMapper> requestMappers;

  static {
    specPath = new PathItem();
    specPath.setGet(new Operation());
  }

  @Autowired
  public OpenApiRequestMapper(@NonNull OpenAPIV3Parser openApiParser,
      @NonNull ApplicationProperties applicationProperties,
      @NonNull List<RequestMapper> requestMappers) {
    this.openApiParser = openApiParser;
    this.applicationProperties = applicationProperties;
    this.requestMappers = requestMappers;
  }

  @Override
  public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  public void setEnvironment(@NonNull Environment environment) {
    this.environment = environment;
  }

  public void map(@NonNull HttpConfiguration httpConfiguration) throws IOException {
    org.springframework.core.io.Resource[] resources;

    try {
      resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(
          applicationProperties.getResourcePath() + "/openapi/**.y*ml");
    } catch (FileNotFoundException exp) {
      LOG.warn("No Open API resources found in path:{}/openapi",
          applicationProperties.getResourcePath());
      return;
    }

    for (org.springframework.core.io.Resource resource : resources) {
      InputStream inputStream =
          new EnvironmentAwareResource(resource.getInputStream(), environment).getInputStream();
      String result = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
      if (!StringUtils.isBlank(result)) {
        ParseOptions options = new ParseOptions();
        options.setResolveFully(true);
        OpenAPI openAPI = openApiParser.readContents(result, new ArrayList<>(), options).getOpenAPI();
        mapOpenApiDefinition(openAPI, httpConfiguration);
        addSpecResource(result, openAPI, httpConfiguration);
      }
    }
  }

  private void mapOpenApiDefinition(OpenAPI openAPI, HttpConfiguration httpConfiguration) {
    String basePath = createBasePath(openAPI);

    openAPI.getPaths().forEach((path, pathItem) -> {
      Collection<ApiOperation> apiOperations =
          OpenApiSpecUtils.extractApiOperations(openAPI, path, pathItem);
      String absolutePath = basePath.concat(path);

      Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);

      for (ApiOperation apiOperation : apiOperations) {
        Operation operation = apiOperation.getOperation();

        if (operation.getRequestBody() != null) {
          verifySchemasHaveTypeObject(operation.getRequestBody());
        }

        Optional<RequestMapper> optionalRequestMapper = requestMappers.stream() //
            .filter(mapper -> mapper.supportsVendorExtension(operation.getExtensions())) //
            .findFirst();

        optionalRequestMapper.ifPresent(
            mapper -> mapper.map(resourceBuilder, openAPI, apiOperation, operation, absolutePath));

        if (!optionalRequestMapper.isPresent()) {
          LOG.warn("Path '{}' is not mapped to an information product or transaction.",
              absolutePath);
        }
      }

      if (!resourceBuilder.build().getAllMethods().isEmpty()) {
        resourceBuilder.addMethod(HttpMethod.OPTIONS).handledBy(
            new OptionsRequestHandler(pathItem));
        httpConfiguration.registerResources(resourceBuilder.build());
      }
    });
  }

  private void addSpecResource(String yaml, OpenAPI openAPI, HttpConfiguration httpConfiguration)
      throws IOException {
    String basePath = createBasePath(openAPI);
    String specEndpoint = getSpecEndpoint(openAPI).orElse("/");
    OpenApiSpecHandler handler = new OpenApiSpecHandler(yaml);
    Builder specResourceBuilder = Resource.builder().path(basePath + specEndpoint);
    specResourceBuilder//
        .addMethod(GET)//
        .produces("text/yaml")//
        .handledBy(handler);
    specResourceBuilder.addMethod(HttpMethod.OPTIONS).handledBy(
        new OptionsRequestHandler(specPath));
    httpConfiguration.registerResources(specResourceBuilder.build());
  }

  private Optional<String> getSpecEndpoint(OpenAPI openAPI) {
    return Optional.ofNullable(openAPI.getExtensions()).map(
        map -> (String) map.get(OpenApiSpecificationExtensions.SPEC_ENDPOINT));
  }

  /**
   * @throws ConfigurationException If the supplied operation has a body parameter, and it does not
   *         have a schema of type Object (because a schema of type Object is the only type we are
   *         currently supporting).
   */
  private void verifySchemasHaveTypeObject(RequestBody requestBody) {
    LinkedHashMap<String,MediaType> content = requestBody.getContent();
    if (content == null) {
      throw new ConfigurationException("No object property in body parameter.");
    }
    String mediaType1 = content.values().stream()
        .map(MediaType::getSchema)
        .filter(Objects::nonNull)
        .map(Schema::getType)
        .filter("object"::equalsIgnoreCase)
        .findAny()
        .orElseThrow(() -> new ConfigurationException("No object property in body parameter."));
    LOG.debug("Object property: {}", mediaType1);
  }

  private String createBasePath(OpenAPI openAPI) {
    if (openAPI.getServers() == null
        || openAPI.getServers().isEmpty()
        || openAPI.getServers().get(0).getUrl().equalsIgnoreCase("/")) {
      throw new ConfigurationException(String.format("Expecting at least one server definition on "
          + "the OpenAPI spec '%s'. See: "
          + "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md#schema",
          openAPI.getInfo().getDescription()));
    }

    String url = openAPI.getServers().get(0).getUrl();

    return url.substring(url.indexOf("://") + 2);
  }

}
