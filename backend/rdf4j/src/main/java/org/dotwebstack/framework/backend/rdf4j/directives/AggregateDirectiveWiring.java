package org.dotwebstack.framework.backend.rdf4j.directives;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.Objects;
import java.util.Optional;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.directives.AutoRegisteredSchemaDirectiveWiring;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.ValidatingDirectiveWiring;
import org.springframework.stereotype.Component;

@Component
public class AggregateDirectiveWiring extends ValidatingDirectiveWiring implements AutoRegisteredSchemaDirectiveWiring {

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
    validate(environment);
    return environment.getElement();
  }

  @Override
  public GraphQLInputObjectField onInputObjectField(
      SchemaDirectiveWiringEnvironment<GraphQLInputObjectField> environment) {
    validate(environment);
    return environment.getElement();
  }

  private void validate(SchemaDirectiveWiringEnvironment<?> environment) {
    GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();
    GraphQLFieldsContainer fieldsContainer = environment.getFieldsContainer();
    String typeName = fieldsContainer.getName();

    validate(getDirectiveName(), fieldDefinition, fieldsContainer, () -> {
      validateDataType(fieldDefinition);
      validateMax(typeName, fieldDefinition);
    });
  }

  void validateDataType(GraphQLFieldDefinition fieldDefinition) {
    GraphQLType rawType = GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType());

    boolean hasTransformDirective = Objects.nonNull(fieldDefinition.getDirective(CoreDirectives.TRANSFORM_NAME));

    assert hasTransformDirective || Scalars.GraphQLInt.equals(rawType) : "expected output type is Int but got "
        + rawType.getName();
  }

  void validateMax(String typeName, GraphQLFieldDefinition fieldDefinition) {
    Optional<PropertyShape> shapeOptional = Optional.ofNullable(nodeShapeRegistry.getByShaclName(typeName))
        .map(nodeShape -> nodeShape.getPropertyShape(fieldDefinition.getName()))
        .filter(propertyShape -> Objects.nonNull(propertyShape.getMaxCount()))
        .filter(propertyShape -> propertyShape.getMaxCount() == 1);

    assert shapeOptional.isEmpty() : String.format("propertyShape with sh:name '%s': sh:maxCount of 1 is invalid!",
        shapeOptional.get()
            .getName());

  }
}
