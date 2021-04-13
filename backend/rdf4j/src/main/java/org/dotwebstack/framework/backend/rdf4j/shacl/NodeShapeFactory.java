package org.dotwebstack.framework.backend.rdf4j.shacl;

import static org.dotwebstack.framework.backend.rdf4j.ValueUtils.findOptionalPropertyIri;
import static org.dotwebstack.framework.backend.rdf4j.ValueUtils.findRequiredProperty;
import static org.dotwebstack.framework.backend.rdf4j.ValueUtils.findRequiredPropertyIri;
import static org.dotwebstack.framework.backend.rdf4j.ValueUtils.findRequiredPropertyLiteral;
import static org.dotwebstack.framework.backend.rdf4j.helper.MemStatementListHelper.listOf;
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
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.sail.memory.model.MemBNode;
import org.eclipse.rdf4j.sail.memory.model.MemIRI;
import org.eclipse.rdf4j.sail.memory.model.MemResource;
import org.eclipse.rdf4j.sail.memory.model.MemStatement;
import org.eclipse.rdf4j.sail.memory.model.MemStatementList;
import org.eclipse.rdf4j.sail.memory.model.MemValue;

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

    NodeShape nodeShape = NodeShape.builder()
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
        .filter(NodeShapeFactory::hasPathPropery)
        .collect(Collectors.toList());
  }

  private static Set<IRI> getOrClassConstraints(Model shapeModel, Resource identifier) {
    return Models.getPropertyResources(shapeModel, identifier, SHACL.OR)
        .stream()
        .map(or -> unwrapOrStatements(shapeModel, or))
        .flatMap(List::stream)
        .map(NodeShapeFactory::getClassIri)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  static IRI getClassIri(Resource resource) {
    if (resource instanceof MemBNode) {
      MemStatementList subjectStatements = ((MemBNode) resource).getSubjectStatementList();
      if (subjectStatements.size() == 1 && Objects.equals(SHACL.CLASS, subjectStatements.get(0)
          .getPredicate())) {
        MemValue value = subjectStatements.get(0)
            .getObject();
        if (value instanceof IRI) {
          return (IRI) value;
        }
      }
    }
    return null;
  }

  private static List<Resource> getShaclOrShapes(Model shapeModel, Resource identifier) {
    return Models.getPropertyResources(shapeModel, identifier, SHACL.OR)
        .stream()
        .map(or -> unwrapOrStatements(shapeModel, or).stream()
            .map(resource -> {
              /*
               * All the individual childs under an sh:or statement are enriched with the shared values on the
               * same level as the sh:or. The sh:or is excluded from this enrichment. These enrichments are added
               * to the model for later reuse.
               */
              shapeModel.filter(identifier, null, null)
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
                  });
              return resource;
            })
            .collect(Collectors.toList()))
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private static boolean hasPathPropery(Resource orShape) {
    return listOf(((MemBNode) orShape).getSubjectStatementList()).stream()
        .map(MemStatement::getPredicate)
        .anyMatch(SHACL.PATH::equals);
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
      IRI nodeIri = findRequiredPropertyIri(shapeModel, usedShape, SHACL.NODE);
      builder.node(createShapeFromModel(shapeModel, nodeIri, nodeShapeMap));

      usedShape = nodeIri;
    } else if (ValueUtils.isPropertyPresent(shapeModel, usedShape, SHACL.NODE)) {
      MemBNode bnode = (MemBNode) findRequiredProperty(shapeModel, usedShape, SHACL.NODE);
      builder.node(createShapeFromModel(shapeModel, bnode, nodeShapeMap));
      usedShape = bnode;
    }

    if (ValueUtils.isPropertyIriPresent(shapeModel, usedShape, SHACL.NODE_KIND_PROP)) {
      IRI nodeKind = findRequiredPropertyIri(shapeModel, usedShape, SHACL.NODE_KIND_PROP);
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
        .filter(MemBNode.class::isInstance)
        .map(resource -> resource.getSubjectStatementList()
            .get(0))
        .findFirst()
        .ifPresent(rest -> shapes.addAll(unwrapOrStatements(shapeModel, rest.getSubject())));

    return shapes;
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
      NodeShape parentShape = nodeShapeMap.get(parent);
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
