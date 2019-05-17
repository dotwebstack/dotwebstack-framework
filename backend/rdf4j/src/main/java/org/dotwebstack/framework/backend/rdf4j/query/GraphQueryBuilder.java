package org.dotwebstack.framework.backend.rdf4j.query;

import com.google.common.collect.Iterables;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.List;
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


  private List<TriplePattern> getTriplePatterns(List<SelectedField> fields, NodeShape nodeShape, Variable subject) {
    return fields
        .stream()
        .filter(field -> !field.getQualifiedName().contains("/"))
        .flatMap(field -> {
          GraphQLOutputType fieldType = field.getFieldDefinition().getType();

          PropertyShape propertyShape = nodeShape.getPropertyShape(field.getName());
          List<TriplePattern> result = new ArrayList<>();
          Variable variable = query.var();

          result.add(GraphPatterns.tp(subject, propertyShape.getPath().toPredicate(), variable));

          if (!GraphQLTypeUtil.isLeaf(fieldType)) {
            NodeShape nodeShape1 = environment.getNodeShapeRegistry().get((IRI) propertyShape.getIdentifier());
            result.addAll(getTriplePatterns(field.getSelectionSet().getFields(), nodeShape1, variable));
          }

          return result.stream();
        }).collect(Collectors.toList());
  }

  String getQueryString() {
    Variable subjectVar = query.var();
    NodeShape nodeShape = environment.getNodeShapeRegistry().get(environment.getObjectType());

    List<TriplePattern> triplePatterns = getTriplePatterns(
        environment.getSelectionSet().getFields(),nodeShape,subjectVar);

    Expression<?> filterExpr = Expressions
        .or(Iterables.toArray(subjects
            .stream()
            .map(subject -> Expressions.equals(subjectVar, Rdf.iri(subject)))
            .collect(Collectors.toList()), Expression.class));

    List<GraphPattern> wherePatterns = triplePatterns
        .stream()
        .map(GraphPatterns::optional)
        .collect(Collectors.toList());

    // Fetch type statement to discover if subject exists (e.g. in case of only nullable fields)
    TriplePattern typePattern = GraphPatterns
        .tp(subjectVar, RDF.TYPE, nodeShape.getTargetClass());

    query
        .construct(typePattern)
        .construct(Iterables.toArray(triplePatterns, TriplePattern.class))
        .where(typePattern
            .filter(filterExpr)
            .and(Iterables.toArray(wherePatterns, GraphPattern.class)));

    return query.getQueryString();
  }
}
