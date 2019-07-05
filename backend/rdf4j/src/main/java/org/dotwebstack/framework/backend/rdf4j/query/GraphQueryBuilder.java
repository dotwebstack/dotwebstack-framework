package org.dotwebstack.framework.backend.rdf4j.query;

import com.google.common.collect.Iterables;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.backend.rdf4j.expression.ExpressionContext;
import org.dotwebstack.framework.backend.rdf4j.expression.ExpressionHelper;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.FilterJoinType;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.helpers.ObjectHelper;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ConstructQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;

class GraphQueryBuilder extends AbstractQueryBuilder<ConstructQuery> {

  private Variable rootSubject;

  private final List<IRI> subjects;

  private Map<String, List<TriplePattern>> nestedTriples = new HashMap<>();

  private List<GraphPattern> nonOptionals = new ArrayList<>();

  private final List<ExpressionContext> filters = new ArrayList<>();

  private GraphQueryBuilder(QueryEnvironment environment, List<IRI> subjects) {
    super(environment, Queries.CONSTRUCT());
    this.subjects = subjects;
  }

  static GraphQueryBuilder create(QueryEnvironment environment, List<IRI> subjects) {
    return new GraphQueryBuilder(environment, subjects);
  }

  String getQueryString() {
    rootSubject = query.var();
    NodeShape nodeShape = environment.getNodeShapeRegistry()
        .get(environment.getObjectType());

    List<TriplePattern> triplePatterns = getTriplePatterns(environment.getSelectionSet()
        .getFields(), nodeShape, rootSubject);

    List<GraphPattern> wherePatterns = triplePatterns.stream()
        .map(this::getGraphPattern)
        .collect(Collectors.toList());

    // Fetch type statement to discover if subject exists (e.g. in case of only nullable fields)
    TriplePattern typePattern = GraphPatterns.tp(rootSubject, RDF.TYPE, nodeShape.getTargetClass());

    triplePatterns.addAll(nestedTriples.values()
        .stream()
        .flatMap(List::stream)
        .collect(Collectors.toList()));

    Expression<?> filterExpr = Expressions.or(Iterables.toArray(subjects.stream()
        .map(subject -> Expressions.equals(rootSubject, Rdf.iri(subject)))
        .collect(Collectors.toList()), Expression.class));

    query.construct(typePattern)
        .construct(Iterables.toArray(triplePatterns, TriplePattern.class))
        .where(typePattern.filter(filterExpr)
            .and(Iterables.toArray(wherePatterns, GraphPattern.class)));

    return query.getQueryString();
  }

  private List<TriplePattern> getTriplePatterns(List<SelectedField> fields, NodeShape nodeShape, Variable subject) {
    return fields.stream()
        .filter(field -> !field.getQualifiedName()
            .contains("/"))
        .flatMap(field -> getTriplePatterns(field, nodeShape, subject).stream())
        .collect(Collectors.toList());
  }

  private List<TriplePattern> getTriplePatterns(SelectedField field, NodeShape nodeShape, Variable subject) {
    GraphQLOutputType fieldType = field.getFieldDefinition()
        .getType();

    PropertyShape propertyShape = nodeShape.getPropertyShape(field.getName());
    List<TriplePattern> result = new ArrayList<>();
    Variable variable = query.var();

    TriplePattern triple = GraphPatterns.tp(subject, propertyShape.getPath()
        .toPredicate(), variable);

    if (rootSubject.getQueryString()
        .equals(subject.getQueryString())) {
      result.add(triple);
    } else {
      List<TriplePattern> triples = nestedTriples.getOrDefault(subject.getQueryString(), new ArrayList<>());
      triples.add(triple);
      if (GraphQLTypeUtil.isList(field.getFieldDefinition()
          .getType()) && field.getArguments() != null) {
        field.getFieldDefinition()
            .getArguments()
            .stream()
            .filter(argument -> GraphQLTypeUtil.isList(field.getFieldDefinition()
                .getType()) && argument.getDirective(CoreDirectives.FILTER_NAME) != null)
            .forEach(argument -> {
              String argumentName = !Objects.isNull(argument.getDirective(CoreDirectives.FILTER_NAME)
                  .getArgument(CoreDirectives.FILTER_ARG_FIELD)
                  .getValue()) ? (String) argument.getDirective(CoreDirectives.FILTER_NAME)
                      .getArgument(CoreDirectives.FILTER_ARG_FIELD)
                      .getValue() : argument.getName();

              List<Object> filterArguments;
              Object filterValue = field.getArguments()
                  .get(argument.getName());
              if (filterValue instanceof List) {
                filterArguments = ObjectHelper.castToList(filterValue);
              } else {
                filterArguments = Collections.singletonList(filterValue);
              }

              List<Operand> operands = filterArguments.stream()
                  .map(filterArgument -> ExpressionHelper.getOperand(nodeShape.getPropertyShape(field.getName())
                      .getNode(), argumentName, filterArgument))
                  .collect(Collectors.toList());

              filters.add(ExpressionContext.builder()
                  .operator(FilterOperator.getByValue((String) argument.getDirective(CoreDirectives.FILTER_NAME)
                      .getArgument(CoreDirectives.FILTER_ARG_OPERATOR)
                      .getValue())
                      .orElse(FilterOperator.EQ))
                  .operands(operands)
                  .joinType(FilterJoinType.AND)
                  .parent(variable)
                  .nodeShape(nodeShape.getPropertyShape(field.getName())
                      .getNode())
                  .propertyShape(nodeShape.getPropertyShape(field.getName())
                      .getNode()
                      .getPropertyShape(argumentName))
                  .build());
            });
      }
      nestedTriples.put(subject.getQueryString(), triples);
    }

    if (propertyShape.getNode() != null) {
      List<TriplePattern> triples = nestedTriples.getOrDefault(variable.getQueryString(), new ArrayList<>());
      TriplePattern nonOptional = GraphPatterns.tp(variable, RDF.TYPE, propertyShape.getNode()
          .getTargetClass());
      triples.add(nonOptional);
      nestedTriples.put(variable.getQueryString(), triples);
      nonOptionals.add(nonOptional);
    }

    if (!GraphQLTypeUtil.isLeaf(fieldType)) {
      GraphQLType innerType = GraphQLTypeUtil.unwrapAll(fieldType);

      if (innerType instanceof GraphQLObjectType) {
        NodeShape childShape = environment.getNodeShapeRegistry()
            .get((GraphQLObjectType) innerType);
        result.addAll(getTriplePatterns(field.getSelectionSet()
            .getFields(), childShape, variable));
      } else {
        throw ExceptionHelper
            .unsupportedOperationException("SPARQL triple pattern construction for type {} not supported!", innerType);
      }
    }

    return result;
  }

