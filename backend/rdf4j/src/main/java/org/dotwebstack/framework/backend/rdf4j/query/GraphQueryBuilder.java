package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.query.helper.QueryBuilderHelper;
import org.dotwebstack.framework.backend.rdf4j.query.model.Vertice;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ConstructQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;

class GraphQueryBuilder extends AbstractQueryBuilder<ConstructQuery> {

  private final List<IRI> subjects;

  private final VerticeFactory constructVerticeFactory;

  private GraphQueryBuilder(@NonNull QueryEnvironment environment, @NonNull List<IRI> subjects,
      @NonNull VerticeFactory constructVerticeFactory) {
    super(environment, Queries.CONSTRUCT());
    this.subjects = subjects;
    this.constructVerticeFactory = constructVerticeFactory;
  }

  static GraphQueryBuilder create(QueryEnvironment environment, List<IRI> subjects,
      @NonNull VerticeFactory constructVerticeFactory) {
    return new GraphQueryBuilder(environment, subjects, constructVerticeFactory);
  }

  String getQueryString() {
    NodeShape nodeShape = environment.getNodeShapeRegistry()
        .get(environment.getObjectType());

    Vertice root = constructVerticeFactory.buildConstructQuery(nodeShape, environment.getSelectionSet()
        .getFields(), query);
    query.construct(QueryBuilderHelper.getConstructPatterns(root)
        .toArray(new TriplePattern[] {}))
        .where(QueryBuilderHelper.getWherePatterns(root)
            .toArray(new GraphPattern[] {}));

    String queryString = query.getQueryString();
    if (queryString.contains("SELECT")) {
      String[] splitted = queryString.split("WHERE \\{");
      Stream.iterate(2, index -> index < splitted.length, index -> index + 1)
          .forEach(index -> {
            splitted[index] = createValuesBlock(subjects, root.getSubject()) + splitted[index];
          });
      return String.join("WHERE {", splitted);
    }
    return queryString.replace("WHERE {", "WHERE {" + createValuesBlock(subjects, root.getSubject()));
  }

  private String createValuesBlock(List<IRI> subjects, Variable subjectVariable) {
    String subjectString = subjects.stream()
        .map(iri -> "<" + iri + ">")
        .collect(Collectors.joining(" "));
    return String.format("VALUES %s {%s} ", subjectVariable.getQueryString(), subjectString);
  }
}
