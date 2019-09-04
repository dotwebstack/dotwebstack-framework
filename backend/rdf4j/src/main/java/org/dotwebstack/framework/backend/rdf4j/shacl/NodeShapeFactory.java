package org.dotwebstack.framework.backend.rdf4j.shacl;

import static org.dotwebstack.framework.backend.rdf4j.ValueUtils.findRequiredPropertyIri;
import static org.dotwebstack.framework.backend.rdf4j.ValueUtils.findRequiredPropertyIris;
import static org.dotwebstack.framework.backend.rdf4j.ValueUtils.findRequiredPropertyLiteral;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.ValueUtils;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PropertyPathFactory;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.sail.memory.model.MemBNode;
import org.eclipse.rdf4j.sail.memory.model.MemIRI;
import org.eclipse.rdf4j.sail.memory.model.MemResource;
import org.eclipse.rdf4j.sail.memory.model.MemStatement;

public class NodeShapeFactory {

  private NodeShapeFactory() {}

  public static NodeShape createShapeFromModel(@NonNull Model shapeModel, @NonNull IRI identifier) {
    return createShapeFromModel(shapeModel, identifier, new HashMap<>());
  }

  public static NodeShape createShapeFromModel(@NonNull Model shapeModel, @NonNull IRI identifier,
      Map<IRI, NodeShape> nodeShapeMap) {
    Map<String, PropertyShape> propertyShapes = new HashMap<>();

    if (nodeShapeMap.containsKey(identifier)) {
      return nodeShapeMap.get(identifier);
    }

    NodeShape nodeShape = NodeShape.builder()
        .name(findRequiredPropertyLiteral(shapeModel, identifier, SHACL.NAME).stringValue())
        .identifier(identifier)
        .targetClasses(findRequiredPropertyIris(shapeModel, identifier, SHACL.TARGET_CLASS))
        .propertyShapes(propertyShapes)
        .build();

    nodeShapeMap.put(identifier, nodeShape);

    // add propertyshapes afterwards to handle cyclic dependencies
    propertyShapes.putAll(buildPropertyShapes(shapeModel, identifier, nodeShapeMap));

    return nodeShape;
  }

