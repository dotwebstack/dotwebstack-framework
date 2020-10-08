package org.dotwebstack.framework.service.openapi.mapping;

import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.mappingException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.notFoundException;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsExtension;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.isTransient;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.ARRAY_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.OBJECT_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR_FALLBACK_VALUE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR_VALUE;
import static org.dotwebstack.framework.service.openapi.mapping.ResponseMapperHelper.isRequiredOrExpandedAndNullOrEmpty;
import static org.dotwebstack.framework.service.openapi.response.ResponseContextHelper.getPathString;
import static org.dotwebstack.framework.service.openapi.response.ResponseContextHelper.isExpanded;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.copyResponseContext;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.createResponseContextFromChildData;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.createResponseWriteContextFromChildSchema;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.unwrapChildSchema;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.unwrapComposedSchema;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.unwrapItemSchema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.media.Schema;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.service.openapi.conversion.TypeConverterRouter;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
import org.dotwebstack.framework.service.openapi.response.FieldContext;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
import org.dotwebstack.framework.service.openapi.response.ResponseWriteContext;
import org.dotwebstack.framework.service.openapi.response.SchemaSummary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

@Component
public class JsonResponseMapper {

  private static final Map<String, Class<?>> TYPE_CLASS_MAPPING =
      Map.of("string", String.class, "integer", Integer.class);

  private final ObjectMapper objectMapper;

  private final JexlHelper jexlHelper;

  private final EnvironmentProperties properties;

  private final TypeConverterRouter typeConverterRouter;

  public JsonResponseMapper(Jackson2ObjectMapperBuilder objectMapperBuilder, JexlEngine jexlEngine,
      EnvironmentProperties properties, TypeConverterRouter typeConverterRouter) {
    this.objectMapper = objectMapperBuilder.build();
    this.jexlHelper = new JexlHelper(jexlEngine);
    this.properties = properties;
    this.typeConverterRouter = typeConverterRouter;
  }

  public String toResponse(@NonNull Object input) {
    if (input instanceof ResponseWriteContext) {
      try {
        return toJson((ResponseWriteContext) input);
      } catch (JsonProcessingException jpe) {
        throw new ResponseMapperException("An exception occurred when serializing to JSON.", jpe);
      }
    } else {
      throw new IllegalArgumentException("Input can only be of the type ResponseWriteContext.");
    }
  }

  private String toJson(ResponseWriteContext writeContext) throws JsonProcessingException {
    Object response = mapDataToResponse(writeContext, "");
    if (Objects.isNull(response)) {
      throw notFoundException("Did not find data for your response.");
    }

    return toJson(response);
  }

  private String toJson(Object object) throws JsonProcessingException {
    return this.objectMapper.writer()
        .writeValueAsString(object);
  }

  private Object mapDataToResponse(@NonNull ResponseWriteContext writeContext, String path) {
    ResponseObject responseObject = writeContext.getResponseObject();
    SchemaSummary summary = responseObject.getSummary();

    String newPath = addToPath(path, responseObject, false);

    switch (summary.getType()) {
      case ARRAY_TYPE:
        if ((summary.isRequired()
            || isExpanded(writeContext.getParameters(), removeRoot(addToPath(newPath, responseObject, true))))) {

          if (isDefaultValue(summary)) {
            return mapDefaultArrayToResponse(responseObject);
          }

          return mapArrayDataToResponse(writeContext, newPath);
        }

        return null;
      case OBJECT_TYPE:
        Object object = processObject(writeContext, summary, newPath);

        /*
         * After the object is mapped, we check if it was a composed schema. If that is the case one layer
         * is unwrapped in the response. This layer only exist in the schema, not in the response.
         */
        if (object != null && !writeContext.getResponseObject()
            .getSummary()
            .getComposedOf()
            .isEmpty()) {
          object = ((Map) object).get(((Map) object).keySet()
              .iterator()
              .next());
        }
        return object;
      default:
        if (summary.isRequired() || Objects.nonNull(summary.getDwsExpr())
            || isExpanded(writeContext.getParameters(), removeRoot(newPath))) {
          return mapScalarDataToResponse(writeContext);
        }
        return null;
    }
  }

