package org.dotwebstack.framework.backend.rdf4j.query;

import com.google.common.collect.ImmutableList;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.core.graphql.directives.DirectiveUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;

@Slf4j
@RequiredArgsConstructor
public final class QueryFetcher implements DataFetcher<Object> {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private final RepositoryConnection repositoryConnection;

  private final NodeShapeRegistry nodeShapeRegistry;

  private final Map<String, String> prefixMap;

  @Override
  public Object get(@NonNull DataFetchingEnvironment environment) {
    GraphQLDirective sparqlDirective = environment.getFieldDefinition()
        .getDirective(Rdf4jDirectives.SPARQL_NAME);
    GraphQLType outputType = GraphQLTypeUtil.unwrapNonNull(environment.getFieldType());
    GraphQLUnmodifiedType rawType = GraphQLTypeUtil.unwrapAll(outputType);

    if (!(rawType instanceof GraphQLObjectType)) {
      throw new UnsupportedOperationException(
          "Field types other than object fields are not yet supported.");
    }

    GraphQLObjectType objectType = (GraphQLObjectType) rawType;

    // Find shapes matching request
    List<IRI> subjects = fetchSubjects(sparqlDirective, environment.getArguments(), objectType,
        repositoryConnection);

    // Fetch graph for given subjects
    Model model = fetchGraph(subjects, environment.getSelectionSet(), objectType,
        repositoryConnection);

    if (GraphQLTypeUtil.isList(outputType)) {
      return subjects.stream()
          .map(subject -> new QuerySolution(model, subject))
          .collect(Collectors.toList());
    }

    return model.isEmpty() ? null : new QuerySolution(model, subjects.get(0));
  }

  private List<IRI> fetchSubjects(GraphQLDirective sparqlDirective, Map<String, Object> arguments,
      GraphQLObjectType objectType, RepositoryConnection con) {
    String subjectTemplate = DirectiveUtils
        .getStringArgument(Rdf4jDirectives.SPARQL_ARG_SUBJECT, sparqlDirective);

    if (subjectTemplate != null) {
      StringSubstitutor substitutor = new StringSubstitutor(arguments);
      IRI subject = VF.createIRI(substitutor.replace(subjectTemplate));

      return ImmutableList.of(subject);
    }

    String subjectQuery = SubjectQueryBuilder
        .create(objectType, nodeShapeRegistry, prefixMap)
        .getQueryString();

    LOG.debug("Exececuting query for subjects:\n{}", subjectQuery);

    return QueryResults.asList(con.prepareTupleQuery(subjectQuery).evaluate())
        .stream()
        .map(bindings -> (IRI) bindings.getValue("s"))
        .collect(Collectors.toList());
  }

  private Model fetchGraph(List<IRI> subjects, DataFetchingFieldSelectionSet selectionSet,
      GraphQLObjectType objectType, RepositoryConnection con) {
    if (subjects.isEmpty()) {
      return new TreeModel();
    }

    String graphQuery = GraphQueryBuilder
        .create(objectType, subjects, selectionSet, nodeShapeRegistry, prefixMap)
        .getQueryString();

    LOG.debug("Exececuting query for graph:\n{}", graphQuery);

    return QueryResults.asModel(con.prepareGraphQuery(graphQuery).evaluate());
  }

  private Iri fromIri(IRI iri) {
    return Rdf.iri(iri.getNamespace(), iri.getLocalName());
  }

}
