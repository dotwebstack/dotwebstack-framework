package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;

import graphql.schema.SelectedField;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.rdf4j.serializers.SerializerRouter;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.BasePath;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.springframework.stereotype.Component;

@Component
public class ConstructVerticeFactory extends AbstractVerticeFactory {

  public ConstructVerticeFactory(SerializerRouter serializerRouter) {
    super(serializerRouter);
  }

  public Vertice createVertice(List<IRI> filterSubjects, final Variable subject, OuterQuery<?> query,
      NodeShape nodeShape, List<SelectedField> fields) {
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

          if (Objects.isNull(childShape)) {
            return createSimpleEdge(query.var(), propertyShape.getPath(), true, true);
          }
          return createComplexEdge(query, nodeShape, field);
        })
        .collect(Collectors.toList());

    makeEdgesUnique(edges);


    Set<Iri> iris = nodeShape.getTargetClasses()
        .stream()
        .map(targetClass -> Rdf.iri(targetClass.stringValue()))
        .collect(Collectors.toSet());

    edges.add(createSimpleEdge(null, iris, () -> stringify(RDF.TYPE), true));

    getArgumentFieldMapping(nodeShape, fields)
        .forEach((argument, field) -> findEdgesToBeProcessed(nodeShape, field, edges)
            .forEach(edge -> processEdge(edge.getObject(), argument, query, nodeShape, field)));

    return Vertice.builder()
        .subject(subject)
        .edges(edges)
        .build();
  }

  /*
   * A complex edge is an edge with filters vertices/filters added to it
   */
  private Edge createComplexEdge(OuterQuery<?> query, NodeShape nodeShape, SelectedField field) {
    BasePath path = nodeShape.getPropertyShape(field.getName())
        .getPath();

    return Edge.builder()
        .predicate(path.toPredicate())
        .constructPredicate(path.toConstructPredicate())
        .object(createVertice(query.var(), query, nodeShape.getPropertyShape(field.getName())
            .getNode(),
            field.getSelectionSet()
                .getFields()))
        .isOptional(true)
        .isVisible(true)
        .build();
  }

  private void addSubjectFilters(Vertice vertice, List<IRI> filterSubjects) {
    vertice.getFilters()
        .add(Filter.builder()
            .operator(FilterOperator.EQ)
            .operands(filterSubjects.stream()
                .map(filterSubject -> Rdf.iri(filterSubject.stringValue()))
                .collect(Collectors.toList()))
            .build());
  }
}
