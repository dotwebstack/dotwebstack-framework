package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.traversers.TraverserFilter.directiveWithValueFilter;

import com.google.common.collect.ImmutableList;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.text.StringSubstitutor;
import org.dotwebstack.framework.backend.rdf4j.RepositoryAdapter;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.query.context.ConstructVerticeFactory;
import org.dotwebstack.framework.backend.rdf4j.query.context.SelectVerticeFactory;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.DirectiveUtils;
import org.dotwebstack.framework.core.traversers.CoreTraverser;
import org.dotwebstack.framework.core.traversers.DirectiveContainerTuple;
import org.dotwebstack.framework.core.validators.QueryValidator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;

@Slf4j
public final class QueryFetcher implements DataFetcher<Object> {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private final RepositoryAdapter repositoryAdapter;

  private final NodeShapeRegistry nodeShapeRegistry;

  private final Map<String, String> prefixMap;

  private final JexlEngine jexlEngine;

  private final CoreTraverser coreTraverser;

  private final List<QueryValidator> validators;

  private final SelectVerticeFactory selectVerticeFactory;

  private final ConstructVerticeFactory constructVerticeFactory;

  public QueryFetcher(RepositoryAdapter repositoryAdapter, NodeShapeRegistry nodeShapeRegistry,
      Map<String, String> prefixMap, JexlEngine jexlEngine, List<QueryValidator> validators,
      CoreTraverser coreTraverser, SelectVerticeFactory selectVerticeFactory,
      ConstructVerticeFactory constructVerticeFactory) {
    this.repositoryAdapter = repositoryAdapter;
    this.nodeShapeRegistry = nodeShapeRegistry;
    this.prefixMap = prefixMap;
    this.jexlEngine = jexlEngine;
    this.coreTraverser = coreTraverser;
    this.validators = validators;
    this.selectVerticeFactory = selectVerticeFactory;
    this.constructVerticeFactory = constructVerticeFactory;
  }

  @Override
  public Object get(@NonNull DataFetchingEnvironment environment) {
    GraphQLType outputType = GraphQLTypeUtil.unwrapNonNull(environment.getFieldType());
    GraphQLUnmodifiedType rawType = GraphQLTypeUtil.unwrapAll(outputType);

    if (!(rawType instanceof GraphQLObjectType)) {
      throw new UnsupportedOperationException("Field types other than object fields are not yet supported.");
    }

    validators.forEach(validator -> validator.validate(environment));

    QueryEnvironment queryEnvironment = QueryEnvironment.builder()
        .objectType((GraphQLObjectType) rawType)
        .selectionSet(environment.getSelectionSet())
        .nodeShapeRegistry(nodeShapeRegistry)
        .prefixMap(prefixMap)
        .fieldDefinition(environment.getFieldDefinition())
        .build();

    List<DirectiveContainerTuple> filterMapping =
        coreTraverser.getTuples(environment, directiveWithValueFilter(CoreDirectives.FILTER_NAME));

    List<IRI> subjects = fetchSubjects(queryEnvironment, filterMapping, environment.getArguments(), repositoryAdapter);

    // Fetch graph for given subjects
    Model model = fetchGraph(queryEnvironment, subjects, repositoryAdapter);

    if (GraphQLTypeUtil.isList(outputType)) {
      return subjects.stream()
          .map(subject -> new QuerySolution(model, subject))
          .collect(Collectors.toList());
    }

    return model.isEmpty() ? null : new QuerySolution(model, subjects.get(0));
  }

  private List<IRI> fetchSubjects(QueryEnvironment environment, List<DirectiveContainerTuple> filterMapping,
      Map<String, Object> arguments, RepositoryAdapter repositoryAdapter) {

    GraphQLDirective sparqlDirective = environment.getFieldDefinition()
        .getDirective(Rdf4jDirectives.SPARQL_NAME);

    String subjectTemplate =
        DirectiveUtils.getArgument(Rdf4jDirectives.SPARQL_ARG_SUBJECT, sparqlDirective, String.class);

    if (subjectTemplate != null) {
      StringSubstitutor substitutor = new StringSubstitutor(arguments);
      IRI subject = VF.createIRI(substitutor.replace(subjectTemplate));

      return ImmutableList.of(subject);
    }

    String subjectQuery = SubjectQueryBuilder.create(environment, jexlEngine, selectVerticeFactory)
        .getQueryString(arguments, sparqlDirective, filterMapping);

    LOG.debug("Executing query for subjects:\n{}", subjectQuery);

    String repositoryId =
        DirectiveUtils.getArgument(Rdf4jDirectives.SPARQL_ARG_REPOSITORY, sparqlDirective, String.class);

    TupleQueryResult queryResult =
        repositoryAdapter.prepareTupleQuery(repositoryId, environment.getFieldDefinition(), subjectQuery)
            .evaluate();

    return QueryResults.asList(queryResult)
        .stream()
        .map(bindings -> (IRI) bindings.getValue("s"))
        .collect(Collectors.toList());
  }

  private Model fetchGraph(QueryEnvironment environment, List<IRI> subjects, RepositoryAdapter repositoryAdapter) {
    if (subjects.isEmpty()) {
      return new TreeModel();
    }

    String graphQuery = GraphQueryBuilder.create(environment, subjects, constructVerticeFactory)
        .getQueryString();

    LOG.debug("Executing query for graph:\n{}", graphQuery);


    GraphQLDirective sparqlDirective = environment.getFieldDefinition()
        .getDirective(Rdf4jDirectives.SPARQL_NAME);

    String repositoryId =
        DirectiveUtils.getArgument(Rdf4jDirectives.SPARQL_ARG_REPOSITORY, sparqlDirective, String.class);

    GraphQueryResult queryResult =
        repositoryAdapter.prepareGraphQuery(repositoryId, environment.getFieldDefinition(), graphQuery)
            .evaluate();

    return QueryResults.asModel(queryResult);
  }
}
