package org.dotwebstack.framework.service.openapi.mapping;

import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.mappingException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.notFoundException;
import static org.dotwebstack.framework.service.openapi.mapping.ResponseMapperHelper.isRequiredOrExpandedAndNullOrEmpty;
import static org.dotwebstack.framework.service.openapi.response.ResponseContextHelper.getPathString;
import static org.dotwebstack.framework.service.openapi.response.ResponseContextHelper.isExpanded;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.createObjectContext;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.createResponseContextFromChildData;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.createResponseWriteContextFromChildSchema;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.unpackCollectionData;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.unwrapItemSchema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.service.openapi.conversion.TypeConverterRouter;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
import org.dotwebstack.framework.service.openapi.response.FieldContext;
import org.dotwebstack.framework.service.openapi.response.ResponseWriteContext;
import org.dotwebstack.framework.service.openapi.response.oas.OasArrayField;
import org.dotwebstack.framework.service.openapi.response.oas.OasField;
import org.dotwebstack.framework.service.openapi.response.oas.OasObjectField;
import org.dotwebstack.framework.service.openapi.response.oas.OasScalarExpressionField;
import org.dotwebstack.framework.service.openapi.response.oas.OasScalarField;
import org.dotwebstack.framework.service.openapi.response.oas.OasType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JsonResponseMapper {

  private static final Map<String, Class<?>> TYPE_CLASS_MAPPING =
      Map.of("string", String.class, "integer", Integer.class);

  private final ObjectMapper objectMapper;

  private final JexlHelper jexlHelper;

  private final EnvironmentProperties properties;

  private final TypeConverterRouter typeConverterRouter;

  private final boolean pagingEnabled;

  public JsonResponseMapper(Jackson2ObjectMapperBuilder objectMapperBuilder, JexlEngine jexlEngine,
      EnvironmentProperties properties, TypeConverterRouter typeConverterRouter, Schema schema) {
    this.objectMapper = objectMapperBuilder.build();
    this.jexlHelper = new JexlHelper(jexlEngine);
    this.properties = properties;
    this.typeConverterRouter = typeConverterRouter;
    this.pagingEnabled = schema.usePaging();
  }

  public Mono<String> toResponse(@NonNull Object input) {
    if (input instanceof ResponseWriteContext) {
      try {
        return Mono.just(toJson((ResponseWriteContext) input));
      } catch (JsonProcessingException jpe) {
        throw new MappingException("An exception occurred when serializing to JSON.", jpe);
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
    var oasField = writeContext.getOasField();

    String newPath = addToPath(path, writeContext.getOasField(), writeContext.getIdentifier(), false);

    switch (oasField.getType()) {
      case ARRAY:
        if ((oasField.isRequired() || isExpanded(writeContext.getParameters(),
            removeRoot(addToPath(newPath, oasField, writeContext.getIdentifier(), true))))) {
          if (oasField.hasDefault()) {
            return mapDefaultArrayToResponse((OasArrayField) oasField, path);
          }
          return mapArrayDataToResponse(writeContext, newPath);
        }
        return null;
      case OBJECT:
        var object = processObject(writeContext, (OasObjectField) oasField, newPath);
        return isObjectIncluded(object, (OasObjectField) oasField) ? object : null;
      case SCALAR:
        if (oasField.isRequired() || isExpanded(writeContext.getParameters(), removeRoot(newPath))) {
          return mapScalarDataToResponse(writeContext, newPath);
        }
        break;
      case SCALAR_EXPRESSION:
        if (oasField.isRequired() || isExpanded(writeContext.getParameters(), removeRoot(newPath))) {
          return mapScalarDataExpressionToResponse(writeContext, newPath);
        }
        break;
      default:
        break;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private boolean isObjectIncluded(Object objectResult, OasObjectField oasField) {
    if (oasField.getIncludeExpression() != null && objectResult instanceof Map) {
      JexlContext jexlContext = new MapContext();
      ((Map<String, Object>) objectResult).forEach(jexlContext::set);

      return jexlHelper.evaluateScript(oasField.getIncludeExpression(), jexlContext, Boolean.class)
          .orElse(true);
    }

    return true;
  }

  private Object mapDefaultArrayToResponse(OasArrayField field, String path) {
    Object defaultValue = field.getDefaultValue();

    if (defaultValue instanceof List) {
      return defaultValue;
    } else if (defaultValue != null) {
      throw mappingException("'{}' value for property '{}' not of type array!", OasConstants.X_DWS_DEFAULT, path);
    } else if (field.isNillable()) {
      return null;
    }
    return List.of();
  }

  private Object processObject(@NonNull ResponseWriteContext writeContext, OasObjectField oasField, String newPath) {

    if (oasField.isRequired() || oasField.isDwsTransient()
        || isExpanded(writeContext.getParameters(), removeRoot(newPath)) || oasField.getIncludeExpression() != null) {
      if (oasField.isEnvelope()) {
        return mapEnvelopeObjectToResponse(writeContext, newPath);
      }
      return mapObjectDataToResponse(writeContext, newPath);
    }
    return null;
  }

  private Map<String, Object> createResponseObject(Map<String, Object> result, ResponseWriteContext parentContext,
      String path) {
    if (result.isEmpty()) {
      return null;
    }
    List<ResponseWriteContext> children = createObjectContext(parentContext, pagingEnabled);

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
    Object data = unpackCollectionData(parentContext.getData(), parentContext.getOasField(), pagingEnabled);
    if (Objects.isNull(data)) {
      return mapDefaultArrayToResponse((OasArrayField) parentContext.getOasField(), path);
    } else if (data instanceof List) {
      return ((List<Object>) data).stream()
          .map(childData -> mapDataToResponse(createResponseContextFromChildData(parentContext, childData), path))
          .collect(Collectors.toList());
    }
    return mapDataToResponse(unwrapItemSchema(parentContext), path);
  }

  private Object mapObjectDataToResponse(@NonNull ResponseWriteContext parentContext, String path) {
    if (Objects.isNull(parentContext.getData())) {
      return null;
    }

    if (Objects.nonNull(parentContext.getOasField()
        .getDwsType())) {
      return parentContext.getData();
    }

    OasObjectField oasObjectField = (OasObjectField) parentContext.getOasField();
    if (oasObjectField.getFields()
        .isEmpty()) {
      return parentContext.getData();
    }

    Map<String, Object> result = new HashMap<>();
    oasObjectField.getFields()
        .forEach((identifier, field) -> {
          var writeContext = createResponseWriteContextFromChildSchema(parentContext, identifier, field);
          addDataToResponse(path, result, identifier, writeContext);
        });

    return createResponseObject(result, parentContext, path);
  }

  private void addDataToResponse(String path, Map<String, Object> response, String identifier,
      ResponseWriteContext writeContext) {
    boolean isExpanded = isExpanded(writeContext.getParameters(), childPath(path, identifier));
    var object = mapObject(writeContext, mapDataToResponse(writeContext, path), isExpanded);

    if (Objects.nonNull(object) || (writeContext.isSchemaRequiredNillable() || isExpanded)) {
      response.put(identifier, convertType(writeContext, object));
    }
  }

  private String childPath(String path, String identifier) {
    String newPath = removeRoot(path);
    return newPath.length() > 0 ? newPath + "." + identifier : identifier;
  }

  Object mapScalarDataToResponse(@NonNull ResponseWriteContext writeContext, String path) {

    OasField field = writeContext.getOasField();

    if (field.hasDefault()) {
      return getScalarDefaultValue(field, path);
    }
    return writeContext.getData();
  }

  Object mapScalarDataExpressionToResponse(@NonNull ResponseWriteContext writeContext, String path) {
    OasScalarExpressionField field = (OasScalarExpressionField) writeContext.getOasField();

    if (field.hasDefault()) {
      return getScalarDefaultValue(field, path);
    }

    Optional<String> evaluated = evaluateJexl(writeContext);
    if (evaluated.isPresent()) {
      return evaluated.get();
    } else if (field.getDefaultValue() != null) {
      return field.getDefaultValue();
    }

    if (writeContext.isSchemaRequiredNonNillable()) {
      throw mappingException(String.format(
          "Could not create response: required and non-nillable property '%s' expression evaluation returned null.",
          path));
    }

    return null;
  }

  private Object getScalarDefaultValue(OasField oasField, String path) {
    Object defaultValue = oasField.getDefaultValue();
    String oasType;
    if (oasField.getType() == OasType.SCALAR) {
      oasType = ((OasScalarField) oasField).getScalarType();
    } else {
      oasType = ((OasScalarExpressionField) oasField).getScalarType();
    }

    Class<?> typeClass = TYPE_CLASS_MAPPING.get(oasType);
    if (typeClass != null && typeClass.isAssignableFrom(defaultValue.getClass())) {
      return defaultValue;
    }

    throw mappingException("'{}' value for property '{}' not of type '{}'", OasConstants.X_DWS_DEFAULT, path, oasType);

  }

  private Object mapEnvelopeObjectToResponse(ResponseWriteContext parentContext, String path) {
    Map<String, Object> result = new HashMap<>();

    List<ResponseWriteContext> childContexts = createObjectContext(parentContext, pagingEnabled);
    childContexts.forEach(child -> addDataToResponse(path, result, child.getIdentifier(), child));

    return createResponseObject(result, parentContext, path);
  }

  private Object convertType(ResponseWriteContext writeContext, Object item) {
    if (Objects.isNull(item)) {
      return null;
    }

    return typeConverterRouter.convert(item, writeContext.getParameters());
  }

  private void validateRequiredProperties(ResponseWriteContext context, String path, Map<String, Object> data) {
    List<ResponseWriteContext> responseWriteContexts = createObjectContext(context, pagingEnabled);

    responseWriteContexts.forEach(writeContext -> {
      String childIdentifier = writeContext.getIdentifier();
      boolean isExpanded = isExpanded(context.getParameters(), childPath(path, childIdentifier));
      if (isRequiredOrExpandedAndNullOrEmpty(writeContext, data.get(childIdentifier), isExpanded)
          && !writeContext.getOasField()
              .isNillable()) {
        throw mappingException("Could not map GraphQL response: Required and non-nillable "
            + "property '{}' was not returned in GraphQL response.", writeContext.getIdentifier());
      }
    });
  }

  private Object mapObject(ResponseWriteContext writeContext, Object object, boolean isExpanded) {
    if (isRequiredOrExpandedAndNullOrEmpty(writeContext, object, isExpanded) && writeContext.getOasField()
        .isNillable()) {
      return null;
    }

    return object;
  }

  @SuppressWarnings("unchecked")
  private Optional<String> evaluateJexl(ResponseWriteContext writeContext) {
    var context = JexlHelper.getJexlContext(null, writeContext.getParameters(), null);

    // add object data to context
    writeContext.getParameters()
        .forEach((key1, value1) -> context.set("input." + key1, value1));

    context.set("data", writeContext.getData());

    var fieldsBuilder = new StringBuilder("fields.");
    writeContext.getDataStack()
        .stream()
        .map(FieldContext::getData)
        .forEach(data -> {
          ((Map<String, Object>) data).entrySet()
              .stream()
              .filter(entry -> !(entry.getValue() instanceof Map))
              .forEach(entry -> context.set(fieldsBuilder + entry.getKey(), entry.getValue()));
          fieldsBuilder.append("_parent.");
        });

    var argsBuilder = new StringBuilder("args.");
    writeContext.getDataStack()
        .stream()
        .map(FieldContext::getInput)
        .forEach(input -> {
          input.forEach((key, value) -> context.set(argsBuilder + key, value));
          argsBuilder.append("_parent.");
        });

    // add uri to context
    var path = writeContext.getUri()
        .getPath();
    var uriString = writeContext.getUri()
        .toString();
    int pathIdx = uriString.indexOf(path);
    context.set("request.uri", uriString.substring(pathIdx));

    // add properties data to context
    this.properties.getAllProperties()
        .forEach((key, value) -> context.set("env." + key, value));

    OasScalarExpressionField field = (OasScalarExpressionField) writeContext.getOasField();
    return jexlHelper.evaluateScriptWithFallback(field.getExpression(), field.getFallbackValue(), context,
        String.class);
  }

  private String addToPath(String path, OasField oasField, String identifier, boolean canAddArray) {
    if ((!oasField.isArray() || canAddArray) && (!oasField.isTransient() || oasField.hasDefault())) {
      return getPathString(path, identifier);
    }
    return path;
  }
}
