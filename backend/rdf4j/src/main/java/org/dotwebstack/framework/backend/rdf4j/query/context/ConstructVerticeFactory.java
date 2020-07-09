package org.dotwebstack.framework.backend.rdf4j.query.context;

import static java.util.Objects.nonNull;
import static org.dotwebstack.framework.backend.rdf4j.query.context.EdgeHelper.createSimpleEdge;
import static org.dotwebstack.framework.backend.rdf4j.query.context.EdgeHelper.deepList;
import static org.dotwebstack.framework.backend.rdf4j.query.context.EdgeHelper.findEdgesToBeProcessed;
import static org.dotwebstack.framework.backend.rdf4j.query.context.EdgeHelper.isEqualToEdge;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import graphql.schema.SelectedField;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.query.FieldPath;
import org.dotwebstack.framework.backend.rdf4j.query.FilteredField;
import org.dotwebstack.framework.backend.rdf4j.serializers.SerializerRouter;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.BasePath;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.springframework.stereotype.Component;

@Component
public class ConstructVerticeFactory extends AbstractVerticeFactory {

  public ConstructVerticeFactory(SerializerRouter serializerRouter, Rdf4jProperties rdf4jProperties) {
    super(serializerRouter, rdf4jProperties);
  }

  public Vertice createRoot(@NonNull NodeShape nodeShape, @NonNull List<SelectedField> fields,
      @NonNull OuterQuery<?> query) {
    Vertice root = createVertice(nodeShape, fields, null, query.var(), query);
    addConstraints(root, query);
    return root;
  }

  private Vertice createVertice(NodeShape nodeShape, List<SelectedField> fields, FieldPath fieldPath,
      final Variable subject, OuterQuery<?> query) {
    List<Edge> edges = getAllEdges(nodeShape, fields, fieldPath, query);

    return Vertice.builder()
        .nodeShape(nodeShape)
        .subject(subject)
        .edges(edges)
        .build();
  }

  private List<Edge> getAllEdges(NodeShape nodeShape, List<SelectedField> selectedFields, FieldPath fieldPath,
      OuterQuery<?> query) {
    List<Edge> edges = selectedFields.stream()
        .filter(field -> Objects.isNull(field.getFieldDefinition()
            .getDirective(Rdf4jDirectives.RESOURCE_NAME)))
        .filter(field -> !field.getQualifiedName()
            .contains("/"))
        .map(selectedField -> getEdge(nodeShape, selectedField, fieldPath, query))
        .collect(Collectors.toList());

    if (Objects.isNull(fieldPath)) {
      doSortMapping(nodeShape, selectedFields, edges, query);
      doFilterMapping(nodeShape, selectedFields, edges, query);
    }
    List<PropertyShape> unselected = getUnselectedPropertyShapes(nodeShape.getPropertyShapes()
        .values(), edges);
    edges.addAll(getRequiredEdges(unselected, query));

    return edges;
  }

  private List<PropertyShape> getUnselectedPropertyShapes(Collection<PropertyShape> propertyShapes, List<Edge> edges) {
    return propertyShapes.stream()
        .filter(propertyShape -> edges.stream()
            .noneMatch(edge -> isEqualToEdge(propertyShape, edge)))
        .collect(Collectors.toList());
  }

  private Edge getEdge(NodeShape nodeShape, SelectedField selectedField, FieldPath fieldPath, OuterQuery<?> query) {
    PropertyShape propertyShape = nodeShape.getPropertyShape(selectedField.getName());
    NodeShape childShape = propertyShape.getNode();
    Edge edge;
    boolean optional = propertyShape.getMinCount() == null || propertyShape.getMinCount() < 1;
    if (Objects.isNull(childShape)) {
      edge = createSimpleEdge(query.var(), propertyShape, true, optional);
    } else {
      edge = createComplexEdge(nodeShape, selectedField, fieldPath, query, optional);
    }

    addLanguageFilter(edge, propertyShape);
    return edge;
  }

  private Edge processFilters(Edge edge, OuterQuery<?> query, PropertyShape propertyShape,
      ArgumentResultWrapper argumentResultWrapper, FieldPath fieldPath) {

    Vertice vertice = edge.getObject();

    Object value = argumentResultWrapper.getSelectedField()
        .getArguments()
        .get(argumentResultWrapper.getArgument()
            .getName());

    if (nonNull(value)) {
      FilterRule filterRule = FilterRule.builder()
          .fieldPath(fieldPath)
          .value(value)
          .build();

      addFilterToVertice(vertice, query, propertyShape.getNode(), filterRule);
      return edge;
    }

    return null;
  }

  private Optional<Edge> doFilterMapping(ArgumentResultWrapper argumentResultWrapper, Edge edge, NodeShape nodeShape,
      OuterQuery<?> query, SelectedField selectedField, FieldPath fieldPath) {
    return Optional.ofNullable(processFilters(edge, query, nodeShape.getPropertyShape(selectedField.getName()),
        argumentResultWrapper, fieldPath));
  }

