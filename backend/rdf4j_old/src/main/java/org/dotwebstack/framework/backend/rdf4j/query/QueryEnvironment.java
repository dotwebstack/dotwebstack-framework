package org.dotwebstack.framework.backend.rdf4j.query;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;

@Builder
@Getter
public class QueryEnvironment {

  private final GraphQLObjectType objectType;

  private final GraphQLFieldDefinition fieldDefinition;

  private final DataFetchingFieldSelectionSet selectionSet;

  private final NodeShapeRegistry nodeShapeRegistry;

  private final Map<String, String> prefixMap;

}
