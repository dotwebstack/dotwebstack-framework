package org.dotwebstack.framework.service.openapi.mapping;

import graphql.language.Field;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;

public interface TypeMapper {

  List<Field> schemaToField(String name, Schema<?> schema);

  Object fieldToBody(Object data);

  String typeName();
}
