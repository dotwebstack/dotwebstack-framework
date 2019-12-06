package org.dotwebstack.framework.backend.rdf4j.helper;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FieldPathHelper {

  public static List<GraphQLFieldDefinition> getFieldDefinitions(GraphQLObjectType objectType, String fieldName) {
    List<String> splitted = Arrays.asList(fieldName.split("\\."));

    return getFieldDefinitions(objectType, splitted);
  }

  public static List<GraphQLFieldDefinition> getFieldDefinitions(SelectedField selectedField, String fieldName) {
    if (selectedField == null) {
      return emptyList();
    }

    GraphQLFieldDefinition fieldDefinition = selectedField.getFieldDefinition();

    GraphQLUnmodifiedType type = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    if (GraphQLTypeUtil.isScalar(type)) {
      return singletonList(fieldDefinition);
    }

    return getFieldDefinitions((GraphQLObjectType) type, fieldName);
  }

  public static List<GraphQLFieldDefinition> getFieldDefinitions(GraphQLObjectType objectType, List<String> fieldPath) {
    if (fieldPath.size() > 0) {
      String fieldName = fieldPath.get(0);
      GraphQLFieldDefinition fieldDefinition = objectType.getFieldDefinition(fieldName);
      List<GraphQLFieldDefinition> result = new ArrayList<>();

      if (Objects.isNull(fieldDefinition)) {
        throw illegalArgumentException("No property shape found for name '{}' nodeshape '{}'", fieldName,
            objectType.getName());
      }

      result.add(fieldDefinition);

      if (fieldPath.size() > 1) {
        GraphQLObjectType childObjectType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());
        result.addAll(getFieldDefinitions(childObjectType, fieldPath.subList(1, fieldPath.size())));
      }

      return result;

    }
    return emptyList();
  }
}
