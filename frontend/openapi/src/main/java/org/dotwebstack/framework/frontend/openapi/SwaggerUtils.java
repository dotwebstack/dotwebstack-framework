package org.dotwebstack.framework.frontend.openapi;

import com.atlassian.oai.validator.interaction.ApiOperationResolver;
import com.atlassian.oai.validator.interaction.RequestValidator;
import com.atlassian.oai.validator.model.ApiOperation;
import com.atlassian.oai.validator.model.ApiOperationMatch;
import com.atlassian.oai.validator.model.Request.Method;
import com.atlassian.oai.validator.report.LevelResolver;
import com.atlassian.oai.validator.report.MessageResolver;
import com.atlassian.oai.validator.schema.SchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;

public final class SwaggerUtils {

  private static final String VENDOR_EXTENSION_DOTWEBSTACK = "x-dotwebstack";

  private SwaggerUtils() {}

  public static RequestValidator createValidator(@NonNull Swagger swagger) {
    LevelResolver levelResolver = LevelResolver.defaultResolver();
    MessageResolver messageResolver = new MessageResolver(levelResolver);
    SchemaValidator schemaValidator = new SchemaValidator(swagger, messageResolver);
    return new RequestValidator(schemaValidator, messageResolver, swagger);
  }

  /**
   * @param swagger Swagger specification
   * @param path path of the requested operation
   * @param apiPath path of
   * @return {@link ApiOperation} if the provided swagger does contain the requested method at the
   *         provided path, <code>null</code> otherwise
   */
  public static ApiOperation extractApiOperation(@NonNull Swagger swagger, @NonNull String path,
      @NonNull Path apiPath) {
    Method realMethod = Method.GET;

    if (apiPath.getGet() != null) {
      realMethod = Method.GET;
    }
    if (apiPath.getPost() != null) {
      realMethod = Method.POST;
    }
    ApiOperationMatch apiOperationMatch =
        new ApiOperationResolver(swagger, null).findApiOperation(path, realMethod);

    return apiOperationMatch.isPathFound() && apiOperationMatch.isOperationAllowed()
        ? apiOperationMatch.getApiOperation()
        : null;
  }

  public static ObjectNode removeVendorExtensions(@NonNull InputStream specStream,
      @NonNull YAMLMapper mapper) throws IOException {
    ObjectNode specNode = mapper.readValue(specStream, ObjectNode.class);
    removeExtensionNodes(specNode);

    return specNode;
  }


  private static void removeExtensionNodes(ContainerNode<?> node) {
    if (node.isObject()) {
      removeExtensionNodesFromObject((ObjectNode) node);
    } else if (node.isArray()) {
      ArrayNode arrayNode = (ArrayNode) node;
      arrayNode.forEach(arrayElement -> {
        if (arrayElement.isContainerNode()) {
          removeExtensionNodes((ContainerNode<?>) arrayElement);
        }
      });
    }
    // other elements are 'textnodes' and do not need to be removed
  }

  private static void removeExtensionNodesFromObject(ObjectNode node) {
    List<String> passedFields = new ArrayList<>();
    node.fieldNames().forEachRemaining(name -> {
      if (!name.startsWith(VENDOR_EXTENSION_DOTWEBSTACK)) {
        passedFields.add(name);
      }
    });
    node.retain(passedFields);
    node.fieldNames().forEachRemaining(name -> {
      JsonNode jsonNode = node.get(name);
      if (jsonNode.isContainerNode()) {
        removeExtensionNodes((ContainerNode<?>) jsonNode);
      }
    });
  }

}
