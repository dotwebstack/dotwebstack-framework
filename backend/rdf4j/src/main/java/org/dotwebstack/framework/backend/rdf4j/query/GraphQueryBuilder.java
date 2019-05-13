package org.dotwebstack.framework.backend.rdf4j.query;

import com.google.common.collect.Iterables;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeUtil;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ConstructQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;

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
    Variable subjectVar = query.var();
    NodeShape nodeShape = environment.getNodeShapeRegistry().get(environment.getObjectType());

    final Map<String, TriplePattern> whereStatements = environment.getSelectionSet()
        .getFields()
        .stream()
        .map(field -> {
          GraphQLOutputType fieldType = field.getFieldDefinition().getType();

          if (GraphQLTypeUtil.isLeaf(fieldType)) {
            return nodeShape.getPropertyShape(field.getName());
          } else { //detect
            throw new UnsupportedOperationException("Non-leaf nodes are not yet supported.");
          }
        }).collect(Collectors.toMap(
            PropertyShape::getName,
            propertyShape -> GraphPatterns.tp(subjectVar, toPredicate(propertyShape.getPath()),
                query.var())));

    Expression<?> filterExpr = Expressions
        .or(Iterables.toArray(subjects
            .stream()
            .map(subject -> Expressions.equals(subjectVar, ns(subject)))
            .collect(Collectors.toList()), Expression.class));

    List<GraphPattern> wherePatterns = whereStatements.values()
        .stream()
        .map(GraphPatterns::optional)
        .collect(Collectors.toList());

    // Fetch type statement to discover if subject exists (e.g. in case of only nullable fields)
    TriplePattern typePattern = GraphPatterns
        .tp(subjectVar, ns(RDF.TYPE), ns(nodeShape.getTargetClass()));

    query
        .construct(typePattern)
        .construct(Iterables.toArray(whereStatements.values(), TriplePattern.class))

        .where(typePattern
            .filter(filterExpr)
            .and(Iterables.toArray(wherePatterns, GraphPattern.class)));

    return query.getQueryString();
  }
}
