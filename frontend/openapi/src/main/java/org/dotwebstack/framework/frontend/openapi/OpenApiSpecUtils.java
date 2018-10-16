package org.dotwebstack.framework.frontend.openapi;

import com.atlassian.oai.validator.interaction.ApiOperationResolver;
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
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.handlers.validation.RequestValidator;

public final class OpenApiSpecUtils {

  private static final String VENDOR_EXTENSION_DOTWEBSTACK = "x-dotwebstack";

  private OpenApiSpecUtils() {}

  public static RequestValidator createValidator(@NonNull OpenAPI openApi) {
    LevelResolver levelResolver = LevelResolver.defaultResolver();
    MessageResolver messageResolver = new MessageResolver(levelResolver);
    SchemaValidator schemaValidator = new SchemaValidator(openApi, messageResolver);
    return new RequestValidator(schemaValidator, messageResolver);
  }

  /**
   * @param openApi OpenAPI specification
   * @param path path of the requested operation
   * @param pathItem path of
   * @return {@link ApiOperation} if the provided swagger does contain the requested method at the
   *         provided path, <code>null</code> otherwise
   */
  public static Collection<ApiOperation> extractApiOperations(@NonNull OpenAPI openApi,
      @NonNull String path, @NonNull PathItem pathItem) {
    Set<Method> realMethods = new HashSet<>();
    realMethods.add(Method.GET);

    if (pathItem.getPost() != null) {
      realMethods.add(Method.POST);
    }

    if (pathItem.getPut() != null) {
      realMethods.add(Method.PUT);
    }

    Set<ApiOperation> apiOperations = new HashSet<>();
    realMethods.forEach(realMethod -> {
      ApiOperationMatch apiOperationMatch =
          new ApiOperationResolver(openApi, null).findApiOperation(path, realMethod);

      if (apiOperationMatch.isPathFound() && apiOperationMatch.isOperationAllowed()) {
        apiOperations.add(apiOperationMatch.getApiOperation());
      }
    });

    return apiOperations;
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
