package org.dotwebstack.framework.backend.rdf4j.query.context;

import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;
import static org.dotwebstack.framework.backend.rdf4j.query.context.EdgeHelper.createSimpleEdge;
import static org.dotwebstack.framework.backend.rdf4j.query.context.EdgeHelper.deepList;
import static org.dotwebstack.framework.backend.rdf4j.query.context.EdgeHelper.findEdgesToBeProcessed;
import static org.dotwebstack.framework.backend.rdf4j.query.context.EdgeHelper.isEqualToEdge;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import graphql.schema.SelectedField;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.springframework.stereotype.Component;

@Component
public class ConstructVerticeFactory extends AbstractVerticeFactory {

  public ConstructVerticeFactory(SerializerRouter serializerRouter, Rdf4jProperties rdf4jProperties) {
    super(serializerRouter, rdf4jProperties);
  }

  public Vertice createRoot(@NonNull NodeShape nodeShape, @NonNull List<SelectedField> fields,
      @NonNull OuterQuery<?> query) {
    Vertice root = createVertice(nodeShape, fields, null, query.var(), query);
    addRequiredEdges(root, nodeShape, query);
    return root;
  }

  private void addRequiredEdges(Vertice vertice, NodeShape nodeShape, OuterQuery<?> query) {
    List<PropertyShape> unselectedFields = nodeShape.getPropertyShapes()
        .values()
        .stream()
        .filter(propertyShape -> vertice.getEdges()
            .stream()
            .noneMatch(edge -> isEqualToEdge(propertyShape, edge)))
        .collect(Collectors.toList());

    List<Edge> edges = unselectedFields.stream()
        .filter(ps -> ps.getMinCount() != null && ps.getMinCount() >= 1)
        .map(ps -> {
          if (ps.getHasValue() != null) {
            return createSimpleEdge(ps.getHasValue(), ps, false, false);
          }

          Variable var = query.var();
          return createSimpleEdge(var, ps, false, false);
        })
        .collect(Collectors.toList());
    vertice.getEdges()
        .addAll(edges);

    vertice.getEdges()
        .forEach(edge -> {
          NodeShape childShape = edge.getObject() != null ? edge.getObject()
              .getNodeShape() : null;
          if (childShape != null) {
            addRequiredEdges(edge.getObject(), childShape, query);
          }
        });
  }

  private Vertice createVertice(NodeShape nodeShape, List<SelectedField> fields, FieldPath fieldPath,
      final Variable subject, OuterQuery<?> query) {
    List<Edge> edges = transformFieldsToEdges(nodeShape, fields, fieldPath, query);

    return Vertice.builder()
        .nodeShape(nodeShape)
        .subject(subject)
        .edges(edges)
        .build();
  }

  private List<Edge> transformFieldsToEdges(NodeShape nodeShape, List<SelectedField> selectedFields,
      FieldPath fieldPath, OuterQuery<?> query) {
    List<Edge> edges = selectedFields.stream()
        .filter(field -> Objects.isNull(field.getFieldDefinition()
            .getDirective(Rdf4jDirectives.RESOURCE_NAME)))
        .filter(field -> !field.getQualifiedName()
            .contains("/"))
        .map(selectedField -> getEdge(nodeShape, selectedField, fieldPath, query))
        .collect(Collectors.toList());

    if (Objects.isNull(fieldPath)) {
      doSortMapping(nodeShape, selectedFields, edges, query);
      Set<Iri> classIris = getClassIris(nodeShape);
      if (!classIris.isEmpty()) {
        edges.add(createSimpleEdge(classIris, () -> stringify(RDF.TYPE), true));
      }
      doFilterMapping(nodeShape, selectedFields, edges, query);
    }

    return edges;
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

    addHasValueConstraint(edge, propertyShape);
    addLanguageFilter(edge, propertyShape);
    return edge;
  }

  private Set<Iri> getClassIris(NodeShape nodeShape) {
    return nodeShape.getClasses()
        .stream()
        .map(targetClass -> Rdf.iri(targetClass.stringValue()))
        .collect(Collectors.toSet());
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

      deepList(singletonList(edge)).forEach(e -> e.setOptional(false));

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
