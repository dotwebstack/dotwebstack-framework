package org.dotwebstack.framework.backend.rdf4j.shacl;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.rdf4j.ValueUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.SHACL;

@Builder
@Getter
public final class NodeShape {

  private final Resource identifier;

  private final IRI targetClass;

  private final Map<String, PropertyShape> propertyShapes;

  public static NodeShape fromShapeModel(Model shapeModel, Resource identifier) {
    return builder()
        .identifier(identifier)
        .targetClass(ValueUtils.findRequiredPropertyIri(shapeModel, identifier, SHACL.TARGET_CLASS))
        .propertyShapes(buildPropertyShapes(shapeModel, identifier))
        .build();
  }

  private static Map<String, PropertyShape> buildPropertyShapes(Model shapeModel,
      Resource nodeShape) {
    return Models
        .getPropertyResources(shapeModel, nodeShape, SHACL.PROPERTY)
        .stream()
        .map(shape -> {
          IRI nodeKind = ValueUtils
              .findRequiredPropertyIri(shapeModel, shape, SHACL.NODE_KIND_PROP);

          PropertyShape.PropertyShapeBuilder builder = PropertyShape.builder()
              .identifier(shape)
              .name(
                  ValueUtils.findRequiredPropertyLiteral(shapeModel, shape, SHACL.NAME)
                      .stringValue())
              .path(ValueUtils.findRequiredPropertyIri(shapeModel, shape, SHACL.PATH))
              .minCount(Models.getPropertyLiteral(shapeModel, shape, SHACL.MIN_COUNT)
                  .map(Literal::intValue)
                  .orElse(0))
              .maxCount(Models.getPropertyLiteral(shapeModel, shape, SHACL.MAX_COUNT)
                  .map(Literal::intValue)
                  .orElse(Integer.MAX_VALUE))
              .nodeKind(nodeKind);

          if (nodeKind.equals(SHACL.LITERAL)) {
            builder.datatype(ValueUtils.findRequiredPropertyIri(shapeModel, shape, SHACL.DATATYPE));
          }

          return builder.build();
        })
        .collect(Collectors.toMap(PropertyShape::getName, Function.identity()));
  }

}
