package org.dotwebstack.framework.backend.rdf4j.query;

import com.google.common.collect.Iterables;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeUtil;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
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

  private final DataFetchingFieldSelectionSet selectionSet;

  private GraphQueryBuilder(GraphQLObjectType objectType, List<IRI> subjects,
      DataFetchingFieldSelectionSet selectionSet, NodeShapeRegistry nodeShapeRegistry,
      Map<String, String> prefixMap) {
    super(objectType, nodeShapeRegistry, prefixMap, Queries.CONSTRUCT());
    this.subjects = subjects;
    this.selectionSet = selectionSet;
  }

  static GraphQueryBuilder create(GraphQLObjectType objectType, List<IRI> subjects,
      DataFetchingFieldSelectionSet selectionSet, NodeShapeRegistry nodeShapeRegistry,
      Map<String, String> prefixMap) {
    return new GraphQueryBuilder(objectType, subjects, selectionSet, nodeShapeRegistry, prefixMap);
  }

  String getQueryString() {
    Variable subjectVar = query.var();
    NodeShape nodeShape = nodeShapeRegistry.get(objectType);

    List<TriplePattern> triplePatterns = selectionSet
        .getFields()
        .stream()
        .map(field -> {
          GraphQLOutputType fieldType = field.getFieldDefinition().getType();

          if (GraphQLTypeUtil.isLeaf(fieldType)) {
            PropertyShape propertyShape = nodeShape.getPropertyShape(field.getName());
            return GraphPatterns.tp(subjectVar, ns(propertyShape.getPath()), query.var());
          } else {
            throw new UnsupportedOperationException("Non-leaf nodes are not yet supported.");
          }
        })
        .collect(Collectors.toList());

    Expression<?> filterExpr = Expressions
        .or(Iterables.toArray(subjects
            .stream()
            .map(subject -> Expressions.equals(subjectVar, ns(subject)))
            .collect(Collectors.toList()), Expression.class));

    List<GraphPattern> wherePatterns = triplePatterns
        .stream()
        .map(GraphPatterns::optional)
        .collect(Collectors.toList());

    // Fetch type statement to discover if subject exists (e.g. in case of only nullable fields)
    TriplePattern typePattern = GraphPatterns
        .tp(subjectVar, ns(RDF.TYPE), ns(nodeShape.getTargetClass()));

    query
        .construct(typePattern)
        .construct(Iterables.toArray(triplePatterns, TriplePattern.class))
        .where(typePattern
            .filter(filterExpr)
            .and(Iterables.toArray(wherePatterns, GraphPattern.class)));

    return query.getQueryString();
  }

}
