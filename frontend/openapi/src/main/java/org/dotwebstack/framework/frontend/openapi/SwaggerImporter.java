package org.dotwebstack.framework.frontend.openapi;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.openapi.handlers.GetRequestHandler;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.InformationProductLoader;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;

@Service
public class SwaggerImporter implements ResourceLoaderAware {

  private static final Logger LOG = LoggerFactory.getLogger(OpenApiExtension.class);

  private ResourceLoader resourceLoader;

  private InformationProductLoader informationProductLoader;

  private HttpConfiguration httpConfiguration;

  private SwaggerParser swaggerParser;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Autowired
  public SwaggerImporter(InformationProductLoader informationProductLoader,
      HttpConfiguration httpConfiguration, SwaggerParser swaggerParser) {
    this.informationProductLoader = Objects.requireNonNull(informationProductLoader);
    this.httpConfiguration = Objects.requireNonNull(httpConfiguration);
    this.swaggerParser = Objects.requireNonNull(swaggerParser);
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = Objects.requireNonNull(resourceLoader);
  }

  public void importDefinitions() {
    try {
      org.springframework.core.io.Resource[] resources =
          ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(
              "classpath:**/openapi/*");

      for (org.springframework.core.io.Resource resource : resources) {
        Swagger swagger = swaggerParser.parse(IOUtils.toString(resource.getInputStream(), "UTF-8"));
        mapSwaggerDefinition(swagger);
      }
    } catch (IOException e) {
      throw new ConfigurationException("Could not load OpenAPI definition documents.", e);
    }
  }

  private void mapSwaggerDefinition(Swagger swagger) {
    String basePath = createBasePath(swagger);

    swagger.getPaths().forEach((path, pathItem) -> {
      String absolutePath = basePath.concat(path);
      Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
      Operation getOperation = pathItem.getGet();

      if (getOperation == null) {
        return;
      }

      if (!getOperation.getVendorExtensions().containsKey("x-dotwebstack-information-product")) {
        LOG.warn("Path '{}' is not mapped to an information product.", absolutePath);
        return;
      }

      List<String> produces =
          getOperation.getProduces() != null ? getOperation.getProduces() : swagger.getProduces();

      if (produces == null) {
        throw new ConfigurationException(
            String.format("Path '%s' should produce at least one media type.", absolutePath));
      }

      IRI informationProductIdentifier = valueFactory.createIRI(
          (String) getOperation.getVendorExtensions().get("x-dotwebstack-information-product"));

      InformationProduct informationProduct =
          informationProductLoader.getInformationProduct(informationProductIdentifier);

      ResourceMethod.Builder methodBuilder =
          resourceBuilder.addMethod("GET").handledBy(new GetRequestHandler(informationProduct));

      produces.forEach(methodBuilder::produces);

      httpConfiguration.registerResource(resourceBuilder.build());

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
