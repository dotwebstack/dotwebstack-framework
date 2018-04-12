package org.dotwebstack.framework.frontend.openapi.mappers;

import com.atlassian.oai.validator.model.ApiOperation;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.parser.SwaggerParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.EnvironmentAwareResource;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.openapi.SwaggerUtils;
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

  private final SwaggerParser openApiParser;

  private ApplicationProperties applicationProperties;

  private ResourceLoader resourceLoader;

  private Environment environment;

  private InformationProductRequestMapper informationProductRequestMapper;

  private TransactionRequestMapper transactionRequestMapper;

  @Autowired
  public OpenApiRequestMapper(@NonNull SwaggerParser openApiParser,
      @NonNull ApplicationProperties applicationProperties,
      @NonNull InformationProductRequestMapper informationProductRequestMapper,
      @NonNull TransactionRequestMapper transactionRequestMapper) {
    this.openApiParser = openApiParser;
    this.applicationProperties = applicationProperties;
    this.informationProductRequestMapper = informationProductRequestMapper;
    this.transactionRequestMapper = transactionRequestMapper;
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

      Optional<String> vendorExtension =
          getOperation.getVendorExtensions().keySet().stream().findFirst();
      if (vendorExtension.isPresent()
          && informationProductRequestMapper.supportsVendorExtension(vendorExtension.get())) {
        httpConfiguration.registerResources(informationProductRequestMapper.map(swagger, pathItem,
            apiOperation, getOperation, absolutePath));
      } else if (vendorExtension.isPresent()
          && transactionRequestMapper.supportsVendorExtension(vendorExtension.get())) {
        httpConfiguration.registerResources(transactionRequestMapper.map(swagger, pathItem,
            apiOperation, getOperation, absolutePath));
      } else {
        LOG.warn("Path '{}' is not mapped to an information product or transaction.", absolutePath);
      }
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
