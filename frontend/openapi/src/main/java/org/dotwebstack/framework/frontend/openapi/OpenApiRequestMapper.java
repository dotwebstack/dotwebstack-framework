package org.dotwebstack.framework.frontend.openapi;

import com.google.common.base.Charsets;
import com.google.common.base.Functions;
import com.google.common.io.CharStreams;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import io.swagger.parser.SwaggerParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;
import lombok.NonNull;
import org.dotwebstack.framework.EnvironmentAwareResource;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.openapi.handlers.GetRequestHandler;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.InformationProductResourceProvider;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;

@Service
public class OpenApiRequestMapper implements ResourceLoaderAware {

  private static final Logger LOG = LoggerFactory.getLogger(OpenApiRequestMapper.class);

  private final String resourcePath;

  private final Environment environment;

  private final InformationProductResourceProvider informationProductResourceProvider;

  private final SwaggerParser openApiParser;

  private ResourceLoader resourceLoader;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Autowired
  public OpenApiRequestMapper(@NonNull InformationProductResourceProvider informationProductLoader,
      @NonNull SwaggerParser openApiParser,
      @Value("${dotwebstack.config.resourcePath: classpath:.}") String resourcePath,
      @NonNull Environment environment) {
    this.informationProductResourceProvider = informationProductLoader;
    this.openApiParser = openApiParser;
    this.resourcePath = resourcePath;
    this.environment = environment;
  }

  @Override
  public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  void map(HttpConfiguration httpConfiguration) throws IOException {
    org.springframework.core.io.Resource[] resources;

    try {
      resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(
          resourcePath + "/openapi/*");
    } catch (FileNotFoundException e) {
      LOG.warn("No openapi resources found in path:" + resourcePath);
      return;
    }

    for (org.springframework.core.io.Resource resource : resources) {
      InputStream inputStream =
          new EnvironmentAwareResource(resource.getInputStream(), environment).getInputStream();
      String result = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));

      Swagger swagger = openApiParser.parse(result);
      mapSwaggerDefinition(swagger, httpConfiguration);
    }
  }

  private void mapSwaggerDefinition(Swagger swagger, HttpConfiguration httpConfiguration) {
    String basePath = createBasePath(swagger);

    swagger.getPaths().forEach((path, pathItem) -> {
      String absolutePath = basePath.concat(path);
      Operation getOperation = pathItem.getGet();

      if (getOperation == null) {
        return;
      }

      if (!getOperation.getVendorExtensions().containsKey(
          OpenApiSpecificationExtensions.INFORMATION_PRODUCT)) {
        LOG.warn("Path '{}' is not mapped to an information product.", absolutePath);
        return;
      }

      String okStatusCode = Integer.toString(Status.OK.getStatusCode());

      if (getOperation.getResponses() == null
          || !getOperation.getResponses().containsKey(okStatusCode)) {
        throw new ConfigurationException(String.format(
            "Resource '%s' does not specify a status %s response.", absolutePath, okStatusCode));
      }

      List<String> produces =
          getOperation.getProduces() != null ? getOperation.getProduces() : swagger.getProduces();

      if (produces == null) {
        throw new ConfigurationException(
            String.format("Path '%s' should produce at least one media type.", absolutePath));
      }

      Property schema = getOperation.getResponses().get(okStatusCode).getSchema();

      if (schema == null) {
        throw new ConfigurationException(
            String.format("Resource '%s' does not specify a schema for the status %s response.",
                absolutePath, okStatusCode));
      }

      // Will eventually be replaced by OASv3 Content object
      Map<String, Property> schemaMap =
          produces.stream().collect(Collectors.toMap(Functions.identity(), mediaType -> schema));

      IRI informationProductIdentifier =
          valueFactory.createIRI((String) getOperation.getVendorExtensions().get(
              OpenApiSpecificationExtensions.INFORMATION_PRODUCT));

      InformationProduct informationProduct =
          informationProductResourceProvider.get(informationProductIdentifier);

      Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);

      ResourceMethod.Builder methodBuilder = resourceBuilder.addMethod(HttpMethod.GET).handledBy(
          new GetRequestHandler(informationProduct, schemaMap));

      produces.forEach(methodBuilder::produces);

      httpConfiguration.registerResources(resourceBuilder.build());

      LOG.debug("Mapped GET operation for request path {}", absolutePath);
    });
  }

  private String createBasePath(Swagger swagger) {
    String basePath = "/";

    if (swagger.getHost() == null) {
      throw new ConfigurationException(
          String.format("OpenAPI definition document '%s' must contain a 'host' attribute.",
              swagger.getInfo().getDescription()));
    }

    basePath = basePath.concat(swagger.getHost());

    if (swagger.getBasePath() != null) {
      basePath = basePath.concat(swagger.getBasePath());
    }

    return basePath;
  }

}
