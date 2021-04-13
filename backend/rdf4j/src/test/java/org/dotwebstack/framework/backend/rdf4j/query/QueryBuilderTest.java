package org.dotwebstack.framework.backend.rdf4j.query;


import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.SelectedField;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.rdf4j.config.Rdf4jFieldConfiguration;
import org.dotwebstack.framework.backend.rdf4j.config.Rdf4jTypeConfiguration;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.datafetchers.FieldKeyCondition;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueryBuilderTest {

  private static final String FIELD_IDENTIFIER = "identifier";

  private static final String FIELD_NAME = "name";

  private static final String FIELD_BREWERY = "brewery";

  @Mock
  private NodeShapeRegistry nodeShapeRegistry;

  private final SimpleValueFactory simpleValueFactory = SimpleValueFactory.getInstance();

  private QueryBuilder queryBuilder;

  @BeforeEach
  void beforeAll() {
    queryBuilder = new QueryBuilder(nodeShapeRegistry);
  }

  @Test
  void buildwithKeyCondition() {
    Rdf4jTypeConfiguration typeConfiguration = createBeerTypeConfiguration();

    when(nodeShapeRegistry.get("Beer")).thenReturn(NodeShape.builder()
        .name("Beer")
        .classes(Set.of(Set.of(simpleValueFactory.createIRI("https://github.com/dotwebstack/beer/def#Beer"))))
        .propertyShapes(Map.of("identifier", PropertyShape.builder()
            .path(PredicatePath.builder()
                .iri(simpleValueFactory.createIRI("https://github.com/dotwebstack/beer/def#identifier"))
                .build())
            .build(), "name",
            PropertyShape.builder()
                .path(PredicatePath.builder()
                    .iri(simpleValueFactory.createIRI("https://github.com/dotwebstack/beer/def#name"))
                    .build())
                .build()))
        .build());

    FieldKeyCondition keyCondition = FieldKeyCondition.builder()
        .fieldValues(Map.of("identifier", "id-1"))
        .build();

    List<SelectedField> selectedFields = List.of(mockSelectedField(FIELD_IDENTIFIER,
        GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_IDENTIFIER)
            .type(Scalars.GraphQLString)
            .build()),
        mockSelectedField(FIELD_NAME, GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_NAME)
            .type(Scalars.GraphQLString)
            .build()));

    DataFetchingFieldSelectionSet selectionSet = mockDataFetchingFieldSelectionSet(selectedFields);

    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, selectionSet, keyCondition);

    assertThat(queryHolder, notNullValue());
    assertThat(queryHolder.getQuery(), equalTo("SELECT ?x2 ?x4\n"
        + "WHERE { ?x1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/dotwebstack/beer/def#Beer> .\n"
        + "OPTIONAL { ?x1 <https://github.com/dotwebstack/beer/def#identifier> ?x2 . }\n"
        + "OPTIONAL { ?x1 <https://github.com/dotwebstack/beer/def#name> ?x4 . }\n"
        + "FILTER ( ?x2 IN ( \"id-1\" ) ) }\n" + "LIMIT 10\n"));
  }

  @Test
  void buildwithoutKeyCondition() {
    Rdf4jTypeConfiguration typeConfiguration = createBeerTypeConfiguration();

    when(nodeShapeRegistry.get("Beer")).thenReturn(NodeShape.builder()
        .name("Beer")
        .classes(Set.of(Set.of(simpleValueFactory.createIRI("https://github.com/dotwebstack/beer/def#Beer"))))
        .propertyShapes(Map.of("identifier", PropertyShape.builder()
            .path(PredicatePath.builder()
                .iri(simpleValueFactory.createIRI("https://github.com/dotwebstack/beer/def#identifier"))
                .build())
            .build(), "name",
            PropertyShape.builder()
                .path(PredicatePath.builder()
                    .iri(simpleValueFactory.createIRI("https://github.com/dotwebstack/beer/def#name"))
                    .build())
                .build()))
        .build());

    List<SelectedField> selectedFields = List.of(mockSelectedField(FIELD_IDENTIFIER,
        GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_IDENTIFIER)
            .type(Scalars.GraphQLString)
            .build()),
        mockSelectedField(FIELD_NAME, GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_NAME)
            .type(Scalars.GraphQLString)
            .build()));

    DataFetchingFieldSelectionSet selectionSet = mockDataFetchingFieldSelectionSet(selectedFields);

    QueryHolder queryHolder = queryBuilder.build(typeConfiguration, selectionSet, (KeyCondition) null);

    assertThat(queryHolder, notNullValue());
    assertThat(queryHolder.getQuery(), equalTo("SELECT ?x2 ?x3\n"
        + "WHERE { ?x1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/dotwebstack/beer/def#Beer> .\n"
        + "OPTIONAL { ?x1 <https://github.com/dotwebstack/beer/def#identifier> ?x2 . }\n"
        + "OPTIONAL { ?x1 <https://github.com/dotwebstack/beer/def#name> ?x3 . } }\n" + "LIMIT 10\n"));
  }

  private Rdf4jTypeConfiguration createBeerTypeConfiguration() {
    Rdf4jTypeConfiguration typeConfiguration = new Rdf4jTypeConfiguration();

    KeyConfiguration keyConfiguration = new KeyConfiguration();
    keyConfiguration.setField(FIELD_IDENTIFIER);
    typeConfiguration.setKeys(List.of(keyConfiguration));

    typeConfiguration.setFields(new HashMap<>(
        Map.of(FIELD_IDENTIFIER, new Rdf4jFieldConfiguration(), FIELD_NAME, new Rdf4jFieldConfiguration())));

    typeConfiguration.setName("Beer");

    typeConfiguration.init(Map.of(), newObjectTypeDefinition().name("Beer")
        .fieldDefinition(newFieldDefinition().name(FIELD_IDENTIFIER)
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_NAME)
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_BREWERY)
            .build())
        .build());

    return typeConfiguration;
  }

  private SelectedField mockSelectedField(String name) {
    SelectedField selectedField = mock(SelectedField.class);
    when(selectedField.getName()).thenReturn(name);
    return selectedField;
  }

  private SelectedField mockSelectedField(String name, GraphQLFieldDefinition fieldDefinition) {
    SelectedField selectedField = mock(SelectedField.class);
    when(selectedField.getName()).thenReturn(name);
    lenient().when(selectedField.getFieldDefinition())
        .thenReturn(fieldDefinition);
    lenient().when(selectedField.getFullyQualifiedName())
        .thenReturn(name);
    return selectedField;
  }

  private DataFetchingFieldSelectionSet mockDataFetchingFieldSelectionSet(String... selectedFields) {
    return mockDataFetchingFieldSelectionSet(Arrays.stream(selectedFields)
        .map(this::mockSelectedField)
        .collect(Collectors.toList()));
  }

  private DataFetchingFieldSelectionSet mockDataFetchingFieldSelectionSet(List<SelectedField> selectedFields) {
    DataFetchingFieldSelectionSet selectionSet = mock(DataFetchingFieldSelectionSet.class);

    when(selectionSet.getFields(any())).thenReturn(selectedFields);

    return selectionSet;
  }
}
