package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.traversers.TraverserFilter.directiveWithValueFilter;

import com.google.common.collect.ImmutableList;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
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
import org.dotwebstack.framework.core.traversers.DirectiveContainerObject;
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

    List<DirectiveContainerObject> filterMapping =
        coreTraverser.getTuples(environment, directiveWithValueFilter(CoreDirectives.FILTER_NAME))
            .stream()
            .map(determineIsResource(environment))
            .collect(Collectors.toList());

    List<IRI> subjects =
        fetchSubjects(environment, queryEnvironment, filterMapping, environment.getArguments(), repositoryAdapter);

    LOG.debug("Fetched subjects: {}", subjects);

    // Fetch graph for given subjects
    Model model = fetchGraph(environment, queryEnvironment, subjects, repositoryAdapter);

    if (GraphQLTypeUtil.isList(outputType)) {
      return subjects.stream()
          .map(subject -> new QuerySolution(model, subject))
          .collect(Collectors.toList());
    }

    return model.isEmpty() ? null : new QuerySolution(model, subjects.get(0));
  }

  private Function<DirectiveContainerObject, DirectiveContainerObject> determineIsResource(
      @NonNull DataFetchingEnvironment environment) {
    return directiveContainerObject -> {
      GraphQLArgument filterArgument = getFilterArgument(directiveContainerObject);
      String name = getPropertyName(directiveContainerObject, filterArgument);

      Optional.ofNullable(getField(environment.getFieldDefinition(), name))
          .flatMap(definition -> Optional.ofNullable(definition.getDirective(Rdf4jDirectives.RESOURCE_NAME)))
          .ifPresent(directive -> directiveContainerObject.setResource(true));

      return directiveContainerObject;
    };
  }

  private String getPropertyName(DirectiveContainerObject dco, GraphQLArgument filterArgument) {
    return Objects.nonNull(filterArgument.getValue()) ? filterArgument.getName()
        : dco.getContainer()
            .getName();
  }

  private GraphQLArgument getFilterArgument(DirectiveContainerObject dco) {
    return dco.getContainer()
        .getDirective(CoreDirectives.FILTER_NAME)
        .getArgument(CoreDirectives.FILTER_ARG_FIELD);
  }

  private List<IRI> fetchSubjects(DataFetchingEnvironment environment, QueryEnvironment queryEnvironment,
      List<DirectiveContainerObject> filterMapping, Map<String, Object> arguments,
      RepositoryAdapter repositoryAdapter) {

    GraphQLDirective sparqlDirective = environment.getFieldDefinition()
        .getDirective(Rdf4jDirectives.SPARQL_NAME);

    List<Object> orderByObject = getOrderByObject(environment, arguments, queryEnvironment);

    String subjectTemplate =
        DirectiveUtils.getArgument(Rdf4jDirectives.SPARQL_ARG_SUBJECT, sparqlDirective, String.class);

    if (subjectTemplate != null) {
      StringSubstitutor substitutor = new StringSubstitutor(arguments);
      IRI subject = VF.createIRI(substitutor.replace(subjectTemplate));

      return ImmutableList.of(subject);
    }

    String subjectQuery = SubjectQueryBuilder.create(queryEnvironment, jexlEngine, selectVerticeFactory)
        .getQueryString(arguments, sparqlDirective, filterMapping, orderByObject);

    LOG.debug("Executing query for subjects:\n{}", subjectQuery);

    String repositoryId =
        DirectiveUtils.getArgument(Rdf4jDirectives.SPARQL_ARG_REPOSITORY, sparqlDirective, String.class);

    TupleQueryResult queryResult = repositoryAdapter.prepareTupleQuery(repositoryId, environment, subjectQuery)
        .evaluate();

    return QueryResults.asList(queryResult)
        .stream()
        .map(bindings -> (IRI) bindings.getValue("s"))
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private List<Object> getOrderByObject(DataFetchingEnvironment environment, Map<String, Object> arguments,
      QueryEnvironment queryEnvironment) {
    Optional<GraphQLArgument> sortOptional = environment.getFieldDefinition()
        .getArguments()
        .stream()
        .filter(argument -> Objects.nonNull(argument.getDirective(CoreDirectives.SORT_NAME)))
        .findFirst();

    List<Object> orderByObject;
    if (sortOptional.isPresent()) {
      GraphQLArgument sortArgument = sortOptional.get();
      if (Objects.nonNull(arguments.get(sortArgument.getName()))) {
        List<Map<String, String>> sortArgsMap = (List<Map<String, String>>) arguments.get(sortArgument.getName());

        orderByObject = sortArgsMap.stream()
            .map(determineOrderableIsResource(environment))
            .collect(Collectors.toList());
      } else {
        orderByObject = (List<Object>) sortArgument.getDefaultValue();
      }
    } else {
      orderByObject = Collections.emptyList();
    }
    return orderByObject;
  }

  private Function<Map<String, String>, Map<String, String>> determineOrderableIsResource(
      DataFetchingEnvironment environment) {
    return sortArgs -> {
      Optional.ofNullable(getField(environment.getFieldDefinition(), sortArgs.get("field")))
          .flatMap(definition -> Optional.ofNullable(definition.getDirective(Rdf4jDirectives.RESOURCE_NAME)))
          .ifPresent(directive -> sortArgs.put("isResource", "true"));
      return sortArgs;
    };
  }

  private GraphQLFieldDefinition getField(GraphQLFieldDefinition environment, String pad) {

    List<String> path = new ArrayList<>(Arrays.asList(pad.split("\\.")));

    GraphQLFieldDefinition fieldDefinition =
        ((GraphQLObjectType) GraphQLTypeUtil.unwrapAll(environment.getType())).getFieldDefinition(path.get(0));

    if (path.size() > 1) {
      path.remove(0);
      return getField(environment, String.join(".", path));
    }
    return fieldDefinition;
  }

  private Model fetchGraph(DataFetchingEnvironment environment, QueryEnvironment queryEnvironment, List<IRI> subjects,
      RepositoryAdapter repositoryAdapter) {

    if (subjects.isEmpty()) {
      return new TreeModel();
    }

    String graphQuery = GraphQueryBuilder.create(queryEnvironment, subjects, constructVerticeFactory)
        .getQueryString();

    LOG.debug("Executing query for graph:\n{}", graphQuery);

    GraphQLDirective sparqlDirective = environment.getFieldDefinition()
        .getDirective(Rdf4jDirectives.SPARQL_NAME);

    String repositoryId =
        DirectiveUtils.getArgument(Rdf4jDirectives.SPARQL_ARG_REPOSITORY, sparqlDirective, String.class);

    GraphQueryResult queryResult = repositoryAdapter
        .prepareGraphQuery(repositoryId, environment, graphQuery, subjects.stream()
            .map(IRI::toString)
            .collect(Collectors.toList()))
        .evaluate();

    Model result = QueryResults.asModel(queryResult);
    LOG.debug("Fetched [{}] triples", result.size());
    return result;
  }
}
