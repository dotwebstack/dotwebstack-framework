package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.query.context.ConstructVerticeFactory;
import org.dotwebstack.framework.backend.rdf4j.query.context.Vertice;
import org.dotwebstack.framework.backend.rdf4j.query.context.VerticeHelper;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ConstructQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;

class GraphQueryBuilder extends AbstractQueryBuilder<ConstructQuery> {

  private final List<IRI> subjects;

  private final ConstructVerticeFactory constructVerticeFactory;

  private GraphQueryBuilder(@NonNull QueryEnvironment environment, @NonNull List<IRI> subjects,
      @NonNull ConstructVerticeFactory constructVerticeFactory) {
    super(environment, Queries.CONSTRUCT());
    this.subjects = subjects;
    this.constructVerticeFactory = constructVerticeFactory;
  }

  static GraphQueryBuilder create(QueryEnvironment environment, List<IRI> subjects,
      @NonNull ConstructVerticeFactory constructVerticeFactory) {
    return new GraphQueryBuilder(environment, subjects, constructVerticeFactory);
  }

  String getQueryString() {
    NodeShape nodeShape = environment.getNodeShapeRegistry()
        .get(environment.getObjectType());

    Variable subjectVariable = query.var();

    Vertice root = constructVerticeFactory.createRoot(subjectVariable, query, nodeShape, environment.getSelectionSet()
        .getFields());

    query.construct(VerticeHelper.getConstructPatterns(root)
        .toArray(new TriplePattern[] {}))
        .where(VerticeHelper.getWherePatterns(root, true)
            .toArray(new GraphPattern[] {}));

    String queryString = query.getQueryString();
    if (queryString.contains("SELECT")) {
      String[] splitted = queryString.split("WHERE \\{");
      Stream.iterate(2, index -> index < splitted.length, index -> index + 1)
          .forEach(index -> {
           splitted[index] = createValuesBlock(subjects, subjectVariable) + splitted[index] ;
          });
      return String.join("WHERE {", splitted);
    }
    return queryString.replace("WHERE {", "WHERE {" + createValuesBlock(subjects, subjectVariable));
  }

  private String createValuesBlock(List<IRI> subjects, Variable subjectVariable) {
    String subjectString = subjects.stream()
        .map(iri -> "<" + iri + ">")
        .collect(Collectors.joining(" "));
    return String.format("VALUES %s {%s} ", subjectVariable.getQueryString(), subjectString);
  }
}
