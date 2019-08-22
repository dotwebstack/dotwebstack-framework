package org.dotwebstack.framework.service.openapi.response;

import java.util.List;
import java.util.Map;

public class ResponseWriteContextHelper {

  private ResponseWriteContextHelper() {}

  public static ResponseWriteContext unwrapSchema(ResponseWriteContext parentContext) {
    return ResponseWriteContext.builder()
        .schema(parentContext.getSchema()
            .getChildren()
            .get(0))
        .data(parentContext.getData())
        .parameters(parentContext.getParameters())
        .build();
  }

  @SuppressWarnings("unchecked")
  public static ResponseWriteContext unwrapData(ResponseWriteContext parentContext) {
    return ResponseWriteContext.builder()
        .parameters(parentContext.getParameters())
        .schema(parentContext.getSchema())
        .data(((Map<String, Object>) parentContext.getData()).get(parentContext.getSchema()
            .getIdentifier()))
        .build();
  }

  @SuppressWarnings("rawtypes")
  public static ResponseWriteContext unwrapSchemaAndData(ResponseWriteContext parentContext) {
    ResponseObject embedded = parentContext.getSchema()
        .getChildren()
        .get(0);
    List childData = (List) ((Map) parentContext.getData()).get(embedded.getIdentifier());
    return ResponseWriteContext.builder()
        .schema(embedded)
        .data(childData)
        .parameters(parentContext.getParameters())
        .build();
  }

}
