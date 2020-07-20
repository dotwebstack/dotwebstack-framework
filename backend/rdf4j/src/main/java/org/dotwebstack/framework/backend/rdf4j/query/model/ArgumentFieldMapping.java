package org.dotwebstack.framework.backend.rdf4j.query.model;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import graphql.schema.GraphQLArgument;
import graphql.schema.SelectedField;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.rdf4j.query.FieldPath;

@Builder
@Getter
public class ArgumentFieldMapping {

  private GraphQLArgument argument;

  private SelectedField selectedField;

  private FieldPath fieldPath;

  public boolean argumentIsSet() {
    return !argumentIsEmpty();
  }

  public boolean argumentIsEmpty() {
    return selectedField.getArguments()
        .get(argument.getName()) == null;
  }

  public Object getArgumentValue() {
    return selectedField.getArguments()
        .get(argument.getName());
  }

  public boolean isSingleton() {
    return fieldPath.isSingleton();
  }

  public FieldPath fieldPathRest() {
    return fieldPath.rest()
        .orElseThrow(() -> illegalStateException("Expected rest fieldPath but got nothing!"));
  }

}
