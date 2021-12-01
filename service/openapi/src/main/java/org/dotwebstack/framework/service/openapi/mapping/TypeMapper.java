package org.dotwebstack.framework.service.openapi.mapping;

import graphql.language.Field;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import java.util.Map;

public interface TypeMapper {

  List<Field> schemaToField(String name, Schema<?> schema, Map<String, Object> parameters);

  Object fieldToBody(Object data, Schema<?> schema);

  String typeName();
}
