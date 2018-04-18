package org.dotwebstack.framework.frontend.openapi;

import com.atlassian.oai.validator.model.ApiOperation;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.parser.SwaggerParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.EnvironmentAwareResource;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.openapi.handlers.OptionsRequestHandler;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestHandlerFactory;
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
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;

@Service
class OpenApiRequestMapper implements ResourceLoaderAware, EnvironmentAware {

  private static final Logger LOG = LoggerFactory.getLogger(OpenApiRequestMapper.class);

  private final InformationProductResourceProvider informationProductResourceProvider;

  private final SwaggerParser openApiParser;

  private final RequestHandlerFactory requestHandlerFactory;

  private ApplicationProperties applicationProperties;

  private ResourceLoader resourceLoader;

  private Environment environment;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Autowired
  public OpenApiRequestMapper(@NonNull InformationProductResourceProvider informationProductLoader,
      @NonNull SwaggerParser openApiParser, @NonNull ApplicationProperties applicationProperties,
      @NonNull RequestHandlerFactory requestHandlerFactory) {
    this.informationProductResourceProvider = informationProductLoader;
    this.openApiParser = openApiParser;
    this.applicationProperties = applicationProperties;
    this.requestHandlerFactory = requestHandlerFactory;
  }

  @Override
  public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  public void setEnvironment(@NonNull Environment environment) {
    this.environment = environment;
  }

  void map(@NonNull HttpConfiguration httpConfiguration) throws IOException {
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
        Swagger swagger = openApiParser.parse(result);
        mapOpenApiDefinition(swagger, httpConfiguration);
      }
    }
  }

  private void mapOpenApiDefinition(Swagger swagger, HttpConfiguration httpConfiguration) {
    String basePath = createBasePath(swagger);

    swagger.getPaths().forEach((path, pathItem) -> {
      ApiOperation apiOperation = SwaggerUtils.extractApiOperation(swagger, path, pathItem);

      if (apiOperation == null) {
        return;
      }

      Operation getOperation = apiOperation.getOperation();

      validateOperation(apiOperation, swagger);

      String absolutePath = basePath.concat(path);

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

      Response response = getOperation.getResponses().get(okStatusCode);

      IRI informationProductIdentifier =
          valueFactory.createIRI((String) getOperation.getVendorExtensions().get(
              OpenApiSpecificationExtensions.INFORMATION_PRODUCT));

      InformationProduct informationProduct =
          informationProductResourceProvider.get(informationProductIdentifier);

      Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);

      ResourceMethod.Builder methodBuilder =
          resourceBuilder.addMethod(apiOperation.getMethod().name()).handledBy(
              requestHandlerFactory.newRequestHandler(apiOperation, informationProduct, response,
                  swagger));

      produces.forEach(methodBuilder::produces);

      resourceBuilder.addMethod(HttpMethod.OPTIONS).handledBy(new OptionsRequestHandler(pathItem));

      httpConfiguration.registerResources(resourceBuilder.build());

      LOG.debug("Mapped {} operation for request path {}", apiOperation.getMethod().name(),
          absolutePath);
    });
  }

  /**
   * @throws ConfigurationException If the supplied operation has a body parameter, and it does not
   *         have a schema of type Object (because a schema of type Object is the only type we are
   *         currently supporting).
   */
  private void validateOperation(ApiOperation apiOperation, Swagger swagger) {
    apiOperation.getOperation().getParameters().stream().filter(
        parameterBody -> "body".equalsIgnoreCase(parameterBody.getIn())).forEach(parameterBody -> {
          if ((parameterBody instanceof BodyParameter)) {
            ModelImpl parameterModel = getBodyParameter(swagger, (BodyParameter) parameterBody);
            if (!"object".equalsIgnoreCase(parameterModel.getType())) {
              throw new ConfigurationException("No object property in body parameter.");
            }
          }
        });
  }

  private ModelImpl getBodyParameter(@NonNull Swagger swagger, BodyParameter parameterBody) {
    ModelImpl parameterModel = null;
    if (parameterBody.getSchema() instanceof ModelImpl) {
      parameterModel = ((ModelImpl) (parameterBody.getSchema()));
    }
    if (parameterBody.getSchema() instanceof RefModel) {
      RefModel refModel = ((RefModel) (parameterBody.getSchema()));
      parameterModel = (ModelImpl) swagger.getDefinitions().get(refModel.getSimpleRef());
    }
    return parameterModel;
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
