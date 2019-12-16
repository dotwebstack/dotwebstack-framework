package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLTypeUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FieldPath {
  @Builder.Default
  private List<GraphQLFieldDefinition> fieldDefinitions = new ArrayList<>();

  public boolean isSingleton() {
    return fieldDefinitions.size() == 1;
  }

  public Optional<FieldPath> remainder() {
    List<GraphQLFieldDefinition> fieldDefinitions = new ArrayList<>();
    if (this.fieldDefinitions.size() > 1) {
      fieldDefinitions = this.fieldDefinitions.subList(1, this.fieldDefinitions.size());
      return Optional.of(FieldPath.builder()
          .fieldDefinitions(fieldDefinitions)
          .build());
    }
    return Optional.empty();
  }

  public boolean isRequired() {
    return fieldDefinitions.stream()
        .allMatch(fieldDefinition -> GraphQLTypeUtil.isNonNull(fieldDefinition.getType()));
  }

  public GraphQLFieldDefinition current() {
    if (fieldDefinitions.size() > 0) {
      return fieldDefinitions.get(0);
    }
    throw illegalStateException("No current fieldDefinition!");
  }

  public Optional<GraphQLFieldDefinition> leaf() {
    if (fieldDefinitions.size() > 0) {
      return Optional.of(fieldDefinitions.get(fieldDefinitions.size() - 1));
    }
    return Optional.empty();
  }
}
