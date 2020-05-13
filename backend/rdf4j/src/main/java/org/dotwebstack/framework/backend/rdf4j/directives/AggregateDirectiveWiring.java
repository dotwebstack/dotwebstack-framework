package org.dotwebstack.framework.backend.rdf4j.directives;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.Objects;
import java.util.Optional;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.core.directives.AutoRegisteredSchemaDirectiveWiring;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.springframework.stereotype.Component;

@Component
public class AggregateDirectiveWiring implements AutoRegisteredSchemaDirectiveWiring {

  private final NodeShapeRegistry nodeShapeRegistry;

  public AggregateDirectiveWiring(NodeShapeRegistry nodeShapeRegistry) {
    this.nodeShapeRegistry = nodeShapeRegistry;
  }

  @Override
  public String getDirectiveName() {
    return Rdf4jDirectives.AGGREGATE_NAME;
  }

  @Override
  public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    validate(environment.getFieldsContainer()
        .getName(), environment.getFieldDefinition());
    return environment.getElement();
  }

  @Override
  public GraphQLInputObjectField onInputObjectField(
      SchemaDirectiveWiringEnvironment<GraphQLInputObjectField> environment) {
    validate(environment.getFieldsContainer()
        .getName(), environment.getFieldDefinition());
    return environment.getElement();
  }

  private void validate(String typeName, GraphQLFieldDefinition fieldDefinition) {
    validateDataType(typeName, fieldDefinition);
    validateMax(typeName, fieldDefinition);
  }

  private void validateDataType(String typeName, GraphQLFieldDefinition fieldDefinition) {
    GraphQLType rawType = GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType());

    boolean hasTransformDirective = Objects.isNull(fieldDefinition.getDirective(CoreDirectives.TRANSFORM_NAME));

    if (hasTransformDirective && !Scalars.GraphQLInt.equals(rawType)) {
      throw invalidConfigurationException(
          "Found an error on @aggregate directive defined on field {}.{}: expected output type is Int but got {}",
          typeName, fieldDefinition.getName(), ((GraphQLNamedType) rawType).getName());
    }
  }

  private void validateMax(String typeName, GraphQLFieldDefinition fieldDefinition) {
    Optional.ofNullable(nodeShapeRegistry.getByShaclName(typeName))
        .map(nodeShape -> nodeShape.getPropertyShape(fieldDefinition.getName()))
        .filter(propertyShape -> Objects.nonNull(propertyShape.getMaxCount()) && propertyShape.getMaxCount() == 1)
        .ifPresent(propertyShape -> {
          throw invalidConfigurationException(
              "An @aggregate directive on field '{}.{}' using propertyShape with sh:name '{}':"
                  + " sh:maxCount of 1 is invalid!",
              typeName, fieldDefinition.getName(), propertyShape.getName());
        });
  }
}
