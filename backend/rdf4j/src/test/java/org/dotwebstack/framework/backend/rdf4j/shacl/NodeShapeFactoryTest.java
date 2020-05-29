package org.dotwebstack.framework.backend.rdf4j.shacl;

import static org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PropertyPathFactoryTest.loadShapeModel;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Map;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NodeShapeFactoryTest {

  private static Model shapeModel;

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private static final IRI INGREDIENT_CLASS = VF.createIRI("https://github.com/dotwebstack/beer/def#Ingredient");

  private static final IRI INGREDIENT_PATH = VF.createIRI("https://github.com/dotwebstack/beer/def#ingredient");

  private static final IRI INGREDIENT_SHAPE = VF.createIRI("https://github.com/dotwebstack/beer/shapes#Ingredient");

  private static final IRI SUPPLEMENT_CLASS = VF.createIRI("https://github.com/dotwebstack/beer/def#Supplement");

  private static final IRI SUPPLEMENT_SHAPE = VF.createIRI("https://github.com/dotwebstack/beer/shapes#Supplement");

  @BeforeAll
  static void setUpBefore() throws IOException {
    shapeModel = loadShapeModel();
  }

  @Test
  void get_returnShOrShape_ForGivenModel() {
    // Arrange
    NodeShapeRegistry registry = new NodeShapeRegistry("https://github.com/dotwebstack/beer/shapes#");

    Models.subjectIRIs(shapeModel.filter(null, RDF.TYPE, SHACL.NODE_SHAPE))
        .stream()
        .map(subject -> NodeShapeFactory.createShapeFromModel(shapeModel, subject))
        .forEach(shape -> registry.register(shape.getIdentifier(), shape));

    // Act
    NodeShape breweryShape = registry.get("Brewery");
    NodeShape beerShape = registry.get("Beer");

    // Assert
    assertShOr(beerShape);
    assertShOr(breweryShape);
  }

  private void assertShOr(NodeShape beerShape) {
    Map<String, PropertyShape> propertyShapes = beerShape.getPropertyShapes();

    assertThat(propertyShapes, IsMapContaining.hasKey("ingredients"));
    assertThat(propertyShapes, IsMapContaining.hasKey("supplements"));

    PropertyShape supplements = propertyShapes.get("supplements");

    assertThat(supplements.getPath()
        .toPredicate()
        .getQueryString(),
        equalTo(PredicatePath.builder()
            .iri(INGREDIENT_PATH)
            .build()
            .toPredicate()
            .getQueryString()));
    assertThat(supplements.getName(), equalTo("supplements"));
    assertThat(supplements.getNode()
        .getIdentifier(), equalTo(SUPPLEMENT_SHAPE));
    assertThat(supplements.getNode()
        .getClasses()
        .iterator()
        .next(), equalTo(SUPPLEMENT_CLASS));

    PropertyShape ingredients = propertyShapes.get("ingredients");

    assertThat(ingredients.getPath()
        .toPredicate()
        .getQueryString(),
        equalTo(PredicatePath.builder()
            .iri(INGREDIENT_PATH)
            .build()
            .toPredicate()
            .getQueryString()));
    assertThat(ingredients.getName(), equalTo("ingredients"));
    assertThat(ingredients.getNode()
        .getIdentifier(), equalTo(INGREDIENT_SHAPE));
    assertThat(ingredients.getNode()
        .getClasses()
        .iterator()
        .next(), equalTo(INGREDIENT_CLASS));
  }

  @Test
  public void validatePropertyShapes_doesNotThrowError_forValidPropertyShapes() {
    // Arrange
    Map<String, PropertyShape> propertyShapes = Map.of("beer_sh:Beer", PropertyShape.builder()
        .identifier(() -> "beer_sh:Beer")
        .constraints(Map.of(ConstraintType.MINCOUNT, VF.createLiteral(1)))
        .build());

    // Act & Assert
    assertDoesNotThrow(() -> NodeShapeFactory.validatePropertyShapes(propertyShapes));
  }

  @Test
  public void validatePropertyShapes_throwsError_forPropertyShapeWithMinCount5() {
    // Arrange
    Map<String, PropertyShape> propertyShapes = Map.of("beer_sh:Beer", PropertyShape.builder()
        .identifier(() -> "beer_sh:Beer")
        .constraints(Map.of(ConstraintType.MINCOUNT, VF.createLiteral(5)))
        .build());

    // Act & Assert
    assertThrows(InvalidConfigurationException.class, () -> NodeShapeFactory.validatePropertyShapes(propertyShapes));
  }

}