  private Object mapDefaultArrayToResponse(ResponseObject responseObject) {
    Object defaultValue;
    SchemaSummary summary = responseObject.getSummary();
    if (summary.getSchema() != null
        && (defaultValue = getDwsExtension(summary.getSchema(), OasConstants.X_DWS_DEFAULT)) != null) {
      if (defaultValue instanceof List) {
        return defaultValue;
      }

      throw mappingException("'{}' value for property '{}' not of type array!", OasConstants.X_DWS_DEFAULT,
          responseObject.getIdentifier());
    }

    if (summary.isNillable()) {
      return null;
    }
    return List.of();
  }

  private Object processObject(@NonNull ResponseWriteContext writeContext, SchemaSummary summary, String newPath) {
    if (summary.isRequired() || summary.isTransient()
        || isExpanded(writeContext.getParameters(), removeRoot(newPath))) {
      if (summary.isTransient()) {
        return mapEnvelopeObjectToResponse(writeContext, newPath);
      }
      if (writeContext.isComposedOf()) {
        return mapComposedDataToResponse(writeContext, newPath);
      }
      return mapObjectDataToResponse(writeContext, newPath);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private Object mapComposedDataToResponse(ResponseWriteContext parentContext, String path) {
    if (Objects.isNull(parentContext.getData())) {
      return null;
    }

    Map<String, Object> result = new HashMap<>();
    parentContext.getResponseObject()
        .getSummary()
        .getComposedOf()
        .forEach(composedSchema -> {
          ResponseWriteContext writeContext = copyResponseContext(parentContext, composedSchema);
          mergeComposedResponse(path, result, writeContext, writeContext.getResponseObject()
              .getIdentifier());
        });

    return createResponseObject(result, parentContext, path);
  }

  private Map<String, Object> createResponseObject(Map<String, Object> result, ResponseWriteContext parentContext,
      String path) {
    if (result.isEmpty()) {
      return null;
    }
    List<ResponseWriteContext> children;
    if (parentContext.isComposedOf()) {
      children = unwrapComposedSchema(parentContext);
    } else {
      children = unwrapChildSchema(parentContext);
    }

    /*
     * Objects with an identifying field that have no data are considered null. An identifying field is
     * a field that determines the existence of the associated object.
     */

    boolean hasIdentifyingfield = children.stream()
        .anyMatch(ResponseWriteContext::isSchemaRequiredNonNillable);

    if ((result.values()
        .stream()
        .allMatch(Objects::isNull)) && hasIdentifyingfield) {
      return null;
    }
    validateRequiredProperties(parentContext, path, result);
    return result;
  }

  String removeRoot(String path) {
    if (path.contains(".")) {
      return path.substring(path.indexOf('.') + 1);
    }
    return "";
  }

  @SuppressWarnings("unchecked")
  private Object mapArrayDataToResponse(ResponseWriteContext parentContext, String path) {
    if (Objects.isNull(parentContext.getData())) {
      return mapDefaultArrayToResponse(parentContext.getResponseObject());
    } else if (parentContext.getData() instanceof List) {
      return ((List<Object>) parentContext.getData()).stream()
          .map(childData -> mapDataToResponse(createResponseContextFromChildData(parentContext, childData), path))
          .collect(Collectors.toList());
    }
    return mapDataToResponse(unwrapItemSchema(parentContext), path);
  }

  private Object mapObjectDataToResponse(@NonNull ResponseWriteContext parentContext, String path) {
    if (Objects.isNull(parentContext.getData())) {
      return null;
    }

    if (Objects.nonNull(parentContext.getResponseObject()
        .getSummary()
        .getDwsType())) {
      return parentContext.getData();
    }

    if (parentContext.getResponseObject()
        .getSummary()
        .getChildren()
        .isEmpty()) {
      return parentContext.getData();
    }

    Map<String, Object> result = new HashMap<>();
    parentContext.getResponseObject()
        .getSummary()
        .getChildren()
        .forEach(childSchema -> {
          ResponseWriteContext writeContext = createResponseWriteContextFromChildSchema(parentContext, childSchema);
          addDataToResponse(path, result, childSchema.getIdentifier(), writeContext);
        });

    return createResponseObject(result, parentContext, path);
  }

  private void addDataToResponse(String path, Map<String, Object> response, String identifier,
      ResponseWriteContext writeContext) {
    boolean isExpanded = isExpanded(writeContext.getParameters(), childPath(path, identifier));
    Object object = mapObject(writeContext, mapDataToResponse(writeContext, path), isExpanded);

    if (Objects.nonNull(object) || (writeContext.isSchemaRequiredNillable() || isExpanded)) {
      response.put(identifier, convertType(writeContext, object));
    }
  }

  private String childPath(String path, String identifier) {
    String newPath = removeRoot(path);
    return newPath.length() > 0 ? newPath + "." + identifier : identifier;
  }

  Object mapScalarDataToResponse(@NonNull ResponseWriteContext writeContext) {

    SchemaSummary summary = writeContext.getResponseObject()
        .getSummary();

    if (isDefaultValue(summary)) {
      return getScalarDefaultValue(writeContext, summary);
    }

    if (Objects.isNull(writeContext.getResponseObject()
        .getSummary()
        .getDwsExpr())) {
      return writeContext.getData();
    }

    Optional<String> evaluated = evaluateJexl(writeContext);
    if (evaluated.isPresent()) {
      return evaluated.get();
    }

    if (Objects.nonNull(writeContext.getResponseObject()
        .getSummary()
        .getSchema()
        .getDefault())) {
      return writeContext.getResponseObject()
          .getSummary()
          .getSchema()
          .getDefault();
    }

    if (writeContext.isSchemaRequiredNonNillable()) {
      throw mappingException(String.format(
          "Could not create response: required and non-nillable property '%s' expression evaluation returned null.",
          writeContext.getResponseObject()
              .getIdentifier()));
    }

    return null;
  }

  private Object getScalarDefaultValue(@NonNull ResponseWriteContext writeContext, SchemaSummary summary) {
    Object defaultValue = getDwsExtension(summary.getSchema(), OasConstants.X_DWS_DEFAULT);

    String oasType = summary.getSchema()
        .getType();
    Class<?> typeClass = TYPE_CLASS_MAPPING.get(oasType);
    if (typeClass != null && typeClass.isAssignableFrom(defaultValue.getClass())) {
      return defaultValue;
    }

    throw mappingException("'{}' value for property '{}' not of type '{}'", OasConstants.X_DWS_DEFAULT,
        writeContext.getResponseObject()
            .getIdentifier(),
        summary.getSchema()
            .getType());

  }

  private Object mapEnvelopeObjectToResponse(ResponseWriteContext parentContext, String path) {
    Map<String, Object> result = new HashMap<>();
    unwrapChildSchema(parentContext).forEach(child -> addDataToResponse(path, result, child.getResponseObject()
        .getIdentifier(), child));

    // for a composed envelope schema, we need to merge the underlying schema's into one result
    unwrapComposedSchema(parentContext).forEach(child -> {
      String identifier = child.getResponseObject()
          .getIdentifier();
      mergeComposedResponse(path, result, child, identifier);
    });

    return createResponseObject(result, parentContext, path);
  }

  @SuppressWarnings("unchecked")
  private void mergeComposedResponse(String path, Map<String, Object> result, ResponseWriteContext child,
      String identifier) {
    /*
     * allOf schemas are merged so that the parent object has the combined propertyset of the distinct
     * composed schemas directly underneath.
     */
    Map<String, Object> childResponse = (Map<String, Object>) mapDataToResponse(child, path);

    if ((result.containsKey(identifier))) {
      ((Map<String, Object>) result.get(identifier)).putAll(childResponse);
    } else {
      addDataToResponse(path, result, identifier, child);
    }
  }

  private Object convertType(ResponseWriteContext writeContext, Object item) {
    if (Objects.isNull(item)) {
      return null;
    }

    return typeConverterRouter.convert(item, writeContext.getParameters());
  }

  private void validateRequiredProperties(ResponseWriteContext context, String path, Map<String, Object> data) {
    List<ResponseWriteContext> responseWriteContexts;
    if (context.isComposedOf()) {
      responseWriteContexts = unwrapComposedSchema(context);
    } else {
      responseWriteContexts = unwrapChildSchema(context);
    }
    responseWriteContexts.forEach(writeContext -> {
      String childIdentifier = writeContext.getResponseObject()
          .getIdentifier();
      boolean isExpanded = isExpanded(context.getParameters(), childPath(path, childIdentifier));
      if (isRequiredOrExpandedAndNullOrEmpty(writeContext, data.get(childIdentifier), isExpanded)
          && !writeContext.getResponseObject()
              .getSummary()
              .isNillable()) {
        throw mappingException(
            "Could not map GraphQL response: Required and non-nillable "
                + "property '{}' was not returned in GraphQL response.",
            writeContext.getResponseObject()
                .getIdentifier());
      }
    });
  }

  private Object mapObject(ResponseWriteContext writeContext, Object object, boolean isExpanded) {
    if (isRequiredOrExpandedAndNullOrEmpty(writeContext, object, isExpanded) && writeContext.getResponseObject()
        .getSummary()
        .isNillable()) {
      return null;
    }

    return object;
  }

  @SuppressWarnings("unchecked")
  private Optional<String> evaluateJexl(ResponseWriteContext writeContext) {
    JexlContext context =
        JexlHelper.getJexlContext(null, writeContext.getParameters(), writeContext.getGraphQlField(), null);

    // add object data to context
    writeContext.getParameters()
        .forEach((key1, value1) -> context.set("input." + key1, value1));

    context.set("data", writeContext.getData());

    StringBuilder fieldsBuilder = new StringBuilder("fields.");
    writeContext.getDataStack()
        .stream()
        .map(FieldContext::getData)
        .forEach(data -> {
          ((Map<String, Object>) data).entrySet()
              .stream()
              .filter(entry -> !(entry.getValue() instanceof Map))
              .forEach(entry -> context.set(fieldsBuilder.toString() + entry.getKey(), entry.getValue()));
          fieldsBuilder.append("_parent.");
        });

    StringBuilder argsBuilder = new StringBuilder("args.");
    writeContext.getDataStack()
        .stream()
        .map(FieldContext::getInput)
        .forEach(input -> {
          input.forEach((key, value) -> context.set(argsBuilder.toString() + key, value));
          argsBuilder.append("_parent.");
        });

    // add uri to context
    String path = writeContext.getUri()
        .getPath();
    String uriString = writeContext.getUri()
        .toString();
    int pathIdx = uriString.indexOf(path);
    context.set("request.uri", uriString.substring(pathIdx));

    // add properties data to context
    this.properties.getAllProperties()
        .forEach((key, value) -> context.set("env." + key, value));

    Map<String, String> dwsExprMap = writeContext.getResponseObject()
        .getSummary()
        .getDwsExpr();
    return jexlHelper.evaluateScriptWithFallback(dwsExprMap.get(X_DWS_EXPR_VALUE),
        dwsExprMap.get(X_DWS_EXPR_FALLBACK_VALUE), context, String.class);
  }

  private String addToPath(String path, ResponseObject responseObject, boolean canAddArray) {
    if ((!Objects.equals(ARRAY_TYPE, responseObject.getSummary()
        .getType()) || canAddArray) && (!responseObject.getSummary()
            .isTransient() || isDefaultValue(responseObject.getSummary()))) {
      return getPathString(path, responseObject);
    }
    return path;
  }

  private boolean isDefaultValue(@NonNull SchemaSummary schemaSummary) {
    Schema<?> schema = schemaSummary.getSchema();

    if (schema == null) {
      return false;
    }

    return isTransient(schema) && getDwsExtension(schema, OasConstants.X_DWS_EXPR) == null
        && getDwsExtension(schema, OasConstants.X_DWS_DEFAULT) != null;
  }
}
