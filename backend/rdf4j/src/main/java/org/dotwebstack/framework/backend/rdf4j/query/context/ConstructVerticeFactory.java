package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;

import graphql.schema.SelectedField;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
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
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;
import org.springframework.stereotype.Component;

@Component
public class ConstructVerticeFactory extends AbstractVerticeFactory {

  public ConstructVerticeFactory(SerializerRouter serializerRouter, Rdf4jProperties rdf4jProperties) {
    super(serializerRouter, rdf4jProperties);
  }

  public Vertice createRoot(@NonNull final Variable subject, @NonNull OuterQuery<?> query, @NonNull NodeShape nodeShape,
      @NonNull List<SelectedField> fields) {
    return createVertice(subject, query, nodeShape, fields);
  }

  private Vertice createVertice(final Variable subject, OuterQuery<?> query, NodeShape nodeShape,
      List<SelectedField> fields) {
    List<Edge> edges = transformFieldsToEdges(query, nodeShape, fields);

    return Vertice.builder()
        .subject(subject)
        .edges(edges)
        .build();
  }

  private List<Edge> transformFieldsToEdges(OuterQuery<?> query, NodeShape nodeShape, List<SelectedField> fields) {
    List<SelectedField> filteredFields = filterFields(fields);

    List<Edge> edges = getUniqueEdges(query, nodeShape, filteredFields);

    edges.add(createSimpleEdge(null, getTargetClassIris(nodeShape), () -> stringify(RDF.TYPE), true));

    doArgumentMapping(query, nodeShape, filteredFields, edges);
    return edges;
  }

  private List<SelectedField> filterFields(List<SelectedField> fields) {
    return fields.stream()
        .filter(field -> Objects.isNull(field.getFieldDefinition()
            .getDirective(Rdf4jDirectives.RESOURCE_NAME)))
        .collect(Collectors.toList());
  }

  private List<Edge> getUniqueEdges(OuterQuery<?> query, NodeShape nodeShape, List<SelectedField> filteredFields) {
    List<Edge> edges = filteredFields.stream()
        .filter(field -> !field.getQualifiedName()
            .contains("/"))
        .map(toEdge(query, nodeShape))
        .collect(Collectors.toList());
    makeEdgesUnique(edges);
    return edges;
  }

  private Function<SelectedField, Edge> toEdge(OuterQuery<?> query, NodeShape nodeShape) {
    return field -> {
      PropertyShape propertyShape = nodeShape.getPropertyShape(field.getName());
      NodeShape childShape = propertyShape.getNode();

      return getEdge(query, nodeShape, field, propertyShape, childShape);
    };
  }

  private Edge getEdge(OuterQuery<?> query, NodeShape nodeShape, SelectedField field, PropertyShape propertyShape,
      NodeShape childShape) {
    Edge edge;
    if (Objects.isNull(childShape)) {
      edge = createSimpleEdge(query.var(), propertyShape.getPath(), true, true);
    } else {
      edge = createComplexEdge(query, nodeShape, field);
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

  private void doArgumentMapping(OuterQuery<?> query, NodeShape nodeShape, List<SelectedField> filteredFields,
      List<Edge> edges) {
    getArgumentFieldMapping(nodeShape, filteredFields, CoreDirectives.FILTER_NAME)
        .forEach((argument, field) -> findEdgesToBeProcessed(nodeShape, field, edges)
            .forEach(edge -> processEdge(edge.getObject(), argument, query, nodeShape, field)));

    getArgumentFieldMapping(nodeShape, filteredFields, CoreDirectives.SORT_NAME)
        .forEach((argument, field) -> findEdgesToBeProcessed(nodeShape, field, edges)
            .forEach(edge -> processEdgeSort(edge.getObject(), argument, query, nodeShape, field)));
  }

  /*
   * A complex edge is an edge with filters vertices/filters added to it
   */
  private Edge createComplexEdge(OuterQuery<?> query, NodeShape nodeShape, SelectedField field) {
    BasePath path = nodeShape.getPropertyShape(field.getName())
        .getPath();
    Vertice object = createVertice(query.var(), query, nodeShape.getPropertyShape(field.getName())
        .getNode(),
        field.getSelectionSet()
            .getFields());

    RdfPredicate predicate = path.toPredicate();
    RdfPredicate constructPredicate = path.toConstructPredicate();

    return Edge.builder()
        .predicate(predicate)
        .constructPredicate(constructPredicate)
        .object(object)
        .isOptional(true)
        .isVisible(true)
        .build();
  }

}
