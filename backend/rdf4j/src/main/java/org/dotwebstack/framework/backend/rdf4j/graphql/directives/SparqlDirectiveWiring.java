package org.dotwebstack.framework.backend.rdf4j.graphql.directives;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.backend.rdf4j.graphql.QueryFetcher;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.graphql.directives.DirectiveUtils;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SparqlDirectiveWiring implements SchemaDirectiveWiring {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private final RepositoryManager repositoryManager;

  private final NodeShapeRegistry nodeShapeRegistry;

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
        .getStringArgument(Directives.SPARQL_ARG_REPOSITORY, environment.getDirective());

    if (!repositoryManager.hasRepositoryConfig(repositoryId)) {
      throw new InvalidConfigurationException(
          String.format("Repository '%s' was never configured.", repositoryId));
    }

    QueryFetcher queryFetcher = new QueryFetcher(
        repositoryManager.getRepository(repositoryId).getConnection(), nodeShapeRegistry);

    environment.getCodeRegistry()
        .dataFetcher(environment.getFieldsContainer(), fieldDefinition, queryFetcher);

    return fieldDefinition;
  }

}
