package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.List;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.rdf4j.query.context.Vertice;
import org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactory;
import org.dotwebstack.framework.backend.rdf4j.query.context.VerticeHelper;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ConstructQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;

class GraphQueryBuilder extends AbstractQueryBuilder<ConstructQuery> {

  private final List<IRI> subjects;

  private GraphQueryBuilder(QueryEnvironment environment, List<IRI> subjects) {
    super(environment, Queries.CONSTRUCT());
    this.subjects = subjects;
  }

  static GraphQueryBuilder create(QueryEnvironment environment, List<IRI> subjects) {
    return new GraphQueryBuilder(environment, subjects);
  }

  String getQueryString() {
    NodeShape nodeShape = environment.getNodeShapeRegistry()
        .get(environment.getObjectType());

    Vertice root = VerticeFactory.createVertice(query.var(), query, nodeShape, environment.getSelectionSet()
        .getFields());

    TriplePattern typePattern = GraphPatterns.tp(root.getSubject(), RDF.TYPE, nodeShape.getTargetClass());

    Expression<?> filterExpr = Expressions.or(subjects.stream()
        .map(subject -> Expressions.equals(root.getSubject(), Rdf.iri(subject)))
        .collect(Collectors.toList())
        .toArray(new Expression<?>[] {}));

    query.construct(VerticeHelper.getConstructPatterns(root)
        .toArray(new TriplePattern[] {}))
        .where(typePattern.filter(filterExpr)
            .and(VerticeHelper.getWherePatterns(root)
                .toArray(new GraphPattern[] {})));

    return query.getQueryString();
  }
}
