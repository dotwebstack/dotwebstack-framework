package org.dotwebstack.framework.service.openapi.response;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import lombok.NonNull;

public class ResponseWriteContextHelper {

  private ResponseWriteContextHelper() {}

  public static ResponseWriteContext unwrapChildSchema(@NonNull ResponseWriteContext parentContext) {
    ResponseObject childSchema = parentContext.getSchema()
        .getChildren()
        .get(0);

    Object data = parentContext.getData();
    Deque<Object> dataStack = new ArrayDeque<>(parentContext.getDataStack());

    if (!childSchema.isEnvelope() && data instanceof Map) {
      data = ((Map) data).get(childSchema.getIdentifier());
      dataStack = createNewDataStack(dataStack, data);
    }

    return createNewResponseWriteContext(childSchema, data, parentContext.getParameters(), dataStack);
  }

  public static ResponseWriteContext unwrapItemSchema(@NonNull ResponseWriteContext parentContext) {
    ResponseObject childSchema = parentContext.getSchema()
        .getItems()
        .get(0);
    return createNewResponseWriteContext(childSchema, parentContext.getData(), parentContext.getParameters(),
        parentContext.getDataStack());
  }

  public static Deque<Object> createNewDataStack(@NonNull Deque<Object> previousDataStack, Object newEntry) {
    Deque<Object> dataStack = new ArrayDeque<>(previousDataStack);
    if (newEntry instanceof Map) {
      dataStack.push(newEntry);
    }
    return dataStack;
  }

  public static ResponseWriteContext createResponseWriteContextFromChildSchema(
      @NonNull ResponseWriteContext parentContext, @NonNull ResponseObject childSchema) {
    Deque<Object> dataStack = new ArrayDeque<>(parentContext.getDataStack());
    Object data = parentContext.getData();

    if (!childSchema.isEnvelope()) {
      if (!parentContext.getDataStack()
          .isEmpty()) {
        data = ((Map) parentContext.getDataStack()
            .peek()).get(childSchema.getIdentifier());
        dataStack = createNewDataStack(parentContext.getDataStack(), data);
        return createNewResponseWriteContext(childSchema, data, parentContext.getParameters(), dataStack);
      }

      if (data instanceof Map) {
        data = ((Map) data).get(childSchema.getIdentifier());
      }
    }

    return createNewResponseWriteContext(childSchema, data, parentContext.getParameters(), dataStack);
  }

  public static ResponseWriteContext createResponseContextFromChildData(@NonNull ResponseWriteContext parentContext,
      @NonNull Object childData) {
    Deque<Object> dataStack = createNewDataStack(parentContext.getDataStack(), childData);
    return createNewResponseWriteContext(parentContext.getSchema(), childData, parentContext.getParameters(),
        dataStack);
  }

  public static ResponseWriteContext createNewResponseWriteContext(@NonNull ResponseObject schema, Object data,
      @NonNull Map<String, Object> parameters, @NonNull Deque<Object> dataStack) {
    return ResponseWriteContext.builder()
        .schema(schema)
        .data(data)
        .parameters(parameters)
        .dataStack(dataStack)
        .build();
  }
}
