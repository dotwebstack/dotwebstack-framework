package org.dotwebstack.framework.backend.rdf4j.directives;

import static org.dotwebstack.framework.backend.rdf4j.helper.GraphQlFieldDefinitionHelper.CONSTRUCT_QUERY_COMMAND;
import static org.dotwebstack.framework.backend.rdf4j.helper.GraphQlFieldDefinitionHelper.DESCRIBE_QUERY_COMMAND;
import static org.dotwebstack.framework.backend.rdf4j.helper.GraphQlFieldDefinitionHelper.SELECT_QUERY_COMMAND;
import static org.dotwebstack.framework.backend.rdf4j.helper.GraphQlFieldDefinitionHelper.graphQlFieldDefinitionIsOfType;
import static org.dotwebstack.framework.backend.rdf4j.helper.GraphQlFieldDefinitionHelper.validateQueryHasCommand;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.dotwebstack.framework.backend.rdf4j.RepositoryAdapter;
import org.dotwebstack.framework.backend.rdf4j.query.QueryFetcher;
import org.dotwebstack.framework.backend.rdf4j.query.StaticQueryFetcher;
import org.dotwebstack.framework.backend.rdf4j.query.context.ConstructVerticeFactory;
import org.dotwebstack.framework.backend.rdf4j.query.context.SelectVerticeFactory;
import org.dotwebstack.framework.backend.rdf4j.scalars.Rdf4jScalars;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.directives.AutoRegisteredSchemaDirectiveWiring;
import org.dotwebstack.framework.core.directives.DirectiveUtils;
import org.dotwebstack.framework.core.traversers.CoreTraverser;
import org.dotwebstack.framework.core.validators.ConstraintValidator;
import org.dotwebstack.framework.core.validators.QueryValidator;
import org.dotwebstack.framework.core.validators.SortFieldValidator;
import org.springframework.stereotype.Component;

@Component
public class SparqlDirectiveWiring implements AutoRegisteredSchemaDirectiveWiring {

  private static final String RQ_FILE_EXTENSION = ".rq";

  private final List<RepositoryAdapter> repositoryAdapters;

  private final NodeShapeRegistry nodeShapeRegistry;

  private final Map<String, String> queryReferenceRegistry;

  private final Map<String, String> prefixMap;

  private final JexlEngine jexlEngine;

  private ConstraintValidator constraintValidator;

  private CoreTraverser coreTraverser;

  private final SelectVerticeFactory selectVerticeFactory;

  private final ConstructVerticeFactory constructVerticeFactory;

  public SparqlDirectiveWiring(List<RepositoryAdapter> repositoryAdapters, NodeShapeRegistry nodeShapeRegistry,
      Map<String, String> queryReferenceRegistry, Rdf4jProperties rdf4jProperties, JexlEngine jexlEngine,
      ConstraintValidator constraintValidator, CoreTraverser coreTraverser, SelectVerticeFactory selectVerticeFactory,
      ConstructVerticeFactory constructVerticeFactory) {
    this.repositoryAdapters = repositoryAdapters;
    this.nodeShapeRegistry = nodeShapeRegistry;
    this.queryReferenceRegistry = queryReferenceRegistry;
    this.prefixMap = rdf4jProperties.getPrefixes() != null ? HashBiMap.create(rdf4jProperties.getPrefixes())
        .inverse() : ImmutableMap.of();
    this.jexlEngine = jexlEngine;
    this.constraintValidator = constraintValidator;
    this.coreTraverser = coreTraverser;
    this.selectVerticeFactory = selectVerticeFactory;
    this.constructVerticeFactory = constructVerticeFactory;
  }

  @Override
  public GraphQLFieldDefinition onField(@NonNull SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLFieldDefinition fieldDefinition = environment.getElement();
    GraphQLType outputType = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());

    validateOutputType(outputType);

    String queryReference = getQueryReference(environment);
    String staticSparqlQuery = queryReferenceRegistry.get(queryReference + RQ_FILE_EXTENSION);

