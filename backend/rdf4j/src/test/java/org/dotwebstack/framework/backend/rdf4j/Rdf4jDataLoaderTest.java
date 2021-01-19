package org.dotwebstack.framework.backend.rdf4j;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLObjectType;
import graphql.schema.SelectedField;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.rdf4j.config.Rdf4jFieldConfiguration;
import org.dotwebstack.framework.backend.rdf4j.config.Rdf4jTypeConfiguration;
import org.dotwebstack.framework.backend.rdf4j.shacl.ConstraintType;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.datafetchers.KeyArgument;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.algebra.evaluation.QueryBindingSet;
import org.eclipse.rdf4j.query.impl.IteratingTupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class Rdf4jDataLoaderTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private static final String FIELD_IDENTIFIER = "identifier";

  private static final String FIELD_NAME = "name";

  private static final String NODE_BREWERY = "Brewery";

  @Captor
  private ArgumentCaptor<String> queryCapture;

  @Mock
  private LocalRepositoryManager localRepositoryManager;

  @Mock
  private NodeShapeRegistry nodeShapeRegistry;

  private Rdf4jDataLoader rdf4jDataLoader;

  @BeforeEach
  void init() {
    rdf4jDataLoader = new Rdf4jDataLoader(localRepositoryManager, nodeShapeRegistry);
  }

  @Test
  void supports_True_ForRdf4jTypeConfiguration() {
    // Arrange
    Rdf4jTypeConfiguration rdf4jTypeConfiguration = new Rdf4jTypeConfiguration();

    // Act / Assert
    assertThat(rdf4jDataLoader.supports(rdf4jTypeConfiguration), is(true));
  }

  @Test
  void supports_False_ForUnsupportedConfiguration() {
    // Arrange
    UnsupportedTypeConfiguration unsupportedTypeConfiguration = new UnsupportedTypeConfiguration();

    // Act / Assert
    assertThat(rdf4jDataLoader.supports(unsupportedTypeConfiguration), is(false));
  }

  @Test
  void loadSingle_BreweryX_ForKey() {
    // Arrange
    String identifier = "d3654375-95fa-46b4-8529-08b0f777bd6b";
    String name = "Brewery X";

    LoadEnvironment loadEnvironment = createLoadEnvironment(identifier);
    mockRepository(createBindingSet(identifier, name));

    // Act
    Mono<Map<String, Object>> result = rdf4jDataLoader.loadSingle(null, loadEnvironment);

    // Assert
    assertThat(queryCapture.getValue(), is(getQuery(identifier)));

    assertThat(result.hasElement()
        .block(), is(true));
    Map<String, Object> resultMap = result.block();
    assertThat(resultMap.size(), is(2));
    assertThat(resultMap.get(FIELD_IDENTIFIER), is(identifier));
    assertThat(resultMap.get(FIELD_NAME), is(name));
  }

  @Test
  void loadSingle_Empty_ForNonExistingKey() {
    // Arrange
    String identifier = "not-existing-identifier";

    LoadEnvironment loadEnvironment = createLoadEnvironment(identifier);
    mockRepository();

    // Act
    Mono<Map<String, Object>> result = rdf4jDataLoader.loadSingle(null, loadEnvironment);

    // Assert
    assertThat(result.hasElement()
        .block(), is(false));
  }

  private String getQuery(String identifier) {
    return String.format("SELECT ?x2 ?x3\n"
        + "WHERE { ?x1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/dotwebstack/beer/def#Brewery> .\n"
        + "?x1 <https://github.com/dotwebstack/beer/def#identifier> ?x2 .\n?x1 <http://schema.org/name> ?x3 .\n"
        + "FILTER ( ?x2 = \"%s\" ) }\nLIMIT 10\n", identifier);
  }

  private LoadEnvironment createLoadEnvironment(String identifier) {
    Rdf4jTypeConfiguration rdf4jTypeConfiguration = createRdf4jTypeConfiguration();

    PropertyShape propertyShapeIdentifier =
        createPropertyShape("https://github.com/dotwebstack/beer/def#", FIELD_IDENTIFIER);
    PropertyShape propertyShapeName = createPropertyShape("http://schema.org/", FIELD_NAME);
    NodeShape nodeShape = createNodeShape(propertyShapeIdentifier, propertyShapeName);

    GraphQLObjectType graphQLObjectType = GraphQLObjectType.newObject()
        .name(NODE_BREWERY)
        .build();
    when(nodeShapeRegistry.get(eq(graphQLObjectType))).thenReturn(nodeShape);

    KeyArgument keyArgument = KeyArgument.builder()
        .name("identifier")
        .value(identifier)
        .build();

    return LoadEnvironment.builder()
        .objectType(graphQLObjectType)
        .typeConfiguration(rdf4jTypeConfiguration)
        .selectedFields(List.of(createSelectedField(FIELD_IDENTIFIER), createSelectedField(FIELD_NAME)))
        .keyArguments(List.of(keyArgument))
        .build();
  }

  private Rdf4jTypeConfiguration createRdf4jTypeConfiguration() {
    Rdf4jTypeConfiguration rdf4jTypeConfiguration = new Rdf4jTypeConfiguration();
    KeyConfiguration keyConfiguration = new KeyConfiguration();
    keyConfiguration.setField(FIELD_IDENTIFIER);
    rdf4jTypeConfiguration.setKeys(List.of(keyConfiguration));
    rdf4jTypeConfiguration.setFields(Map.of(FIELD_IDENTIFIER, new Rdf4jFieldConfiguration()));
    return rdf4jTypeConfiguration;
  }

  private PropertyShape createPropertyShape(String namespace, String localName) {
    return PropertyShape.builder()
        .name(localName)
        .path(PredicatePath.builder()
            .iri(VF.createIRI(namespace, localName))
            .build())
        .constraints(Map.of(ConstraintType.MINCOUNT, VF.createLiteral(1)))
        .build();
  }

  private NodeShape createNodeShape(PropertyShape... propertyShapes) {
    Map<String, PropertyShape> propertyShapeMap = Arrays.stream(propertyShapes)
        .collect(Collectors.toMap(PropertyShape::getName, propertyShape -> propertyShape));

    return NodeShape.builder()
        .name(NODE_BREWERY)
        .identifier(VF.createIRI("https://github.com/dotwebstack/beer/shapes", NODE_BREWERY))
        .classes(Set.of(Set.of(VF.createIRI("https://github.com/dotwebstack/beer/def#", NODE_BREWERY))))
        .propertyShapes(propertyShapeMap)
        .build();
  }

  private SelectedField createSelectedField(String name) {
    SelectedField selectedField = mock(SelectedField.class);
    DataFetchingFieldSelectionSet dataFetchingFieldSelectionSet = mock(DataFetchingFieldSelectionSet.class);
    when(dataFetchingFieldSelectionSet.getImmediateFields()).thenReturn(Collections.emptyList());
    when(selectedField.getName()).thenReturn(name);
    when(selectedField.getSelectionSet()).thenReturn(dataFetchingFieldSelectionSet);
    return selectedField;
  }

  private QueryBindingSet createBindingSet(String identifier, String name) {
    QueryBindingSet queryBindingSet = new QueryBindingSet();
    queryBindingSet.setBinding("x2", VF.createLiteral(identifier));
    queryBindingSet.setBinding("x3", VF.createLiteral(name));
    return queryBindingSet;
  }

  private void mockRepository(QueryBindingSet... queryBindingSets) {
    Repository repository = mock(Repository.class);
    RepositoryConnection repositoryConnection = mock(RepositoryConnection.class);

    IteratingTupleQueryResult iteratingTupleQueryResult =
        new IteratingTupleQueryResult(List.of("x2", "x3"), Arrays.asList(queryBindingSets));

    TupleQuery tupleQuery = mock(TupleQuery.class);
    when(tupleQuery.evaluate()).thenReturn(iteratingTupleQueryResult);

    when(repository.getConnection()).thenReturn(repositoryConnection);
    when(repositoryConnection.prepareTupleQuery(queryCapture.capture())).thenReturn(tupleQuery);
    when(localRepositoryManager.getRepository(eq("local"))).thenReturn(repository);
  }

  private static class UnsupportedTypeConfiguration extends AbstractTypeConfiguration<UnsupportedFieldConfiguration> {
  }

  private static class UnsupportedFieldConfiguration extends AbstractFieldConfiguration {
  }
}
