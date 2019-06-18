package org.dotwebstack.framework.backend.rdf4j.shacl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.ValueUtils;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PropertyPathFactory;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.sail.memory.model.MemBNode;
import org.eclipse.rdf4j.sail.memory.model.MemResource;
import org.eclipse.rdf4j.sail.memory.model.MemStatement;

@Builder
@Getter
public final class NodeShape {

  private final IRI identifier;

  private final IRI targetClass;

  private final Map<String, PropertyShape> propertyShapes;

  public PropertyShape getPropertyShape(String name) {
    PropertyShape propertyShape = this.propertyShapes.get(name);

    // We need to validate the graphql definition against the shacl definition on bootstrap
    if (propertyShape == null) {
      throw new InvalidConfigurationException("No property shape found for name '{}'", name);
    }

    return propertyShape;
  }

  public static NodeShape fromShapeModel(@NonNull Model shapeModel, @NonNull IRI identifier) {
    return builder().identifier(identifier)
        .targetClass(ValueUtils.findRequiredPropertyIri(shapeModel, identifier, SHACL.TARGET_CLASS))
        .propertyShapes(buildPropertyShapes(shapeModel, identifier))
        .build();
  }

  private static Map<String, PropertyShape> buildPropertyShapes(Model shapeModel, Resource nodeShape) {
    Map<String, PropertyShape> orShapes = Models.getPropertyResources(shapeModel, nodeShape, SHACL.OR)
        .stream()
        .map(or -> unwrapOrStatements(shapeModel, or))
        .flatMap(List::stream)
        .map(statement -> buildPropertyShape(shapeModel, statement.getSubject()))
        .collect(Collectors.toMap(PropertyShape::getName, Function.identity()));

    Map<String, PropertyShape> propertyShapes = Models.getPropertyResources(shapeModel, nodeShape, SHACL.PROPERTY)
        .stream()
        .map(shape -> buildPropertyShape(shapeModel, shape))
        .collect(Collectors.toMap(PropertyShape::getName, Function.identity()));

    return Stream.concat(orShapes.entrySet()
        .stream(),
        propertyShapes.entrySet()
            .stream())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static PropertyShape buildPropertyShape(Model shapeModel, Resource shape) {
    PropertyShape.PropertyShapeBuilder builder = PropertyShape.builder();
    Model usedModel = shapeModel;
    Resource usedShape = shape;

    builder.path(PropertyPathFactory.create(usedModel, usedShape, SHACL.PATH))
        .name(ValueUtils.findRequiredPropertyLiteral(usedModel, usedShape, SHACL.NAME)
            .stringValue());

    if (ValueUtils.isPropertyIriPresent(usedModel, usedShape, SHACL.NODE)) {
      IRI nodeIri = ValueUtils.findRequiredPropertyIri(usedModel, usedShape, SHACL.NODE);

      builder.node(nodeIri);

      usedShape = nodeIri;
      usedModel = shapeModel.filter(nodeIri, RDF.TYPE, SHACL.NODE_SHAPE);
    }

    if (ValueUtils.isPropertyIriPresent(usedModel, usedShape, SHACL.NODE_KIND_PROP)) {
      IRI nodeKind = ValueUtils.findRequiredPropertyIri(usedModel, usedShape, SHACL.NODE_KIND_PROP);

      builder.nodeKind(nodeKind);

      if (nodeKind.equals(SHACL.LITERAL)) {
        builder.datatype(ValueUtils.findRequiredPropertyIri(usedModel, usedShape, SHACL.DATATYPE));
      }
    }

    builder.identifier(usedShape)
        .minCount(Models.getPropertyLiteral(usedModel, usedShape, SHACL.MIN_COUNT)
            .map(Literal::intValue)
            .orElse(0))
        .maxCount(Models.getPropertyLiteral(usedModel, usedShape, SHACL.MAX_COUNT)
            .map(Literal::intValue)
            .orElse(Integer.MAX_VALUE));

    return builder.build();
  }

  private static List<MemStatement> unwrapOrStatements(Model shapeModel, Resource shape) {
    List<MemStatement> shapes = new ArrayList<>();

    shapeModel.filter(shape, RDF.FIRST, null)
        .stream()
        .map(statement -> ((MemBNode) statement.getObject()).getSubjectStatementList()
            .get(0))
        .findFirst()
        .ifPresent(shapes::add);

    shapeModel.filter(shape, RDF.REST, null)
        .stream()
        .map(statement -> ((MemResource) statement.getObject()))
        .filter(resource -> (resource instanceof MemBNode))
        .map(resource -> resource.getSubjectStatementList()
            .get(0))
        .findFirst()
        .ifPresent(rest -> shapes.addAll(unwrapOrStatements(shapeModel, rest.getSubject())));

    return shapes;
  }

}
