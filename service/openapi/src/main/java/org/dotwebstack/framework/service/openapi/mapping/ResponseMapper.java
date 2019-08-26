package org.dotwebstack.framework.service.openapi.mapping;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.mappingException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.noResultFoundException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.ARRAY_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.OBJECT_TYPE;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.createNewResponseWriteContext;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.unwrapData;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.unwrapSchema;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.unwrapSchemaAndListData;

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
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.core.helpers.JexlHelper;
import org.dotwebstack.framework.service.openapi.conversion.TypeConverterRouter;
import org.dotwebstack.framework.service.openapi.exception.NoResultFoundException;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
import org.dotwebstack.framework.service.openapi.response.ResponseWriteContext;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

@Component
public class ResponseMapper {

  private final ObjectMapper objectMapper;

  private final JexlHelper jexlHelper;

  private final EnvironmentProperties properties;

  private final TypeConverterRouter typeConverterRouter;

  public ResponseMapper(Jackson2ObjectMapperBuilder objectMapperBuilder, JexlEngine jexlEngine,
      EnvironmentProperties properties, TypeConverterRouter typeConverterRouter) {
    this.objectMapper = objectMapperBuilder.build();
    this.jexlHelper = new JexlHelper(jexlEngine);
    this.properties = properties;
    this.typeConverterRouter = typeConverterRouter;
  }

  public String toJson(@NonNull ResponseWriteContext writeContext)
      throws JsonProcessingException, NoResultFoundException {
    Object response = mapDataToResponse(writeContext);
    if (Objects.isNull(response)) {
      throw noResultFoundException("Did not find data for your response.");
    }
    return toJson(response);
  }

  private String toJson(Object object) throws JsonProcessingException {
    return this.objectMapper.writer()
        .writeValueAsString(object);
  }

  private Object mapDataToResponse(@NonNull ResponseWriteContext writeContext) {
    switch (writeContext.getSchema()
        .getType()) {
      case ARRAY_TYPE:
        return mapArrayDataToResponse(writeContext);
      case OBJECT_TYPE:
        return mapObjectDataToResponse(writeContext);
      default:
        return mapScalarDataToResponse(writeContext);
    }
  }

  private Object mapScalarDataToResponse(@NonNull ResponseWriteContext writeContext) {
    if (Objects.nonNull(writeContext.getSchema()
        .getDwsTemplate())) {
      Optional<String> evaluated = evaluateJexl(writeContext);
      if (!evaluated.isPresent() && writeContext.isSchemaRequiredNonNillable()) {
        throw new MappingException(String.format(
            "Could not create response: required and non-nillable property '%s' template evaluation returned null.",
            writeContext.getSchema()
                .getIdentifier()));
      } else if (evaluated.isPresent()) {
        return evaluated.get();
      }
      return null;
    }
    return writeContext.getData();
  }

  @SuppressWarnings("unchecked")
  private Object mapObjectDataToResponse(@NonNull ResponseWriteContext parentContext) {
    if (Objects.isNull(parentContext.getData())) {
      return null;
    }

    Map<String, Object> result = new HashMap<>();
    parentContext.getSchema()
        .getChildren()
        .forEach(child -> {
          ResponseWriteContext writeContext = createNewResponseWriteContext(parentContext, child);
          Object object;
          if (child.isEnvelope()) {
            object = mapEnvelopeObject(writeContext);
            if (Objects.nonNull(object)) {
              result.put(child.getIdentifier(), convertType(writeContext, object));
            }
          } else {
            if (writeContext.getData() instanceof Map) {
              object = mapObject(writeContext, child);
              if (!(Objects.isNull(object))) {
                result.put(child.getIdentifier(), convertType(writeContext, object));
              }
            } else if (writeContext.getData() instanceof List) {
              ((List) writeContext.getData()).stream()
                  .forEach(item -> {
                    ResponseWriteContext itemContext = createNewResponseWriteContext(writeContext, item);
                    Object itemObject = mapObject(itemContext);
                    if (!(Objects.isNull(itemObject))) {
                      result.put(child.getIdentifier(), convertType(itemContext, itemObject));
                    }
                  });
            } else {
              throw invalidConfigurationException("Cannot map {} to response, it is of the wrong type",
                  child.getIdentifier());
            }
          }
        });
    return result;
  }

