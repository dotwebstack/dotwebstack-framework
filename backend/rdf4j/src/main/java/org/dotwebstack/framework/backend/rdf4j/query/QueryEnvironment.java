package org.dotwebstack.framework.backend.rdf4j.query;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLObjectType;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;

@Builder
@Getter
class QueryEnvironment {

  private final GraphQLObjectType objectType;

  private final GraphQLDirective sparqlDirective;

  private final DataFetchingFieldSelectionSet selectionSet;

  private final NodeShapeRegistry nodeShapeRegistry;

  private final Map<String, String> prefixMap;

}