  private GraphPattern getGraphPattern(GraphPattern triple) {
    String tripleObject = getObject(triple.getQueryString());

    if (nestedTriples.containsKey(tripleObject)) {
      List<GraphPattern> triples = nestedTriples.get(tripleObject)
          .stream()
          .map(this::getGraphPattern)
          .collect(Collectors.toList());

      // add the triples for unselected filter fields
      getTripleFiltersForUnselectedField(tripleObject, triples).forEach(filter -> {
        Variable variable = query.var();

        Expression<?> expression =
            ExpressionHelper.buildExpressionFromOperands(null, variable, filter.getOperator(), filter.getOperands());

        GraphPattern pattern = GraphPatterns.tp(filter.getParent(), filter.getPropertyShape()
            .getPath()
            .toPredicate(), variable)
            .filter(expression);

        nonOptionals.add(pattern);
        triples.add(pattern);
      });

      filters.stream()
          .filter(filter -> filter.getParent()
              .getQueryString()
              .equals(tripleObject))
          .filter(filter -> triples.stream()
              .map(statement -> stripQueryString(statement.getQueryString()))
              .anyMatch(query -> filter.getPropertyShape()
                  .getPath()
                  .toPredicate()
                  .getQueryString()
                  .equals(getPredicate(query))))
          .forEach(filter -> triples.stream()
              .filter(statement -> {
                String queryString = stripQueryString(statement.getQueryString());
                return filter.getPropertyShape()
                    .getPath()
                    .toPredicate()
                    .getQueryString()
                    .equals(getPredicate(queryString));
              })
              .forEach(statement -> {
                String queryString = stripQueryString(statement.getQueryString());
                Variable variable = SparqlBuilder.var(getObject(queryString).replace("?", ""));

                Expression<?> expression = ExpressionHelper.buildExpressionFromOperands(null, variable,
                    filter.getOperator(), filter.getOperands());
                if (!nonOptionals.contains(statement)) {
                  nonOptionals.add(statement);
                  statement.optional(false);
                }
                statement.filter(expression);
              }));

      return triple.optional()
          .and(triples.stream()
              .map(additionalTriple -> {
                if (!nonOptionals.contains(additionalTriple)) {
                  return additionalTriple.optional();
                }
                return additionalTriple;
              })
              .collect(Collectors.toList())
              .toArray(new GraphPattern[triples.size()]))
          .optional();
    }
    return nonOptionals.contains(triple) ? triple : triple.optional();
  }

  private Stream<ExpressionContext> getTripleFiltersForUnselectedField(String tripleObject,
      List<GraphPattern> triples) {
    return filters.stream()
        .filter(filter -> filter.getParent()
            .getQueryString()
            .equals(tripleObject))
        .filter(filter -> triples.stream()
            .map(statement -> stripQueryString(statement.getQueryString()))
            .noneMatch(query -> filter.getPropertyShape()
                .getPath()
                .toPredicate()
                .getQueryString()
                .equals(getPredicate(query))));
  }

  private String stripQueryString(String queryString) {
    return queryString
        // strip the outer (optional etc) statements from the triple
        .replaceAll("^.*\\{", "")
        .replaceAll("}.*$", "")

        // remove any trailing characters (. , ;) etc
        .replaceAll("[\\.;,][ ]*$", "")
        .trim();
  }

  private String getPredicate(String queryString) {
    return queryString.split(" ")[1];
  }

  private String getObject(String queryString) {
    return queryString.split(" ")[2];
  }
}
