package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;

import graphql.schema.SelectedField;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
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
    return createVertice(subject, query, nodeShape, fields);
  }

  private Vertice createVertice(final Variable subject, OuterQuery<?> query, NodeShape nodeShape,
      List<SelectedField> fields) {
    List<Edge> edges = fields.stream()
        .filter(field -> !field.getQualifiedName()
            .contains("/"))
        .map(field -> {
          PropertyShape propertyShape = nodeShape.getPropertyShape(field.getName());
          NodeShape childShape = propertyShape.getNode();

          Edge edge;
          if (Objects.isNull(childShape)) {
            edge = createSimpleEdge(query.var(), propertyShape.getPath(), true, true);
          } else {
            edge = createComplexEdge(query, nodeShape, field);
          }

          addLanguageFilter(edge, propertyShape);

          return edge;
        })
        .collect(Collectors.toList());

    makeEdgesUnique(edges);

    Set<Iri> iris = nodeShape.getTargetClasses()
        .stream()
        .map(targetClass -> Rdf.iri(targetClass.stringValue()))
        .collect(Collectors.toSet());

    edges.add(createSimpleEdge(null, iris, () -> stringify(RDF.TYPE), true));

    getArgumentFieldMapping(nodeShape, fields, CoreDirectives.FILTER_NAME)
        .forEach((argument, field) -> findEdgesToBeProcessed(nodeShape, field, edges)
            .forEach(edge -> processEdge(edge.getObject(), argument, query, nodeShape, field)));

    getArgumentFieldMapping(nodeShape, fields, CoreDirectives.SORT_NAME)
        .forEach((argument, field) -> findEdgesToBeProcessed(nodeShape, field, edges)
            .forEach(edge -> processEdgeSort(edge.getObject(), argument, query, nodeShape, field)));

    return Vertice.builder()
        .subject(subject)
        .edges(edges)
        .build();
  }

  /*
   * A complex edge is an edge with filters vertices/filters added to it
   */
  private Edge createComplexEdge(OuterQuery<?> query, NodeShape nodeShape, SelectedField field) {
    PropertyShape propertyShape = nodeShape.getPropertyShape(field.getName());
    BasePath path = propertyShape.getPath();

    return Edge.builder()
        .predicate(path.toPredicate())
        .constructPredicate(path.toConstructPredicate())
        .object(createVertice(query.var(), query, propertyShape.getNode(), field.getSelectionSet()
            .getFields()))
        .isOptional(true)
        .isVisible(true)
        .maxCount(propertyShape.getMaxCount())
        .build();
  }

}
