package org.dotwebstack.framework.backend.rdf4j.graphql.directives;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.backend.rdf4j.graphql.query.QueryFetcher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SparqlDirectiveWiring implements SchemaDirectiveWiring {

  private final QueryFetcher queryFetcher;

  @Override
  public GraphQLFieldDefinition onField(
      @NonNull SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLFieldDefinition fieldDefinition = environment.getElement();
    GraphQLType outputType = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    if (GraphQLTypeUtil.isEnum(outputType) || GraphQLTypeUtil.isScalar(outputType)) {
      throw new UnsupportedOperationException("Scalar or enum output types are not yet supported.");
    }

    environment.getCodeRegistry()
        .dataFetcher(environment.getFieldsContainer(), fieldDefinition, queryFetcher);

    return fieldDefinition;
  }

}
