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
    Deque<FieldContext> dataStack = new ArrayDeque<>(parentContext.getDataStack());

    if (!childSchema.isEnvelope() && data instanceof Map) {
      data = ((Map) data).get(childSchema.getIdentifier());
      dataStack = createNewDataStack(dataStack, data, parentContext.getParameters());
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
      @NonNull ResponseWriteContext parentContext, @NonNull ResponseObject childSchema) {
    Deque<FieldContext> dataStack = new ArrayDeque<>(parentContext.getDataStack());
    Object data = parentContext.getData();

    if (!childSchema.isEnvelope()) {
      if (!parentContext.getDataStack()
          .isEmpty()) {
        data = ((Map) parentContext.getDataStack()
            .peek()
            .getData()).get(childSchema.getIdentifier());
        dataStack = createNewDataStack(parentContext.getDataStack(), data, parentContext.getParameters());
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
    Deque<FieldContext> dataStack =
        createNewDataStack(parentContext.getDataStack(), childData, parentContext.getParameters());
    return createNewResponseWriteContext(parentContext.getSchema(), childData, parentContext.getParameters(),
        dataStack);
  }

  public static ResponseWriteContext createNewResponseWriteContext(@NonNull ResponseObject schema, Object data,
      Map<String, Object> parameters, @NonNull Deque<FieldContext> dataStack) {
    return ResponseWriteContext.builder()
        .schema(schema)
        .data(data)
        .parameters(parameters)
        .dataStack(dataStack)
        .build();
  }
}
