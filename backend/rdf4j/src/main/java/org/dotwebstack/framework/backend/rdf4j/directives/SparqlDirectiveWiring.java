package org.dotwebstack.framework.backend.rdf4j.directives;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.dotwebstack.framework.backend.rdf4j.query.QueryFetcher;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.directives.DirectiveUtils;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.springframework.stereotype.Component;

@Component
public class SparqlDirectiveWiring implements SchemaDirectiveWiring {

  private final RepositoryManager repositoryManager;

  private final NodeShapeRegistry nodeShapeRegistry;

  private final Map<String, String> prefixMap;

  public SparqlDirectiveWiring(RepositoryManager repositoryManager,
      NodeShapeRegistry nodeShapeRegistry, Rdf4jProperties rdf4jProperties) {
    this.repositoryManager = repositoryManager;
    this.nodeShapeRegistry = nodeShapeRegistry;
    this.prefixMap = rdf4jProperties.getPrefixes() != null
        ? HashBiMap.create(rdf4jProperties.getPrefixes()).inverse() : ImmutableMap.of();
  }

  @Override
  public GraphQLFieldDefinition onField(
      @NonNull SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLFieldDefinition fieldDefinition = environment.getElement();
    GraphQLType outputType = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    if (!(outputType instanceof GraphQLObjectType)) {
      throw new UnsupportedOperationException(
          "Field types other than object fields are not yet supported.");
    }

    String repositoryId = DirectiveUtils
        .getStringArgument(Rdf4jDirectives.SPARQL_ARG_REPOSITORY, environment.getDirective());

    if (!repositoryManager.hasRepositoryConfig(repositoryId)) {
      throw new InvalidConfigurationException(
          String.format("Repository '%s' was never configured.", repositoryId));
    }

    RepositoryConnection connection = repositoryManager.getRepository(repositoryId).getConnection();
    QueryFetcher queryFetcher = new QueryFetcher(connection, nodeShapeRegistry, prefixMap);

    environment.getCodeRegistry()
        .dataFetcher(environment.getFieldsContainer(), fieldDefinition, queryFetcher);

    return fieldDefinition;
  }

}
