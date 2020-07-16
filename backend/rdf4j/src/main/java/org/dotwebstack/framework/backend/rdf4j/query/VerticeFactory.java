package org.dotwebstack.framework.backend.rdf4j.query;

import static java.util.Objects.nonNull;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.AggregateHelper.resolveAggregate;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.ConstraintHelper.buildConstraints;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.ConstraintHelper.resolveRequiredEdges;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.EdgeHelper.buildEdge;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.EdgeHelper.isEqualToEdge;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.FieldPathHelper.filteredFields;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.FieldPathHelper.getFieldPath;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.FilterHelper.addLanguageFilter;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.FilterHelper.buildFilterRule;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.FilterHelper.buildOperands;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.PathHelper.resolvePath;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.SortHelper.findOrderVariable;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.SortHelper.getDefaultOrderByValue;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToList;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.query.model.Aggregate;
import org.dotwebstack.framework.backend.rdf4j.query.model.ArgumentFieldMapping;
import org.dotwebstack.framework.backend.rdf4j.query.model.Edge;
import org.dotwebstack.framework.backend.rdf4j.query.model.Filter;
import org.dotwebstack.framework.backend.rdf4j.query.model.FilterRule;
import org.dotwebstack.framework.backend.rdf4j.query.model.OrderBy;
import org.dotwebstack.framework.backend.rdf4j.query.model.PathType;
import org.dotwebstack.framework.backend.rdf4j.query.model.Vertice;
import org.dotwebstack.framework.backend.rdf4j.serializers.SerializerRouter;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
import org.eclipse.rdf4j.sparqlbuilder.core.Orderable;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.springframework.stereotype.Component;

@Component
public class VerticeFactory {

  private SerializerRouter serializerRouter;

  private Rdf4jProperties rdf4jProperties;

  private String language;

  public VerticeFactory(SerializerRouter serializerRouter, Rdf4jProperties rdf4jProperties) {
    this.serializerRouter = serializerRouter;
    this.rdf4jProperties = rdf4jProperties;
    this.language = rdf4jProperties.getShape() != null ? rdf4jProperties.getShape()
        .getLanguage() : null;
  }

  public Vertice buildConstructQuery(@NonNull NodeShape nodeShape, @NonNull List<SelectedField> fields,
      @NonNull OuterQuery<?> query) {
    Vertice root = createVertice(nodeShape, fields, null, query);
    buildConstraints(root, query);
    return root;
  }

  public Vertice buildSelectQuery(NodeShape nodeShape, List<FilterRule> filterRules, List<OrderBy> orderBys,
      OuterQuery<?> query) {
    Vertice root = createVertice(nodeShape, Collections.emptyList(), null, query);

    // These are the filters and sorts that can be provided by the user
    orderBys.forEach(orderBy -> addOrderBy(root, nodeShape, query, orderBy));
    filterRules.forEach(filterRule -> addFilterRule(root, nodeShape, query, filterRule));

    buildConstraints(root, query);
    return root;
  }

  private Vertice createVertice(NodeShape nodeShape, List<SelectedField> fields, FieldPath fieldPath,
      OuterQuery<?> query) {
    Variable subject = query.var();
    List<Edge> edges = resolveEdgesFromSelectedFields(nodeShape, fields, fieldPath, query);

    edges.addAll(resolveRequiredEdges(getUnselectedPropertyShapes(nodeShape.getPropertyShapes()
        .values(), edges), query));

    return Vertice.builder()
        .nodeShape(nodeShape)
        .subject(subject)
        .edges(edges)
        .build();
  }

  private void addFilterRule(Vertice vertice, NodeShape nodeShape, OuterQuery<?> query, FilterRule filterRule) {
    FieldPath fieldPath = filterRule.getFieldPath();
    if (fieldPath.isResource()) {
      vertice.addFilter(createFilter(nodeShape, filterRule, null));
      return;
    }

    nodeShape.getChildNodeShape(fieldPath.getFieldDefinitions())
        .ifPresentOrElse(childShape -> {
          if (fieldPath.rest()
              .isEmpty()) {
            resolveLeafFilter(vertice, nodeShape, query, filterRule, fieldPath, childShape, query.var());
          }
          resolveNodeFilter(vertice, nodeShape, query, filterRule, fieldPath, childShape);
        }, () -> resolveFilter(vertice, query, nodeShape, filterRule, PathType.FILTER));
  }

