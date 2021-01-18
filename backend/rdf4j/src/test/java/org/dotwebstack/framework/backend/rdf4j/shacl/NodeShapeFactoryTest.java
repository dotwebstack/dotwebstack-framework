package org.dotwebstack.framework.backend.rdf4j.shacl;

import static org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeFactory.processInheritance;
import static org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PropertyPathFactoryTest.loadShapeModel;
import static org.eclipse.rdf4j.sail.memory.model.MemValue.EMPTY_LIST;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.sail.memory.model.MemBNode;
import org.eclipse.rdf4j.sail.memory.model.MemIRI;
import org.eclipse.rdf4j.sail.memory.model.MemStatement;
import org.eclipse.rdf4j.sail.memory.model.MemStatementList;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NodeShapeFactoryTest {

  private static Model shapeModel;

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private static final IRI INGREDIENT_CLASS = VF.createIRI("https://github.com/dotwebstack/beer/def#Ingredient");

  private static final IRI INGREDIENT_PATH = VF.createIRI("https://github.com/dotwebstack/beer/def#ingredient");

  private static final IRI BREWERY_SHAPE = VF.createIRI("https://github.com/dotwebstack/beer/shapes#Brewery");

  private static final IRI INGREDIENT_SHAPE = VF.createIRI("https://github.com/dotwebstack/beer/shapes#Ingredient");

  private static final IRI SUPPLEMENT_CLASS = VF.createIRI("https://github.com/dotwebstack/beer/def#Supplement");

  private static final IRI TEST_CLASS = VF.createIRI("https://github.com/dotwebstack/beer/def#Test");

  private static final IRI SUPPLEMENT_SHAPE = VF.createIRI("https://github.com/dotwebstack/beer/shapes#Supplement");

  @Mock
  private MemBNode mockMemBNode;

  @Mock
  private MemStatementList mockMemStatements;

  @Mock
  private MemStatement mockMemStatement;

  @Mock
  private MemIRI mockMemStatementPredicate;

  @Mock
  private MemIRI mockMemStatementObject;

  @BeforeAll
  static void setUpBefore() throws IOException {
    shapeModel = loadShapeModel();
  }

  @Test
  void get_returnShOrShape_ForGivenModel() {
    // Arrange
    NodeShapeRegistry registry = new NodeShapeRegistry("https://github.com/dotwebstack/beer/shapes#");

    Map<Resource, NodeShape> nodeShapeMap = new HashMap<>();
    Models.subjectIRIs(shapeModel.filter(null, RDF.TYPE, SHACL.NODE_SHAPE))
        .forEach(subject -> NodeShapeFactory.createShapeFromModel(shapeModel, subject, nodeShapeMap));
    nodeShapeMap.values()
        .forEach(shape -> {
          processInheritance(shape, nodeShapeMap);
          registry.register(shape.getIdentifier(), shape);
        });

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
        .next(), hasItems(SUPPLEMENT_CLASS, TEST_CLASS));

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
        .next(), hasItems(INGREDIENT_CLASS, TEST_CLASS));
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

  @Test
  public void getClassIri_returnsClassIri_forClassStatement() {
    // Arrange
    when(mockMemBNode.getSubjectStatementList()).thenReturn(mockMemStatements);
    when(mockMemStatements.size()).thenReturn(1);
    when(mockMemStatements.get(0)).thenReturn(mockMemStatement);
    when(mockMemStatement.getPredicate()).thenReturn(mockMemStatementPredicate);
    when(mockMemStatementPredicate.toString()).thenReturn("http://www.w3.org/ns/shacl#class");
    when(mockMemStatement.getObject()).thenReturn(mockMemStatementObject);
    when(mockMemStatementObject.toString()).thenReturn("http://www.example.com#Beer");

    // Act & Assert
    assertThat(NodeShapeFactory.getClassIri(mockMemBNode)
        .toString(), is(equalTo("http://www.example.com#Beer")));
  }

  @Test
  public void getClassIri_returnsNull_forNonClassIri() {
    // Arrange
    when(mockMemBNode.getSubjectStatementList()).thenReturn(mockMemStatements);
    when(mockMemStatements.size()).thenReturn(1);
    when(mockMemStatements.get(0)).thenReturn(mockMemStatement);
    when(mockMemStatement.getPredicate()).thenReturn(mockMemStatementPredicate);
    when(mockMemStatementPredicate.toString()).thenReturn("http://www.w3.org/ns/rdf#type");

    // Act & Assert
    assertNull(NodeShapeFactory.getClassIri(mockMemBNode));
  }

  @Test
  public void getClassIri_returnsNull_forEmptyList() {
    when(mockMemBNode.getSubjectStatementList()).thenReturn(EMPTY_LIST);

    // Act & Assert
    assertNull(NodeShapeFactory.getClassIri(mockMemBNode));
  }

  @Test
  public void getClassIri_returnsNull_forListWithMoreThenOneStatement() {
    when(mockMemBNode.getSubjectStatementList()).thenReturn(mockMemStatements);
    when(mockMemStatements.size()).thenReturn(2);

    // Act & Assert
    assertNull(NodeShapeFactory.getClassIri(mockMemBNode));
  }
}
