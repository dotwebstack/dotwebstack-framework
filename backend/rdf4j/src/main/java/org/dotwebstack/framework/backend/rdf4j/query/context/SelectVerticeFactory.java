package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.getFieldName;
import static org.dotwebstack.framework.backend.rdf4j.query.context.VerticeFactoryHelper.getNextNodeShape;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;

import graphql.schema.GraphQLDirectiveContainer;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.dotwebstack.framework.backend.rdf4j.serializers.SerializerRouter;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.traversers.DirectiveContainerTuple;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.springframework.stereotype.Component;

@Component
public class SelectVerticeFactory extends AbstractVerticeFactory {

  public SelectVerticeFactory(SerializerRouter serializerRouter) {
    super(serializerRouter);
  }

  public Vertice createVertice(Variable subject, OuterQuery<?> query, NodeShape nodeShape,
      List<DirectiveContainerTuple> filterMapping, List<Object> orderByList) {
    Vertice vertice = createVertice(subject, nodeShape);

    filterMapping.forEach(filter -> {
      GraphQLDirectiveContainer container = filter.getContainer();
      String fieldName = getFieldName(container);
      String[] fieldPath = fieldName.split("\\.");
      NodeShape childShape = getNextNodeShape(nodeShape, fieldPath);

      if (nodeShape.equals(childShape)) {
        addFilterToVertice(vertice, container, query, nodeShape, filter.getValue(), fieldPath);
      } else {
        Edge edge = createSimpleEdge(query.var(), null, nodeShape.getPropertyShape(fieldPath[0])
            .getPath()
            .toPredicate(), false);
        fieldPath = ArrayUtils.remove(fieldPath, 0);
        addFilterToVertice(edge.getObject(), container, query, childShape, filter.getValue(), fieldPath);
        vertice.getEdges()
            .add(edge);
      }
    });

    makeEdgesUnique(vertice.getEdges());

    orderByList.forEach(orderBy -> addOrderables(vertice, query, castToMap(orderBy), nodeShape));

    return vertice;
  }

  private Vertice createVertice(final Variable subject, NodeShape nodeShape) {
    List<Edge> edges = new ArrayList<>();

    edges.add(createSimpleEdge(null, Rdf.iri(nodeShape.getTargetClass()
        .stringValue()), () -> stringify(RDF.TYPE), true));

    return Vertice.builder()
        .subject(subject)
        .edges(edges)
        .build();
  }
}
