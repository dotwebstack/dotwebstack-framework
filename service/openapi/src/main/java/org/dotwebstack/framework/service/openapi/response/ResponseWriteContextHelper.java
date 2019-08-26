package org.dotwebstack.framework.service.openapi.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

public class ResponseWriteContextHelper {

  private ResponseWriteContextHelper() {}

  public static ResponseWriteContext createNewResponseWriteContext(@NonNull ResponseWriteContext parentContext,
      ResponseObject child) {
    return ResponseWriteContext.builder()
        .schema(child)
        .data(parentContext.getData())
        .dataStack(parentContext.getDataStack())
        .parameters(parentContext.getParameters())
        .build();
  }

  public static ResponseWriteContext createNewResponseWriteContext(@NonNull ResponseWriteContext parentContext,
      Object childData) {
    List<Object> dataStack = new ArrayList<>(parentContext.getDataStack());
    if (childData instanceof Map) {
      dataStack.add(0, childData);
    }
    return ResponseWriteContext.builder()
        .schema(parentContext.getSchema())
        .data(childData)
        .parameters(parentContext.getParameters())
        .dataStack(dataStack)
        .build();
  }

  public static ResponseWriteContext unwrapSchema(@NonNull ResponseWriteContext parentContext) {
    return ResponseWriteContext.builder()
        .schema(parentContext.getSchema()
            .getChildren()
            .get(0))
        .data(parentContext.getData())
        .dataStack(parentContext.getDataStack())
        .parameters(parentContext.getParameters())
        .build();
  }

  @SuppressWarnings("unchecked")
  public static ResponseWriteContext unwrapData(@NonNull ResponseWriteContext parentContext, ResponseObject child) {
    Object data = ((Map<String, Object>) parentContext.getData()).get(child.getIdentifier());
    List<Object> dataStack = new ArrayList<>(parentContext.getDataStack());
    if (data instanceof Map) {
      dataStack.add(0, data);
    }

    return ResponseWriteContext.builder()
        .parameters(parentContext.getParameters())
        .schema(parentContext.getSchema())
        .data(data)
        .dataStack(dataStack)
        .build();
  }

  @SuppressWarnings("rawtypes")
  public static ResponseWriteContext unwrapSchemaAndListData(@NonNull ResponseWriteContext parentContext) {
    ResponseObject embedded = parentContext.getSchema()
        .getChildren()
        .get(0);
    List data = (List) ((Map) parentContext.getData()).get(embedded.getIdentifier());

    List<Object> dataStack = new ArrayList<>(parentContext.getDataStack());
    if (data instanceof Map) {
      dataStack.add(0, data);
    }

    return ResponseWriteContext.builder()
        .schema(embedded)
        .data(data)
        .parameters(parentContext.getParameters())
        .dataStack(dataStack)
        .build();
  }
}