  private void resolveNodeFilter(Vertice vertice, NodeShape nodeShape, OuterQuery<?> query, FilterRule filterRule,
      FieldPath fieldPath, NodeShape childShape) {
    fieldPath.rest()
        .map(rest -> FilterRule.builder()
            .fieldPath(rest)
            .value(filterRule.getValue())
            .operator(filterRule.getOperator())
            .build())
        .map(childFilterRule -> buildSelectQuery(childShape, List.of(childFilterRule), Collections.emptyList(), query))
        .map(childVertice -> buildEdge(nodeShape, filterRule, childVertice, PathType.NESTED_FILTER))
        .ifPresent(edge1 -> vertice.getEdges()
            .add(edge1));
  }

  private void resolveLeafFilter(Vertice vertice, NodeShape nodeShape, OuterQuery<?> query, FilterRule filterRule,
      FieldPath fieldPath, NodeShape childShape, Variable edgeSubject) {
    Edge edge;
    GraphQLFieldDefinition fieldDefinition = fieldPath.first();
    edge = buildEdge(edgeSubject, nodeShape.getPropertyShape(fieldDefinition.getName()), PathType.FILTER);

    if (Objects.nonNull(fieldDefinition.getDirective(Rdf4jDirectives.AGGREGATE_NAME))) {
      resolveAggregate(fieldDefinition, query.var()).ifPresent(aggregate -> {
        edge.setAggregate(aggregate);
        vertice.addFilter(createFilter(nodeShape, filterRule, aggregate.getVariable()));
      });
    } else {
      resolveFilter(edge.getObject(), query, childShape, filterRule, PathType.FILTER);
    }

    vertice.getEdges()
        .add(edge);
  }

  private List<Edge> resolveEdgesFromSelectedFields(NodeShape nodeShape, List<SelectedField> selectedFields,
      FieldPath fieldPath, OuterQuery<?> query) {
    return selectedFields.stream()
        .filter(field -> Objects.isNull(field.getFieldDefinition()
            .getDirective(Rdf4jDirectives.RESOURCE_NAME)))
        .filter(field -> !field.getQualifiedName()
            .contains("/"))
        .map(selectedField -> {
          Edge edge = resolveEdge(nodeShape, selectedField, fieldPath, query, PathType.SELECTED_FIELD);
          resolveSort(nodeShape, selectedField, edge, query);
          resolveFilters(nodeShape, selectedField, edge, query);
          return edge;
        })
        .collect(Collectors.toList());
  }

  private List<PropertyShape> getUnselectedPropertyShapes(Collection<PropertyShape> propertyShapes, List<Edge> edges) {
    return propertyShapes.stream()
        .filter(propertyShape -> edges.stream()
            .noneMatch(edge -> isEqualToEdge(propertyShape, edge)))
        .collect(Collectors.toList());
  }

  private Edge resolveEdge(NodeShape nodeShape, SelectedField selectedField, FieldPath fieldPath, OuterQuery<?> query,
      PathType pathType) {
    PropertyShape propertyShape = nodeShape.getPropertyShape(selectedField.getName());
    NodeShape childShape = propertyShape.getNode();
    Edge edge;
    if (Objects.isNull(childShape)) {
      edge = buildEdge(query.var(), propertyShape, pathType);
    } else {
      edge = buildNestedEdge(nodeShape, selectedField, fieldPath, query, pathType);
    }

    addLanguageFilter(edge, propertyShape, language);
    return edge;
  }

  private void resolveFilters(NodeShape nodeShape, SelectedField selectedField, Edge selectionEdge,
      OuterQuery<?> query) {
    getArgumentFieldMapping(selectedField, CoreDirectives.FILTER_NAME).stream()
        .filter(ArgumentFieldMapping::argumentIsSet)
        .forEach(mapping -> {
          if (mapping.isSingleton()) {
            PropertyShape propertyShape = nodeShape.getPropertyShape(mapping.getSelectedField()
                .getName());
            resolveFilter(selectionEdge.getObject(), query, propertyShape.getNode(), buildFilterRule(mapping),
                PathType.FILTER);
          } else {
            resolveNestedFilter(selectionEdge, mapping, nodeShape, query);
          }
        });
  }

  private void resolveNestedFilter(Edge baseEdge, ArgumentFieldMapping mapping, NodeShape nodeShape,
      OuterQuery<?> query) {
    NodeShape childShape = nodeShape.getPropertyShape(mapping.getSelectedField()
        .getName())
        .getNode();
    SelectedField filterField = filteredFields(mapping.getSelectedField(), mapping.getFieldPath()).get(0);

    Edge filterEdge = resolveEdge(childShape, filterField, mapping.getFieldPath()
        .rest()
        .orElse(null), query, PathType.NESTED_FILTER);
    filterEdge.addPathType(PathType.NESTED_FILTER);

    FilterRule filterRule = buildFilterRule(mapping.getArgumentValue(), mapping.fieldPathRest());

    resolveFilter(filterEdge.getObject(), query, childShape, filterRule, PathType.NESTED_FILTER);

    baseEdge.getObject()
        .getEdges()
        .add(filterEdge);
  }

