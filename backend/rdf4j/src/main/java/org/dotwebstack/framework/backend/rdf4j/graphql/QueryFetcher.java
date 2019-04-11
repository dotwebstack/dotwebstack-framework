package org.dotwebstack.framework.backend.rdf4j.graphql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
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
import org.dotwebstack.framework.backend.rdf4j.graphql.directives.Directives;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.graphql.directives.DirectiveUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ConstructQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;

@Slf4j
@RequiredArgsConstructor
public final class QueryFetcher implements DataFetcher<Object> {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private final RepositoryConnection repositoryConnection;

  private final NodeShapeRegistry nodeShapeRegistry;

  @Override
  public Object get(@NonNull DataFetchingEnvironment environment) {
    GraphQLDirective sparqlDirective = environment.getFieldDefinition()
        .getDirective(Directives.SPARQL_NAME);
    GraphQLType outputType = GraphQLTypeUtil.unwrapNonNull(environment.getFieldType());
    GraphQLUnmodifiedType rawType = GraphQLTypeUtil.unwrapAll(outputType);

    if (!(rawType instanceof GraphQLObjectType)) {
      throw new UnsupportedOperationException(
          "Field types other than object fields are not yet supported.");
    }

    NodeShape nodeShape = nodeShapeRegistry.get((GraphQLObjectType) rawType);

    // Find shapes matching request
    List<IRI> subjects = fetchSubjects(sparqlDirective, environment.getArguments(), nodeShape,
        repositoryConnection);

    // Fetch graph for given subjects
    Model model = fetchGraph(subjects, environment.getSelectionSet(), nodeShape,
        repositoryConnection);

    if (GraphQLTypeUtil.isList(outputType)) {
      return subjects.stream()
          .map(subject -> new QuerySolution(model, subject))
          .collect(Collectors.toList());
    }

    return model.isEmpty() ? null : new QuerySolution(model, subjects.get(0));
  }

  private List<IRI> fetchSubjects(GraphQLDirective sparqlDirective, Map<String, Object> arguments,
      NodeShape nodeShape, RepositoryConnection con) {
    String subjectTemplate = DirectiveUtils
        .getStringArgument(Directives.SPARQL_ARG_SUBJECT, sparqlDirective);

    if (subjectTemplate != null) {
      StringSubstitutor substitutor = new StringSubstitutor(arguments);
      IRI subject = VF.createIRI(substitutor.replace(subjectTemplate));

      return ImmutableList.of(subject);
    }

    Variable subjectVar = SparqlBuilder.var("s");

    SelectQuery query = Queries.SELECT()
        .select(subjectVar)
        .where(GraphPatterns.tp(subjectVar, RDF.TYPE, nodeShape.getTargetClass()));

    String queryStr = query.getQueryString();
    LOG.debug("Exececuting query for subjects:\n{}", queryStr);

    return QueryResults.asList(con.prepareTupleQuery(queryStr).evaluate())
        .stream()
        .map(bindings -> (IRI) bindings.getValue("s"))
        .collect(Collectors.toList());
  }

  private Model fetchGraph(List<IRI> subjects, DataFetchingFieldSelectionSet selectionSet,
      NodeShape nodeShape, RepositoryConnection con) {
    if (subjects.isEmpty()) {
      return new ModelBuilder().build();
    }

    ConstructQuery query = Queries.CONSTRUCT();
    Variable subjectVar = query.var();

    List<TriplePattern> triplePatterns = selectionSet
        .getFields()
        .stream()
        .map(field -> {
          GraphQLOutputType fieldType = field.getFieldDefinition().getType();

          if (GraphQLTypeUtil.isLeaf(fieldType)) {
            PropertyShape propertyShape = nodeShape.getPropertyShape(field.getName());
            return GraphPatterns.tp(subjectVar, propertyShape.getPath(), query.var());
          } else {
            throw new UnsupportedOperationException("Non-leaf nodes are not yet supported.");
          }
        })
        .collect(Collectors.toList());

    Expression filterExpr = Expressions.or(Iterables.toArray(subjects
        .stream()
        .map(subject -> Expressions.equals(subjectVar, Rdf.iri(subject)))
        .collect(Collectors.toList()), Expression.class));

    List<GraphPattern> wherePatterns = triplePatterns
        .stream()
        .map(GraphPatterns::optional)
        .collect(Collectors.toList());

    // Fetch type statement to discover if subject exists (e.g. in case of only nullable fields)
    TriplePattern typePattern = GraphPatterns.tp(subjectVar, RDF.TYPE, nodeShape.getTargetClass());

    query.construct(typePattern);
    query.construct(Iterables.toArray(triplePatterns, TriplePattern.class))
        .where(typePattern
            .filter(filterExpr)
            .and(Iterables.toArray(wherePatterns, GraphPattern.class)));

    String queryStr = query.getQueryString();
    LOG.debug("Exececuting query for graph:\n{}", queryStr);

    return QueryResults.asModel(con.prepareGraphQuery(queryStr).evaluate());
  }

}
