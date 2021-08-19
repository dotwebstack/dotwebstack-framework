package org.dotwebstack.framework.service.openapi.response;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
import org.dotwebstack.framework.service.openapi.response.oas.OasArrayField;
import org.dotwebstack.framework.service.openapi.response.oas.OasField;
import org.dotwebstack.framework.service.openapi.response.oas.OasObjectField;

public class ResponseWriteContextHelper {

  private ResponseWriteContextHelper() {}

  public static List<ResponseWriteContext> createObjectContext(@NonNull ResponseWriteContext parentContext,
                                                               boolean pagingEnabled) {
    OasObjectField oasObjectField = (OasObjectField)parentContext.getOasField();
    return oasObjectField.getFields().entrySet().stream().map(e->{
      String identifier = e.getKey();
      OasField child = e.getValue();
      return unwrapSubSchema(parentContext,identifier, child, pagingEnabled);
    }).collect(Collectors.toList());
  }


  private static ResponseWriteContext unwrapSubSchema(ResponseWriteContext parentContext, String childIdentifier, OasField child,
      boolean pagingEnabled) {
    Object data = unpackCollectionData(parentContext.getData(), child, pagingEnabled);
    Deque<FieldContext> dataStack = new ArrayDeque<>(parentContext.getDataStack());

    if (!child.isTransient()
        && data instanceof Map) {
      data = ((Map<?, ?>) data).get(childIdentifier);
      dataStack = createNewDataStack(dataStack, data, Collections.emptyMap());
    }

    return createNewResponseWriteContext(child,childIdentifier, data, parentContext.getParameters(), dataStack, parentContext.getUri());
  }

  public static ResponseWriteContext unwrapItemSchema(@NonNull ResponseWriteContext parentContext) {
    OasArrayField arrayField = (OasArrayField) parentContext.getOasField();
    return createNewResponseWriteContext(arrayField.getContent(),parentContext.getIdentifier(), parentContext.getData(), parentContext.getParameters(),
        parentContext.getDataStack(), parentContext.getUri());
  }

  public static Deque<FieldContext> createNewDataStack(@NonNull Deque<FieldContext> previousDataStack, Object newData,
      Map<String, Object> newInput) {
    Deque<FieldContext> dataStack = new ArrayDeque<>(previousDataStack);
    if (newData instanceof Map) {
      dataStack.push(createFieldContext(newData, newInput));
    }
    return dataStack;
  }

  public static FieldContext createFieldContext(Object newData, Map<String, Object> newInput) {
    return FieldContext.builder()
        .data(newData)
        .input(newInput)
        .build();
  }

  public static ResponseWriteContext createResponseWriteContextFromChildSchema(
      @NonNull ResponseWriteContext parentContext, @NonNull String identifier, @NonNull OasField oasField) {
    Deque<FieldContext> dataStack = new ArrayDeque<>(parentContext.getDataStack());
    Object data = parentContext.getData();

    if (!oasField.isTransient()) {
      if (!parentContext.getDataStack()
          .isEmpty()) {
        data = ((Map) parentContext.getDataStack()
            .peek()
            .getData()).get(identifier);
        dataStack = createNewDataStack(parentContext.getDataStack(), data, Collections.emptyMap());
        return createNewResponseWriteContext(oasField,identifier, data, parentContext.getParameters(), dataStack,
            parentContext.getUri());
      }

      if (data instanceof Map) {
        data = ((Map) data).get(identifier);
      }
    }

    return createNewResponseWriteContext(oasField, identifier, data, parentContext.getParameters(), dataStack,
        parentContext.getUri());
  }

  public static ResponseWriteContext createResponseContextFromChildData(@NonNull ResponseWriteContext parentContext,
      @NonNull Object childData) {
    Deque<FieldContext> dataStack = createNewDataStack(parentContext.getDataStack(), childData, Collections.emptyMap());
    return createNewResponseWriteContext(parentContext.getOasField(), parentContext.getIdentifier(), childData, parentContext.getParameters(),
        dataStack, parentContext.getUri());
  }


  public static ResponseWriteContext createNewResponseWriteContext(@NonNull OasField oasField, @NonNull String identifier, Object data,
                                                                   Map<String, Object> parameters, @NonNull Deque<FieldContext> dataStack, URI uri) {
    return ResponseWriteContext.builder()
        .identifier(identifier)
        .oasField(oasField)
        .data(data)
        .parameters(parameters)
        .dataStack(dataStack)
        .uri(uri)
        .build();
  }

  @SuppressWarnings("unchecked")
  public static Object unpackCollectionData(Object data, OasField field, boolean pagingEnabled) {
    if (pagingEnabled && data instanceof Map && field.isArray()) {
      Map<String, ?> dataMap = (Map<String, ?>) data;
      return dataMap.containsKey("nodes") ? dataMap.get("nodes") : dataMap;
    }
    return data;
  }

}
