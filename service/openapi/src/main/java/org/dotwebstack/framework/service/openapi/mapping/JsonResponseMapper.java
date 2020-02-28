package org.dotwebstack.framework.service.openapi.mapping;

import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.mappingException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.noResultFoundException;
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
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.unwrapItemSchema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
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
import org.dotwebstack.framework.service.openapi.response.FieldContext;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
import org.dotwebstack.framework.service.openapi.response.ResponseWriteContext;
import org.dotwebstack.framework.service.openapi.response.SchemaSummary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

@Component
public class JsonResponseMapper {

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
      throw noResultFoundException("Did not find data for your response.");
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
        if (summary.isRequired()
            || isExpanded(writeContext.getParameters(), removeRoot(addToPath(newPath, responseObject, true)))) {
          return mapArrayDataToResponse(writeContext, newPath);
        }
        return null;
      case OBJECT_TYPE:
        return processObject(writeContext, summary, newPath);
      default:
        if (summary.isRequired() || Objects.nonNull(summary.getDwsExpr())
            || isExpanded(writeContext.getParameters(), removeRoot(newPath))) {
          return mapScalarDataToResponse(writeContext);
        }
        return null;
    }
  }

  private Object processObject(@NonNull ResponseWriteContext writeContext, SchemaSummary summary, String newPath) {
    if (summary.isRequired() || summary.isEnvelope() || isExpanded(writeContext.getParameters(), removeRoot(newPath))) {
      if (summary.isEnvelope()) {
        return mapEnvelopeObjectToResponse(writeContext, newPath);
      }
      if (!writeContext.getResponseObject()
          .getSummary()
          .getComposedOf()
          .isEmpty()) {
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

    Map<String, Object> results = new HashMap<>();
    parentContext.getResponseObject()
        .getSummary()
        .getComposedOf()
        .stream()
        .map(composedSchema -> {
          ResponseWriteContext writeContext = copyResponseContext(parentContext, composedSchema);
          return ((Map<String, Object>) mapDataToResponse(writeContext, path));
        })
        .filter(Objects::nonNull)
        .forEach(map -> map.forEach(results::put));

    return results;
  }

  String removeRoot(String path) {
    if (path.contains(".")) {
      return path.substring(path.indexOf('.') + 1);
    }
    return "";
  }

  @SuppressWarnings("unchecked")
  private Object mapArrayDataToResponse(ResponseWriteContext parentContext, String path) {
    if (Objects.isNull(parentContext.getData()) && parentContext.getResponseObject()
        .getSummary()
        .isNillable()) {
      return null;
    } else if (Objects.isNull(parentContext.getData())) {
      return Collections.emptyList();
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

    Map<String, Object> result = new HashMap<>();
    parentContext.getResponseObject()
        .getSummary()
        .getChildren()
        .forEach(childSchema -> {
          ResponseWriteContext writeContext = createResponseWriteContextFromChildSchema(parentContext, childSchema);

          boolean isExpanded = isExpanded(writeContext.getParameters(), childPath(path, childSchema.getIdentifier()));
          Object object = mapObject(writeContext, mapDataToResponse(writeContext, path), isExpanded);

          if (Objects.nonNull(object) || writeContext.isSchemaRequiredNillable() || isExpanded) {
            result.put(childSchema.getIdentifier(), convertType(writeContext, object));
          }
        });
    return result;
  }

  private String childPath(String path, String identifier) {
    String newPath = removeRoot(path);
    return newPath.length() > 0 ? newPath + "." + identifier : identifier;
  }

  private Object mapScalarDataToResponse(@NonNull ResponseWriteContext writeContext) {
    if (Objects.isNull(writeContext.getResponseObject()
        .getSummary()
        .getDwsExpr())) {
      return writeContext.getData();
    }

    Optional<String> evaluated = evaluateJexl(writeContext);
    if (evaluated.isPresent()) {
      return evaluated.get();
    }

    if (writeContext.isSchemaRequiredNonNillable()) {
      throw mappingException(String.format(
          "Could not create response: required and non-nillable property '%s' expression evaluation returned null.",
          writeContext.getResponseObject()
              .getIdentifier()));
    }

    return null;
  }

  @SuppressWarnings("rawtypes")
  private Object mapEnvelopeObjectToResponse(ResponseWriteContext parentContext, String path) {
    Map<String, Object> result = new HashMap<>();
    unwrapChildSchema(parentContext).forEach(child -> {
      Object object = mapDataToResponse(child, path);
      result.put(child.getResponseObject()
          .getIdentifier(), object);
    });
    return result;
  }

  private Object convertType(ResponseWriteContext writeContext, Object item) {
    if (Objects.isNull(item)) {
      return null;
    }

    return typeConverterRouter.convert(item, writeContext.getParameters());
  }

  private Object mapObject(ResponseWriteContext writeContext, Object object, boolean isExpanded) {
    if (isRequiredOrExpandedAndNullOrEmpty(writeContext, object, isExpanded)) {
      if (writeContext.getResponseObject()
          .getSummary()
          .isNillable()) {
        return null;
      } else {
        throw mappingException(
            "Could not map GraphQL response: Required and non-nillable "
                + "property '{}' was not returned in GraphQL response.",
            writeContext.getResponseObject()
                .getIdentifier());
      }
    }
    return object;
  }

  @SuppressWarnings("unchecked")
  private Optional<String> evaluateJexl(ResponseWriteContext writeContext) {
    JexlContext context = JexlHelper.getJexlContext(null, writeContext.getParameters(), writeContext.getGraphQlField());

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
    return jexlHelper.evaluateScript(dwsExprMap.get(X_DWS_EXPR_VALUE), dwsExprMap.get(X_DWS_EXPR_FALLBACK_VALUE),
        context, String.class);
  }

  private String addToPath(String path, ResponseObject responseObject, boolean canAddArray) {
    if ((!Objects.equals(ARRAY_TYPE, responseObject.getSummary()
        .getType()) || canAddArray) && !responseObject.getSummary()
            .isEnvelope()) {
      return getPathString(path, responseObject);
    }
    return path;
  }
}
