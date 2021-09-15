package org.dotwebstack.framework.backend.rdf4j.shacl;

import static org.dotwebstack.framework.backend.rdf4j.ValueUtils.findOptionalPropertyIri;
import static org.dotwebstack.framework.backend.rdf4j.ValueUtils.findRequiredProperty;
import static org.dotwebstack.framework.backend.rdf4j.ValueUtils.findRequiredPropertyIri;
import static org.dotwebstack.framework.backend.rdf4j.ValueUtils.findRequiredPropertyLiteral;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.ValueUtils;
import org.dotwebstack.framework.backend.rdf4j.constants.Rdf4jConstants;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PropertyPathFactory;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.vocabulary.SHACL;

public class NodeShapeFactory {

  private NodeShapeFactory() {}

  public static NodeShape createShapeFromModel(@NonNull Model shapeModel, @NonNull Resource identifier) {
    return createShapeFromModel(shapeModel, identifier, new HashMap<>());
  }

  public static NodeShape createShapeFromModel(@NonNull Model shapeModel, @NonNull Resource identifier,
      Map<Resource, NodeShape> nodeShapeMap) {
    Map<String, PropertyShape> propertyShapes = new HashMap<>();

    if (nodeShapeMap.containsKey(identifier)) {
      return nodeShapeMap.get(identifier);
    }

    var nodeShape = NodeShape.builder()
        .name(findRequiredPropertyLiteral(shapeModel, identifier, SHACL.NAME).stringValue())
        .identifier(identifier)
        .classes(Models.getPropertyIRIs(shapeModel, identifier, SHACL.CLASS)
            .stream()
            .map(Set::of)
            .collect(Collectors.toSet()))
        .parent(findOptionalPropertyIri(shapeModel, identifier, Rdf4jConstants.DOTWEBSTACK_INHERITS).orElse(null))
        .propertyShapes(propertyShapes)
        .build();

    Set<IRI> orClasses = getOrClassConstraints(shapeModel, identifier);
    if (!orClasses.isEmpty()) {
      nodeShape.getClasses()
          .add(orClasses);
    }

    nodeShapeMap.put(identifier, nodeShape);

    // add propertyshapes afterwards to handle cyclic dependencies
    propertyShapes.putAll(buildPropertyShapes(shapeModel, identifier, nodeShapeMap));

    return nodeShape;
  }

  private static Map<String, PropertyShape> buildPropertyShapes(Model shapeModel, Resource nodeShape,
      Map<Resource, NodeShape> nodeShapeMap) {
    /*
     * The sh:or can occur on multiple levels, either as a direct child of a nodeshape or as a child of
     * an sh:property. Here the direct childs of type sh:or on a nodeshape are processed
     */
    List<PropertyShape> orShapes = getOrPropertyShapes(shapeModel, nodeShape).stream()
        .map(shape -> buildPropertyShape(shapeModel, shape, nodeShapeMap))
        .collect(Collectors.toList());

    List<PropertyShape> propertyShapesList = Models.getPropertyResources(shapeModel, nodeShape, SHACL.PROPERTY)
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
        .collect(Collectors.toList());

    Map<String, PropertyShape> propertyShapes = new HashMap<>();

    Stream.concat(propertyShapesList.stream(), orShapes.stream())
        .filter(propertyShape -> !propertyShapes.containsKey(propertyShape.getName()))
        .forEach(propertyShape -> propertyShapes.put(propertyShape.getName(), propertyShape));

    validatePropertyShapes(propertyShapes);
    return propertyShapes;
  }

  static void validatePropertyShapes(Map<String, PropertyShape> propertyShapes) {
    propertyShapes.values()
        .forEach(propertyShape -> {
          if (propertyShape.getMinCount() != null && propertyShape.getMinCount() > 1) {
            throw invalidConfigurationException("Propertyshape {} has a minCount > 1, which is not yet supported",
                propertyShape.getIdentifier());
          }
        });
  }

  private static List<Resource> getOrPropertyShapes(Model shapeModel, Resource identifier) {
    return getShaclOrShapes(shapeModel, identifier).stream()
        .filter(resource -> !shapeModel.filter(resource, SHACL.PATH, null)
            .isEmpty())
        .collect(Collectors.toList());
  }

  private static Set<IRI> getOrClassConstraints(Model shapeModel, Resource identifier) {
    return Models.getPropertyResources(shapeModel, identifier, SHACL.OR)
        .stream()
        .map(or -> unwrapOrStatements(shapeModel, or))
        .flatMap(List::stream)
        .flatMap(resource -> Models.objectIRI(shapeModel.filter(resource, SHACL.CLASS, null))
            .stream())
        .collect(Collectors.toSet());
  }

  private static List<Resource> getShaclOrShapes(Model shapeModel, Resource identifier) {
    return Models.getPropertyResources(shapeModel, identifier, SHACL.OR)
        .stream()
        .flatMap(or -> unwrapAndEnrichOrs(shapeModel, identifier, or))
        .collect(Collectors.toList());
  }