  private void resolveSort(NodeShape nodeShape, SelectedField selectedField, Edge selectionEdge, OuterQuery<?> query) {
    getArgumentFieldMapping(selectedField, CoreDirectives.SORT_NAME).forEach(argumentFieldMapping -> {
      FieldPath fieldPath = argumentFieldMapping.getFieldPath();
      PropertyShape propertyShape = nodeShape.getPropertyShape(argumentFieldMapping.getSelectedField()
          .getName());
      if (!fieldPath.isResource()) {
        resolvePath(selectionEdge.getObject(), propertyShape.getNode(), fieldPath, query, PathType.SORT);
      }
    });
  }

  /*
   * A nested edge is an edge with an object vertice created from another nodeshape
   */
  private Edge buildNestedEdge(NodeShape nodeShape, SelectedField selectedField, FieldPath fieldPath,
      OuterQuery<?> query, PathType pathType) {
    PropertyShape propertyShape = nodeShape.getPropertyShape(selectedField.getName());

    Vertice object = createVertice(propertyShape.getNode(), filteredFields(selectedField, fieldPath),
        Optional.ofNullable(fieldPath)
            .flatMap(FieldPath::rest)
            .orElse(null),
        query);

    Aggregate aggregate = resolveAggregate(selectedField.getFieldDefinition(), query.var()).orElse(null);

    return buildEdge(propertyShape, object, pathType, aggregate);
  }

  List<ArgumentFieldMapping> getArgumentFieldMapping(SelectedField selectedField, String directiveName) {
    if (!selectedField.getQualifiedName()
        .contains("/")) {
      return selectedField.getFieldDefinition()
          .getArguments()
          .stream()
          .filter(argument -> argument.getDirective(directiveName) != null)
          .map(argument -> ArgumentFieldMapping.builder()
              .selectedField(selectedField)
              .argument(argument)
              .fieldPath(getFieldPath(selectedField, argument, directiveName))
              .build())
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  void resolveFilter(Vertice vertice, OuterQuery<?> query, NodeShape nodeShape, FilterRule filterRule,
      PathType pathType) {
    if (filterRule.isResource()) {
      vertice.addFilter(createFilter(nodeShape, filterRule, null));
    } else {
      resolvePath(vertice, nodeShape, filterRule.getFieldPath(), query, pathType).ifPresent(match -> match.getObject()
          .addFilter(createFilter(nodeShape, filterRule, null)));
    }
  }

  /*
   * Create a new filter with either one argument or a list of arguments
   */
  Filter createFilter(NodeShape nodeShape, FilterRule filterRule, Variable variable) {
    List<Operand> operands = resolveOperands(nodeShape, filterRule, rdf4jProperties.getShape()
        .getLanguage());

    FilterOperator operator = FilterOperator.getByValue(filterRule.getOperator())
        .orElse(FilterOperator.EQ);

    return Filter.builder()
        .variable(variable)
        .operator(operator)
        .operands(operands)
        .build();
  }

  private List<Operand> resolveOperands(NodeShape nodeShape, FilterRule filterRule, String language) {
    Stream<Object> stream = filterRule.getValue() instanceof List ? castToList(filterRule.getValue()).stream()
        : Stream.of(filterRule.getValue());

    return stream.map(filterArgument -> serializerRouter.serialize(filterArgument))
        .map(filterString -> buildOperands(nodeShape, filterRule, language, filterString))
        .collect(Collectors.toList());
  }

  void addOrderBy(Vertice vertice, NodeShape nodeShape, OuterQuery<?> query, OrderBy orderBy) {
    findOrderVariable(vertice, nodeShape, query, orderBy).map(s -> Optional.of(orderBy.getFieldPath())
        .filter(fieldPath -> !fieldPath.isRequired())
        .flatMap(FieldPath::last)
        .map(fieldDefinition -> (Orderable) Expressions.coalesce(s, getDefaultOrderByValue(fieldDefinition)))
        .orElse(s))
        .ifPresent(s -> {
          List<Orderable> orderables = nonNull(vertice.getOrderables()) ? vertice.getOrderables() : new ArrayList<>();
          orderables.add((orderBy.getOrder()
              .equalsIgnoreCase("desc")) ? s.desc() : s.asc());
          vertice.setOrderables(orderables);
        });
  }

}
