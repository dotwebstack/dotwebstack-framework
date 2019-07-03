package org.dotwebstack.framework.backend.rdf4j.directives;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.Map;
import lombok.NonNull;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.dotwebstack.framework.backend.rdf4j.query.QueryFetcher;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.directives.DirectiveUtils;
import org.dotwebstack.framework.core.traversers.FilterDirectiveTraverser;
import org.dotwebstack.framework.core.validators.ConstraintValidator;
import org.dotwebstack.framework.core.validators.SortFieldValidator;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.springframework.stereotype.Component;

@Component
public class SparqlDirectiveWiring implements SchemaDirectiveWiring {

  private final RepositoryManager repositoryManager;

  private final NodeShapeRegistry nodeShapeRegistry;

  private final Map<String, String> prefixMap;

  private final JexlEngine jexlEngine;

  private ConstraintValidator constraintValidator;

  private FilterDirectiveTraverser filterDirectiveTraverser;

  public SparqlDirectiveWiring(RepositoryManager repositoryManager, NodeShapeRegistry nodeShapeRegistry,
      Rdf4jProperties rdf4jProperties, JexlEngine jexlEngine, ConstraintValidator constraintValidator,
      FilterDirectiveTraverser filterDirectiveTraverser) {
    this.repositoryManager = repositoryManager;
    this.nodeShapeRegistry = nodeShapeRegistry;
    this.prefixMap = rdf4jProperties.getPrefixes() != null ? HashBiMap.create(rdf4jProperties.getPrefixes())
        .inverse() : ImmutableMap.of();
    this.jexlEngine = jexlEngine;
    this.constraintValidator = constraintValidator;
    this.filterDirectiveTraverser = filterDirectiveTraverser;
  }

  @Override
  public GraphQLFieldDefinition onField(@NonNull SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLFieldDefinition fieldDefinition = environment.getElement();
    GraphQLType outputType = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    if (!(outputType instanceof GraphQLObjectType)) {
      throw new UnsupportedOperationException("Field types other than object fields are not yet supported.");
    }

    String repositoryId =
        DirectiveUtils.getArgument(Rdf4jDirectives.SPARQL_ARG_REPOSITORY, environment.getDirective(), String.class);

    if (!repositoryManager.hasRepositoryConfig(repositoryId)) {
      throw new InvalidConfigurationException("Repository '{}' was never configured.", repositoryId);
    }

    RepositoryConnection connection = repositoryManager.getRepository(repositoryId)
        .getConnection();

    // startup time validation of default values for sort fields
    SortFieldValidator sortFieldValidator = new SortFieldValidator(environment.getRegistry());
    validateSortField(fieldDefinition, sortFieldValidator);

    QueryFetcher queryFetcher = new QueryFetcher(connection, nodeShapeRegistry, prefixMap, jexlEngine,
        constraintValidator, filterDirectiveTraverser, sortFieldValidator);

    environment.getCodeRegistry()
        .dataFetcher(environment.getFieldsContainer(), fieldDefinition, queryFetcher);

    return fieldDefinition;
  }

  private void validateSortField(GraphQLFieldDefinition fieldDefinition, SortFieldValidator sortFieldValidator) {
    // the orderBy argument in the @sparl directive
    GraphQLArgument orderByArgument = fieldDefinition.getDirective(Rdf4jDirectives.SPARQL_NAME)
        .getArgument(Rdf4jDirectives.SPARQL_ARG_ORDER_BY);

    // the argument in field definition to which the orderBy refers
    GraphQLArgument sortArgument = fieldDefinition.getArgument((String) orderByArgument.getValue());

    if (sortArgument != null && sortArgument.getDefaultValue() != null) {
      sortFieldValidator.traverseArgument(fieldDefinition.getType(), sortArgument, sortArgument.getDefaultValue());
    }
  }

}
