package org.dotwebstack.framework.backend.rdf4j.graphql.query;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.backend.rdf4j.ValueUtils;
import org.dotwebstack.framework.backend.rdf4j.graphql.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.model.util.Models;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class ValueFetcher implements DataFetcher<Object> {

  private final NodeShapeRegistry nodeShapeRegistry;

  @Override
  public Object get(DataFetchingEnvironment environment) {
    GraphQLType fieldType = GraphQLTypeUtil.unwrapNonNull(environment.getFieldType());
    QuerySolution source = environment.getSource();

    if (!GraphQLTypeUtil.isScalar(fieldType)) {
      throw new UnsupportedOperationException(
          "Field types other than scalar types are not yet supported.");
    }

    GraphQLObjectType objectType = (GraphQLObjectType) environment.getParentType();
    NodeShape nodeShape = nodeShapeRegistry.get(objectType);

    PropertyShape propertyShape = nodeShape
        .getPropertyShape(environment.getFieldDefinition().getName());

    return Models
        .getProperty(source.getModel(), source.getSubject(), propertyShape.getPath())
        .map(ValueUtils::convertValue)
        .orElse(null);
  }

}