  private static Stream<Resource> unwrapAndEnrichOrs(Model shapeModel, Resource identifier, Resource or) {
    List<Resource> ors = unwrapOrStatements(shapeModel, or);

    ors.forEach(resource -> shapeModel.filter(identifier, null, null)
        .stream()
        .filter(statement -> !SHACL.OR.equals(statement.getPredicate()))
        .filter(statement -> !SHACL.NAME.equals(statement.getPredicate()))
        .forEach(statement -> {
          if (statement.getObject() instanceof Resource) {
            shapeModel.add(resource, statement.getPredicate(), statement.getObject());
            shapeModel.addAll(shapeModel.filter(resource, null, null));
          } else {
            throw unsupportedOperationException("Expected memResource got '{}' for statement '{}'", resource,
                statement);
          }
        }));

    return ors.stream();
  }

  private static List<Resource> unwrapOrStatements(Model shapeModel, Resource shape) {
    return RDFCollections.asValues(shapeModel, shape, new ArrayList<>())
        .stream()
        .map(Resource.class::cast)
        .collect(Collectors.toList());
  }

  private static PropertyShape buildPropertyShape(Model shapeModel, Resource shape,
      Map<Resource, NodeShape> nodeShapeMap) {
    PropertyShape.PropertyShapeBuilder builder = PropertyShape.builder();
    Resource usedShape = shape;

    builder.path(PropertyPathFactory.create(shapeModel, usedShape, SHACL.PATH))
        .name(findRequiredPropertyLiteral(shapeModel, usedShape, SHACL.NAME).stringValue());

    if (ValueUtils.isPropertyIriPresent(shapeModel, usedShape, SHACL.NODE)) {
      /*
       * if a sh:node is present, it means a reference exist to another NodeShape. For that reason the
       * focus is shifted so the properties of that NodeShape are resolved within this property shape
       */
      var nodeIri = findRequiredPropertyIri(shapeModel, usedShape, SHACL.NODE);
      builder.node(createShapeFromModel(shapeModel, nodeIri, nodeShapeMap));

      usedShape = nodeIri;
    } else if (ValueUtils.isPropertyPresent(shapeModel, usedShape, SHACL.NODE)) {
      Resource bnode = (Resource) findRequiredProperty(shapeModel, usedShape, SHACL.NODE);
      builder.node(createShapeFromModel(shapeModel, bnode, nodeShapeMap));
      usedShape = bnode;
    }

    if (ValueUtils.isPropertyIriPresent(shapeModel, usedShape, SHACL.NODE_KIND_PROP)) {
      var nodeKind = findRequiredPropertyIri(shapeModel, usedShape, SHACL.NODE_KIND_PROP);
      builder.nodeKind(nodeKind);

      if (nodeKind.equals(SHACL.LITERAL)) {
        builder.datatype(findRequiredPropertyIri(shapeModel, usedShape, SHACL.DATATYPE));
      }
    }

    builder.constraints(Stream.of(ConstraintType.values())
        .map(constraint -> new AbstractMap.SimpleEntry<ConstraintType, Object>(constraint,
            Models.getProperty(shapeModel, shape, constraint.getType())
                .orElse(null)))
        .filter(entry -> entry.getValue() != null)
        .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
    builder.identifier(usedShape);

    return builder.build();
  }

  private static void chainSuperclasses(NodeShape nodeShape, Map<Resource, NodeShape> nodeShapeMap, List<IRI> parents) {
    if (Objects.nonNull(nodeShape.getParent())) {
      if (nodeShapeMap.containsKey(nodeShape.getParent())) {
        if (parents.contains(nodeShape.getParent())) {
          throw invalidConfigurationException("Introducing cyclic reference by inheriting parent {} on nodeshape {}",
              nodeShape.getParent(), nodeShape.getIdentifier());
        }
        parents.add(nodeShape.getParent());
        chainSuperclasses(nodeShapeMap.get(nodeShape.getParent()), nodeShapeMap, parents);
      } else {
        throw invalidConfigurationException("Nodeshape {} tries to inherit from unexisting parent {}",
            nodeShape.getIdentifier(), nodeShape.getParent());
      }
    }
  }

  public static void processInheritance(@NonNull NodeShape nodeShape, @NonNull Map<Resource, NodeShape> nodeShapeMap) {
    ArrayList<IRI> parents = new ArrayList<>();
    chainSuperclasses(nodeShape, nodeShapeMap, parents);
    parents.forEach(parent -> {
      var parentShape = nodeShapeMap.get(parent);
      parentShape.getPropertyShapes()
          .forEach((key, value) -> {
            if (!nodeShape.getPropertyShapes()
                .containsKey(key)) {
              nodeShape.getPropertyShapes()
                  .put(key, value);
            }
          });
      if (!parentShape.getClasses()
          .isEmpty() && nodeShape.getClasses()
              .isEmpty()) {
        nodeShape.getClasses()
            .addAll(parentShape.getClasses());
      }
    });
  }
}