  private Object convertType(ResponseWriteContext writeContext, Object item) {
    return Objects.nonNull(writeContext.getSchema()
        .getDwsType()) ? typeConverterRouter.convert(item, writeContext.getParameters()) : item;
  }

  @SuppressWarnings("unchecked")
  private Object mapArrayDataToResponse(@NonNull ResponseWriteContext writeContext) {
    if (Objects.isNull(writeContext.getData())) {
      return Collections.emptyList();
    }

    ResponseObject childResponseObject = writeContext.getSchema()
        .getItems()
        .get(0);

    return ((List<Object>) writeContext.getData()).stream()
        .map(childData -> {
          List<Object> dataStack = writeContext.getDataStack();
          dataStack.add(0, childData);

          ResponseWriteContext childWriteContext = ResponseWriteContext.builder()
              .schema(childResponseObject)
              .data(childData)
              .parameters(writeContext.getParameters())
              .dataStack(dataStack)
              .build();

          return mapDataToResponse(childWriteContext);
        })
        .collect(Collectors.toList());
  }

  private Object mapObject(ResponseWriteContext parentContext, ResponseObject child) {
    return mapObject(unwrapData(parentContext, child));
  }

  private Object mapObject(ResponseWriteContext writeContext) {
    Object object = mapDataToResponse(writeContext);
    if (isRequiredAndNullOrEmpty(writeContext, object)) {
      if (writeContext.getSchema()
          .isNillable()) {
        return null;
      }
      throw mappingException(
          "Could not map GraphQL response: Required and non-nillable "
              + "property '{}' was not returned in GraphQL response.",
          writeContext.getSchema()
              .getIdentifier());
    }

    return object;
  }

  @SuppressWarnings("unchecked")
  private Optional<String> evaluateJexl(ResponseWriteContext writeContext) {
    MapContext context = new MapContext();

    // add object data to context
    StringBuilder builder = new StringBuilder("fields.");
    writeContext.getDataStack()
        .forEach(data -> {
          ((Map<String, Object>) data).entrySet()
              .stream()
              .filter(entry -> !(entry.getValue() instanceof Map))
              .forEach(entry -> context.set(builder.toString() + entry.getKey(), entry.getValue()));

          builder.append("_parent.");
        });

    // add properties data to context
    this.properties.getAllProperties()
        .forEach((key, value) -> context.set("env." + key, value));

    return jexlHelper.evaluateExpression(writeContext.getSchema()
        .getDwsTemplate(), context, String.class);
  }

  private boolean isRequiredAndNullOrEmpty(ResponseWriteContext writeContext, Object object) {
    return writeContext.getSchema()
        .isRequired() && ((Objects.isNull(object)) || isEmptyList(writeContext.getSchema(), object));
  }

  private boolean isEmptyList(ResponseObject responseObject, Object object) {
    if (responseObject.isNillable() && object instanceof List) {
      return ((List) object).isEmpty();
    }
    return false;
  }

  private boolean isFilledList(Object object) {
    if (object instanceof List) {
      return !((List) object).isEmpty();
    }
    return false;
  }

  @SuppressWarnings("rawtypes")
  private Object mapEnvelopeObject(ResponseWriteContext parentContext) {
    if (Objects.nonNull(parentContext.getData())) {
      if (isFilledList(parentContext.getData())) {
        return mapDataToResponse(unwrapSchema(parentContext));
      }

      ResponseObject embedded = parentContext.getSchema()
          .getChildren()
          .get(0);
      if (parentContext.getData() instanceof Map
          && ((Map) parentContext.getData()).containsKey(embedded.getIdentifier())) {

        List childData = (List) ((Map) parentContext.getData()).get(embedded.getIdentifier());
        if (!childData.isEmpty()) {
          return mapDataToResponse(unwrapSchemaAndListData(parentContext));
        }
      }
    }
    throw invalidConfigurationException("Unable to map envelope object '{}'", parentContext.getSchema()
        .getIdentifier());
  }
}
