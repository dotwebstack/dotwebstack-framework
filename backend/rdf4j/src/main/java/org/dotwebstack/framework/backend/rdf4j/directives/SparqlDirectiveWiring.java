package org.dotwebstack.framework.backend.rdf4j.directives;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.dotwebstack.framework.backend.rdf4j.query.QueryFetcher;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.directives.DirectiveUtils;
import org.dotwebstack.framework.core.traversers.CoreTraverser;
import org.dotwebstack.framework.core.validators.ConstraintValidator;
import org.dotwebstack.framework.core.validators.SortFieldValidator;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResolver;
import org.springframework.stereotype.Component;

@Component
public class SparqlDirectiveWiring implements SchemaDirectiveWiring {

  private final List<RepositoryResolver> repositoryResolvers;

  private final NodeShapeRegistry nodeShapeRegistry;

  private final Map<String, String> prefixMap;

  private final JexlEngine jexlEngine;

  private ConstraintValidator constraintValidator;

  private CoreTraverser coreTraverser;

  public SparqlDirectiveWiring(List<RepositoryResolver> repositoryResolvers, NodeShapeRegistry nodeShapeRegistry,
      Rdf4jProperties rdf4jProperties, JexlEngine jexlEngine, ConstraintValidator constraintValidator,
      CoreTraverser coreTraverser) {
    this.repositoryResolvers = repositoryResolvers;
    this.nodeShapeRegistry = nodeShapeRegistry;
    this.prefixMap = rdf4jProperties.getPrefixes() != null ? HashBiMap.create(rdf4jProperties.getPrefixes())
        .inverse() : ImmutableMap.of();
    this.jexlEngine = jexlEngine;
    this.constraintValidator = constraintValidator;
    this.coreTraverser = coreTraverser;
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

    RepositoryResolver repositoryResolver = repositoryResolvers.stream()
        .filter(resolver -> resolver.getRepository(repositoryId) != null)
        .findFirst()
        .orElseThrow(() -> new InvalidConfigurationException("Repository '{}' was never configured.", repositoryId));

    RepositoryConnection connection = repositoryResolver.getRepository(repositoryId)
        .getConnection();

    // startup time validation of default values for sort fields
    SortFieldValidator sortFieldValidator = new SortFieldValidator(coreTraverser, environment.getRegistry());

    validateSortField(fieldDefinition, sortFieldValidator);

    QueryFetcher queryFetcher = new QueryFetcher(connection, nodeShapeRegistry, prefixMap, jexlEngine,
        ImmutableList.of(constraintValidator, sortFieldValidator), coreTraverser);

    environment.getCodeRegistry()
        .dataFetcher(environment.getFieldsContainer(), fieldDefinition, queryFetcher);

    return fieldDefinition;
  }

  private void validateSortField(GraphQLFieldDefinition fieldDefinition, SortFieldValidator sortFieldValidator) {
    // the orderBy container in the @sparl directive
    GraphQLArgument orderByArgument = fieldDefinition.getDirective(Rdf4jDirectives.SPARQL_NAME)
        .getArgument(Rdf4jDirectives.SPARQL_ARG_ORDER_BY);

    // the container in field definition to which the orderBy refers
    GraphQLArgument sortArgument = fieldDefinition.getArgument((String) orderByArgument.getValue());

    if (sortArgument != null && sortArgument.getDefaultValue() != null) {
      sortFieldValidator.validate(fieldDefinition.getType(), sortArgument, sortArgument.getDefaultValue());
    }
  }

}
