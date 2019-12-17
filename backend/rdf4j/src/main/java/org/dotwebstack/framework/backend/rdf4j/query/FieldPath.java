package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLTypeUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;

@Builder
@Getter
public class FieldPath {

  @Builder.Default
  private List<GraphQLFieldDefinition> fieldDefinitions = new ArrayList<>();

  public boolean isSingleton() {
    return fieldDefinitions.size() == 1;
  }

  public Optional<FieldPath> rest() {
    List<GraphQLFieldDefinition> fieldDefinitions;
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

  public boolean isResource() {
    if (fieldDefinitions.size() == 1) {
      return Objects.nonNull(fieldDefinitions.get(0)
          .getDirective(Rdf4jDirectives.RESOURCE_NAME));
    }
    return false;
  }

  public GraphQLFieldDefinition first() {
    if (!fieldDefinitions.isEmpty()) {
      return fieldDefinitions.get(0);
    }
    throw illegalStateException("No current fieldDefinition!");
  }

  public Optional<GraphQLFieldDefinition> last() {
    if (!fieldDefinitions.isEmpty()) {
      return Optional.of(fieldDefinitions.get(fieldDefinitions.size() - 1));
    }
    return Optional.empty();
  }
}
