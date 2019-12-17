package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;

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

  public Vertice createRoot(@NonNull final Variable subject, @NonNull OuterQuery<?> query, @NonNull NodeShape nodeShape,
      @NonNull List<SelectedField> fields) {
    return createVertice(nodeShape, fields, null, subject, query);
  }

  private Vertice createVertice(NodeShape nodeShape, List<SelectedField> fields, FieldPath fieldPath,
      final Variable subject, OuterQuery<?> query) {
    List<Edge> edges = transformFieldsToEdges(nodeShape, fields, fieldPath, query);

    return Vertice.builder()
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
      doSortMapping(query, nodeShape, selectedFields, edges);
      makeEdgesUnique(edges);
      edges.add(createSimpleEdge(null, getTargetClassIris(nodeShape), () -> stringify(RDF.TYPE), true));
      edges.addAll(doFilterMapping(query, nodeShape, selectedFields, edges));
    }

    return edges;
  }

  private Edge getEdge(NodeShape nodeShape, SelectedField selectedField, FieldPath fieldPath, OuterQuery<?> query) {
    PropertyShape propertyShape = nodeShape.getPropertyShape(selectedField.getName());
    NodeShape childShape = propertyShape.getNode();
    Edge edge;
    if (Objects.isNull(childShape)) {
      edge = createSimpleEdge(query.var(), propertyShape.getPath(), true, true);
    } else {
      edge = createComplexEdge(nodeShape, selectedField, fieldPath, query);
    }

    addLanguageFilter(edge, propertyShape);
    return edge;
  }

  private Set<Iri> getTargetClassIris(NodeShape nodeShape) {
    return nodeShape.getTargetClasses()
        .stream()
        .map(targetClass -> Rdf.iri(targetClass.stringValue()))
        .collect(Collectors.toSet());
  }

  private List<Edge> doFilterMapping(OuterQuery<?> query, NodeShape nodeShape, List<SelectedField> selectedFields,
      List<Edge> selectionEdges) {
    return getArgumentFieldMapping(nodeShape, selectedFields, CoreDirectives.FILTER_NAME).stream()
        .flatMap(argumentResultWrapper -> {
          List<Edge> edges;
          if (argumentResultWrapper.getFieldPath()
              .isSingleton()) {
            edges = findEdgesToBeProcessed(nodeShape, argumentResultWrapper.getSelectedField(), selectionEdges);
          } else {
            edges = Collections.singletonList(getEdge(nodeShape, argumentResultWrapper.getSelectedField(),
                argumentResultWrapper.getFieldPath(), query));
            deepList(edges).forEach(edge -> edge.setVisible(false));
          }

          return edges.stream()
              .map(edge -> processFilters(edge, query,
                  nodeShape.getPropertyShape(argumentResultWrapper.getSelectedField()
                      .getName()),
                  argumentResultWrapper));
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private void doSortMapping(OuterQuery<?> query, NodeShape nodeShape, List<SelectedField> selectedFields,
      List<Edge> selectionEdges) {
    getArgumentFieldMapping(nodeShape, selectedFields, CoreDirectives.SORT_NAME)
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
      OuterQuery<?> query) {
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
        .isOptional(true)
        .isVisible(true)
        .aggregate(createAggregate(selectedField.getFieldDefinition(), query.var()).orElse(null))
        .build();
  }

  private List<SelectedField> filteredFields(SelectedField selectedField, FieldPath fieldPath) {
    if (Objects.isNull(fieldPath)) {
      return selectedField.getSelectionSet()
          .getFields();
    }
    return selectedField.getSelectionSet()
        .getFields()
        .stream()
        .filter(childField -> childField.getName()
            .equals(fieldPath.first()
                .getName()))
        .collect(Collectors.toList());
  }
}
