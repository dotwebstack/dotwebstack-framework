package org.dotwebstack.framework.backend.rdf4j.query.context;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.dotwebstack.framework.backend.rdf4j.helper.FieldPathHelper.getFieldDefinitions;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.SelectedField;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.rdf4j.query.FieldPath;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.DirectiveUtils;

public class FieldPathHelper {

  @SuppressWarnings("unchecked")
  private static String getFieldName(GraphQLArgument argument, String directiveName) {
    if (CoreDirectives.SORT_NAME.equals(directiveName)) {
      Object fieldValue = nonNull(argument.getValue()) ? argument.getValue() : argument.getDefaultValue();
      Map<String, Object> map = nonNull(fieldValue) ? castToMap(((List<Object>) fieldValue).get(0)) : emptyMap();

      return map.getOrDefault("field", argument.getName())
          .toString();
    }

    GraphQLDirective directive = argument.getDirective(directiveName);
    if (nonNull(directive)) {
      String fieldName = DirectiveUtils.getArgument(directive, "field", String.class);

      if (isNotBlank(fieldName)) {
        return fieldName;
      }

      return argument.getName();
    }

    throw illegalStateException("Could not find directive for argument {} and directiveName {}", argument.getName(),
        directiveName);
  }

  public static FieldPath getFieldPath(SelectedField selectedField, GraphQLArgument argument, String directiveName) {
    GraphQLUnmodifiedType unmodifiedType = GraphQLTypeUtil.unwrapAll(selectedField.getFieldDefinition()
        .getType());
    String fieldName = getFieldName(argument, directiveName);

    if (unmodifiedType instanceof GraphQLObjectType) {
      GraphQLObjectType objectType = (GraphQLObjectType) unmodifiedType;

      return FieldPath.builder()
          .fieldDefinitions(getFieldDefinitions(objectType, fieldName))
          .build();
    }

    if (unmodifiedType instanceof GraphQLScalarType) {
      return FieldPath.builder()
          .fieldDefinitions(singletonList(selectedField.getFieldDefinition()))
          .build();
    }

    throw unsupportedOperationException("Unable to determine fieldDefinition for argument {}", argument);
  }
}