  private static Map<String, PropertyShape> buildPropertyShapes(Model shapeModel, Resource nodeShape,
      Map<IRI, NodeShape> nodeShapeMap) {
    /*
     * The sh:or can occur on multiple levels, either as a direct child of a nodeshape or as a child of
     * an sh:property. Here the direct childs of type sh:or on a nodeshape are processed
     */
    Map<String, PropertyShape> orShapes = getOrPropertyShapes(shapeModel, nodeShape).stream()
        .map(shape -> buildPropertyShape(shapeModel, shape, nodeShapeMap))
        .collect(Collectors.toMap(PropertyShape::getName, Function.identity()));

    Map<String, PropertyShape> propertyShapes = Models.getPropertyResources(shapeModel, nodeShape, SHACL.PROPERTY)
        .stream()
        .map(shape -> {
          if (ValueUtils.isPropertyPresent(shapeModel, shape, SHACL.OR)) {
            /* This processes sh:or constructs that are childs of the sh:property */
            return getOrPropertyShapes(shapeModel, shape);
          } else {
            return Collections.singletonList(shape);
          }
        })
        .flatMap(List::stream)
        .map(shape -> buildPropertyShape(shapeModel, shape, nodeShapeMap))
        .collect(Collectors.toMap(PropertyShape::getName, Function.identity()));

    return Stream.concat(orShapes.entrySet()
        .stream(),
        propertyShapes.entrySet()
            .stream())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static List<Resource> getOrPropertyShapes(Model shapeModel, Resource nodeShape) {
    return Models.getPropertyResources(shapeModel, nodeShape, SHACL.OR)
        .stream()
        .map(or -> unwrapOrStatements(shapeModel, or).stream()
            .peek(resource ->
            /*
             * All the individual childs under an sh:or statement are enriched with the shared values on the
             * same level as the sh:or. The sh:or is excluded from this enrichment. These enrichments are added
             * to the model for later reuse.
             */
            shapeModel.filter(nodeShape, null, null)
                .stream()
                .filter(statement -> !SHACL.OR.equals(statement.getPredicate()))
                .filter(statement -> !SHACL.NAME.equals(statement.getPredicate()))
                .forEach(statement -> {
                  if (resource instanceof MemResource && statement.getObject() instanceof MemResource) {
                    MemStatement memStatement = new MemStatement((MemResource) resource,
                        (MemIRI) statement.getPredicate(), (MemResource) statement.getObject(), null, 0);
                    ((MemBNode) resource).getSubjectStatementList()
                        .add(memStatement);
                    shapeModel.add(memStatement);
                  } else {
                    throw unsupportedOperationException("Expected memResource got '{}' for statement '{}'", resource,
                        statement);
                  }
                }))
            .collect(Collectors.toList()))
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private static PropertyShape buildPropertyShape(Model shapeModel, Resource shape, Map<IRI, NodeShape> nodeShapeMap) {
    PropertyShape.PropertyShapeBuilder builder = PropertyShape.builder();
    Resource usedShape = shape;

    builder.path(PropertyPathFactory.create(shapeModel, usedShape, SHACL.PATH))
        .name(findRequiredPropertyLiteral(shapeModel, usedShape, SHACL.NAME).stringValue());

    if (ValueUtils.isPropertyIriPresent(shapeModel, usedShape, SHACL.NODE)) {
      /*
       * if a sh:node is present, it means a reference exist to another NodeShape. For that reason the
       * focus is shifted so the properties of that NodeShape are resolved within this property shape
       */
      IRI nodeIri = ValueUtils.findRequiredPropertyIri(shapeModel, usedShape, SHACL.NODE);
      builder.node(createShapeFromModel(shapeModel, nodeIri, nodeShapeMap));

      usedShape = nodeIri;
    }

    if (ValueUtils.isPropertyIriPresent(shapeModel, usedShape, SHACL.NODE_KIND_PROP)) {
      IRI nodeKind = findRequiredPropertyIri(shapeModel, usedShape, SHACL.NODE_KIND_PROP);
      builder.nodeKind(nodeKind);

      if (nodeKind.equals(SHACL.LITERAL)) {
        builder.datatype(findRequiredPropertyIri(shapeModel, usedShape, SHACL.DATATYPE));
      }
    }

    builder.identifier(usedShape)
        .minCount(Models.getPropertyLiteral(shapeModel, usedShape, SHACL.MIN_COUNT)
            .map(Literal::intValue)
            .orElse(0))
        .maxCount(Models.getPropertyLiteral(shapeModel, usedShape, SHACL.MAX_COUNT)
            .map(Literal::intValue)
            .orElse(Integer.MAX_VALUE));

    return builder.build();
  }

  private static List<Resource> unwrapOrStatements(Model shapeModel, Resource shape) {
    List<Resource> shapes = new ArrayList<>();

    /*
     * sh:or works with a sequence (first, rest (first, rest)) etc. structure. For that reason this
     * function adds the content of first and then further unwraps the content of rest.
     */
    shapeModel.filter(shape, RDF.FIRST, null)
        .stream()
        .map(statement -> ((MemBNode) statement.getObject()).getSubjectStatementList()
            .get(0)
            .getSubject())
        .findFirst()
        .ifPresent(shapes::add);

    shapeModel.filter(shape, RDF.REST, null)
        .stream()
        .map(statement -> ((MemResource) statement.getObject()))

        /*
         * Something other than type MemBNode means it is RDF.NIL (of type IRI), this means that the end of
         * the sequence is reached. It is removed so that the optional will not be present and the process
         * stops
         */
        .filter(resource -> (resource instanceof MemBNode))
        .map(resource -> resource.getSubjectStatementList()
            .get(0))
        .findFirst()
        .ifPresent(rest -> shapes.addAll(unwrapOrStatements(shapeModel, rest.getSubject())));

    return shapes;
  }

}
