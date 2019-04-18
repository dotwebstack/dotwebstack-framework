package org.dotwebstack.framework.backend.rdf4j.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.backend.rdf4j.ValueUtils;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.model.util.Models;

@RequiredArgsConstructor
final class ValueFetcher implements DataFetcher<Object> {

  private final PropertyShape propertyShape;

  @Override
  public Object get(DataFetchingEnvironment environment) {
    GraphQLType fieldType = GraphQLTypeUtil.unwrapNonNull(environment.getFieldType());
    QuerySolution source = environment.getSource();

    if (!GraphQLTypeUtil.isScalar(fieldType)) {
      throw new UnsupportedOperationException(
          "Field types other than scalar types are not yet supported.");
    }

    return Models
        .getProperty(source.getModel(), source.getSubject(), propertyShape.getPath())
        .map(ValueUtils::convertValue)
        .orElse(null);
  }

}