  private void doFilterMapping(NodeShape nodeShape, List<SelectedField> selectedFields, List<Edge> selectionEdges,
      OuterQuery<?> query) {

    List<ArgumentResultWrapper> argumentResults = getArgumentFieldMapping(selectedFields, CoreDirectives.FILTER_NAME);

    doFilterMappingLeaf(argumentResults, nodeShape, selectionEdges, query);

    doFilterMappingNode(argumentResults, nodeShape, selectionEdges, query);
  }

  private void doFilterMappingNode(List<ArgumentResultWrapper> argumentResults, NodeShape nodeShape,
      List<Edge> selectionEdges, OuterQuery<?> query) {
    argumentResults.stream()
        .filter(argumentResultWrapper -> !argumentResultWrapper.getFieldPath()
            .isSingleton())
        .forEach(argumentResultWrapper -> mapFilterNodeToEdge(argumentResultWrapper, nodeShape, selectionEdges, query));
  }

  private void mapFilterNodeToEdge(ArgumentResultWrapper argumentResultWrapper, NodeShape nodeShape,
      List<Edge> selectionEdges, OuterQuery<?> query) {
    Edge baseEdge = findEdgesToBeProcessed(nodeShape, argumentResultWrapper.getSelectedField(), selectionEdges).get(0);
    NodeShape childShape = nodeShape.getPropertyShape(argumentResultWrapper.getSelectedField()
        .getName())
        .getNode();
    SelectedField filterField =
        filteredFields(argumentResultWrapper.getSelectedField(), argumentResultWrapper.getFieldPath()).get(0);

    doFilterMapping(argumentResultWrapper, getEdge(childShape, filterField, argumentResultWrapper.getFieldPath()
        .rest()
        .orElse(null), query), childShape, query, filterField, argumentResultWrapper.getFieldPath()
            .rest()
            .orElseThrow(() -> illegalStateException("Expected rest fieldPath but got nothing!")))
                .ifPresent(filterEdge -> {
                  deepList(Collections.singletonList(filterEdge)).forEach(edge -> edge.setVisible(false));
                  filterEdge.setOptional(false);
                  baseEdge.getObject()
                      .getEdges()
                      .add(filterEdge);
                });
  }

  private void doFilterMappingLeaf(List<ArgumentResultWrapper> argumentResults, NodeShape nodeShape,
      List<Edge> selectionEdges, OuterQuery<?> query) {
    argumentResults.stream()
        .filter(argumentResultWrapper -> argumentResultWrapper.getFieldPath()
            .isSingleton())
        .forEach(argumentResultWrapper -> findEdgesToBeProcessed(nodeShape, argumentResultWrapper.getSelectedField(),
            selectionEdges)
                .forEach(edge -> doFilterMapping(argumentResultWrapper, edge, nodeShape, query,
                    argumentResultWrapper.getSelectedField(), argumentResultWrapper.getFieldPath())));
  }

  private void doSortMapping(NodeShape nodeShape, List<SelectedField> selectedFields, List<Edge> selectionEdges,
      OuterQuery<?> query) {
    getArgumentFieldMapping(selectedFields, CoreDirectives.SORT_NAME)
        .forEach(argumentResultWrapper -> findEdgesToBeProcessed(nodeShape, argumentResultWrapper.getSelectedField(),
            selectionEdges)
                .forEach(edge -> processSort(edge.getObject(), query,
                    nodeShape.getPropertyShape(argumentResultWrapper.getSelectedField()
                        .getName()),
                    argumentResultWrapper)));
  }

  /*
   * A complex edge is an edge with filters vertices/filters added to it
   */
  private Edge createComplexEdge(NodeShape nodeShape, SelectedField selectedField, FieldPath fieldPath,
      OuterQuery<?> query, boolean isOptional) {
    PropertyShape propertyShape = nodeShape.getPropertyShape(selectedField.getName());
    BasePath path = propertyShape.getPath();

    return Edge.builder()
        .predicate(path.toPredicate())
        .propertyShape(propertyShape)
        .constructPredicate(path.toConstructPredicate())
        .object(createVertice(propertyShape.getNode(), filteredFields(selectedField, fieldPath),
            Optional.ofNullable(fieldPath)
                .flatMap(FieldPath::rest)
                .orElse(null),
            query.var(), query))
        .isOptional(isOptional)
        .isVisible(true)
        .aggregate(createAggregate(selectedField.getFieldDefinition(), query.var()).orElse(null))
        .build();
  }

  private List<SelectedField> filteredFields(SelectedField selectedField, FieldPath fieldPath) {
    if (Objects.isNull(fieldPath)) {
      return selectedField.getSelectionSet()
          .getFields();
    }

    List<SelectedField> selectedFields = selectedField.getSelectionSet()
        .getFields()
        .stream()
        .filter(childField -> childField.getName()
            .equals(fieldPath.first()
                .getName()))
        .collect(Collectors.toList());

    if (selectedFields.isEmpty()) {
      selectedFields.add(new FilteredField(fieldPath));
    }
    return selectedFields;
  }
}
