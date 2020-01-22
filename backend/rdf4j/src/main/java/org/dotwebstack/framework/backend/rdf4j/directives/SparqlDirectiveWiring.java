package org.dotwebstack.framework.backend.rdf4j.directives;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.dotwebstack.framework.backend.rdf4j.RepositoryAdapter;
import org.dotwebstack.framework.backend.rdf4j.query.FixedQueryFetcher;
import org.dotwebstack.framework.backend.rdf4j.query.QueryFetcher;
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

  private final List<RepositoryAdapter> repositoryAdapters;

  private final NodeShapeRegistry nodeShapeRegistry;

  private final Map<String, String> prefixMap;

  private final JexlEngine jexlEngine;

  private ConstraintValidator constraintValidator;

  private CoreTraverser coreTraverser;

  private final SelectVerticeFactory selectVerticeFactory;

  private final ConstructVerticeFactory constructVerticeFactory;

  public SparqlDirectiveWiring(List<RepositoryAdapter> repositoryAdapters, NodeShapeRegistry nodeShapeRegistry,
      Rdf4jProperties rdf4jProperties, JexlEngine jexlEngine, ConstraintValidator constraintValidator,
      CoreTraverser coreTraverser, SelectVerticeFactory selectVerticeFactory,
      ConstructVerticeFactory constructVerticeFactory) {
    this.repositoryAdapters = repositoryAdapters;
    this.nodeShapeRegistry = nodeShapeRegistry;
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

    boolean outputIsModel = outputType.getName()
        .equals(Rdf4jScalars.MODEL.getName());

    if (!outputIsModel && !(outputType instanceof GraphQLObjectType)) {
      throw new UnsupportedOperationException("Field types other than object fields are not yet supported.");
    }

    registerDataFetcher(environment, outputIsModel);
    return fieldDefinition;
  }

  private RepositoryAdapter getRepositoryAdapter(String repositoryId) {
    return repositoryAdapters.stream()
        .filter(repositoryAdapter -> repositoryAdapter.supports(repositoryId))
        .findFirst()
        .orElseThrow(() -> new InvalidConfigurationException("Repository '{}' was never configured.", repositoryId));
  }

  private void registerDataFetcher(@NonNull SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment,
      boolean outputIsModel) {
    GraphQLCodeRegistry.Builder codeRegistry = environment.getCodeRegistry();

    GraphQLFieldsContainer fieldsContainer = environment.getFieldsContainer();
    GraphQLFieldDefinition element = environment.getElement();

    RepositoryAdapter repositoryAdapter = getRepositoryAdapter(getRepositoryId(environment));
    List<QueryValidator> validators = getValidators(environment);
    DataFetcher<?> queryFetcher = getDataFetcher(outputIsModel, repositoryAdapter, validators);

    codeRegistry.dataFetcher(fieldsContainer, element, queryFetcher);
  }

  private DataFetcher<?> getDataFetcher(boolean outputIsModel, RepositoryAdapter supportedAdapter,
      List<QueryValidator> validators) {
    return outputIsModel ? new FixedQueryFetcher(supportedAdapter, validators)
        : new QueryFetcher(supportedAdapter, nodeShapeRegistry, prefixMap, jexlEngine, validators, coreTraverser,
            selectVerticeFactory, constructVerticeFactory);
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

  @Override
  public String getDirectiveName() {
    return Rdf4jDirectives.SPARQL_NAME;
  }
}