    if (Objects.nonNull(staticSparqlQuery)) {
      if (graphQlFieldDefinitionIsOfType((GraphQLOutputType) outputType, Rdf4jScalars.IRI)) {

        validateQueryHasCommand(staticSparqlQuery, SELECT_QUERY_COMMAND);
        validateBindingName(fieldDefinition, staticSparqlQuery);

      } else if (graphQlFieldDefinitionIsOfType((GraphQLOutputType) outputType, Rdf4jScalars.MODEL)) {
        validateQueryHasCommand(staticSparqlQuery, DESCRIBE_QUERY_COMMAND, CONSTRUCT_QUERY_COMMAND);
      }
    }

    registerDataFetcher(environment, staticSparqlQuery);

    return fieldDefinition;
  }

  private void validateBindingName(GraphQLFieldDefinition fieldDefinition, String queryString) {
    String name = fieldDefinition.getName();
    if (!queryString.contains(name)) {
      throw invalidConfigurationException("GraphQL field name:{} should be bounded", name);
    }
  }

  private void validateOutputType(GraphQLType outputType) {
    if (!outputType.getName()
        .equals(Rdf4jScalars.MODEL.getName())
        && !outputType.getName()
            .equals(Rdf4jScalars.IRI.getName())
        && !(outputType instanceof GraphQLObjectType)) {
      throw new IllegalArgumentException("Output types other than 'Model', 'IRI' or 'Object' are not yet supported.");
    }
  }

  private RepositoryAdapter getRepositoryAdapter(String repositoryId) {
    return repositoryAdapters.stream()
        .filter(repositoryAdapter -> repositoryAdapter.supports(repositoryId))
        .findFirst()
        .orElseThrow(() -> new InvalidConfigurationException("Repository '{}' was never configured.", repositoryId));
  }

  private void registerDataFetcher(@NonNull SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment,
      String staticSparqlQuery) {
    GraphQLCodeRegistry.Builder codeRegistry = environment.getCodeRegistry();

    GraphQLFieldsContainer fieldsContainer = environment.getFieldsContainer();
    GraphQLFieldDefinition fieldDefinition = environment.getElement();

    RepositoryAdapter repositoryAdapter = getRepositoryAdapter(getRepositoryId(environment));

    List<QueryValidator> validators = getValidators(environment);
    DataFetcher<?> queryFetcher = getDataFetcher(fieldDefinition, repositoryAdapter, validators, staticSparqlQuery);

    codeRegistry.dataFetcher(fieldsContainer, fieldDefinition, queryFetcher);
  }

  private DataFetcher<?> getDataFetcher(GraphQLFieldDefinition fieldDefinition, RepositoryAdapter supportedAdapter,
      List<QueryValidator> validators, String staticSparqlQuery) {
    if (StaticQueryFetcher.supports(fieldDefinition)) {
      return new StaticQueryFetcher(supportedAdapter, validators, staticSparqlQuery);
    } else {
      return new QueryFetcher(supportedAdapter, nodeShapeRegistry, prefixMap, jexlEngine, validators, coreTraverser,
          selectVerticeFactory, constructVerticeFactory);
    }
  }

  private List<QueryValidator> getValidators(
      @NonNull SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    // startup time validation of default values for sort fields
    SortFieldValidator sortFieldValidator = new SortFieldValidator(coreTraverser, environment.getRegistry());

    return ImmutableList.of(constraintValidator, sortFieldValidator);
  }

  private String getRepositoryId(@NonNull SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    return DirectiveUtils.getArgument(environment.getDirective(), Rdf4jDirectives.SPARQL_ARG_REPOSITORY, String.class);
  }

  private String getQueryReference(@NonNull SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    return DirectiveUtils.getArgument(environment.getDirective(), Rdf4jDirectives.SPARQL_ARG_QUERY_REF, String.class);
  }

  @Override
  public String getDirectiveName() {
    return Rdf4jDirectives.SPARQL_NAME;
  }
}
