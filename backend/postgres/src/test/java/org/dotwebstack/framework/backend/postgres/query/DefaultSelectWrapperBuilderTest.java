package org.dotwebstack.framework.backend.postgres.query;

import graphql.Scalars;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.SelectedField;
import org.dotwebstack.framework.backend.postgres.config.JoinColumn;
import org.dotwebstack.framework.backend.postgres.config.JoinTable;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static graphql.language.TypeName.newTypeName;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultSelectWrapperBuilderTest {

  @Mock
  private Map<String, AbstractTypeConfiguration<?>> typeMappingMock;

  @Mock
  private DSLContext dslContext;

  @Mock
  private SelectWrapperBuilderFactory factory;

  @Mock
  private DotWebStackConfiguration dotWebStackConfiguration;

  @InjectMocks
  private DefaultSelectWrapperBuilder defaultSelectWrapperBuilder;


  @BeforeEach
  public void setup() {
    when(typeMappingMock.get("Beer")).thenReturn((AbstractTypeConfiguration) new PostgresTypeConfiguration());

    PostgresTypeConfiguration beerTypeConfiguration = createBeerTypeConfiguration();

    when(typeMappingMock.get("Beer")).thenReturn((AbstractTypeConfiguration)beerTypeConfiguration);
  }

  @Test
  public void addFields_addSelectedJoinColumnForReferenceJoinColumn() {
    QueryContext mockQueryContext = mock(QueryContext.class);
    SelectContext selectContext = new SelectContext(mockQueryContext);

    PostgresTypeConfiguration ingredientTypeConfiguration = createIngredientTypeConfiguration();
    Map<String, SelectedField> selectedFields = Map.of("identifier_ingredient", mockSelectedField("identifier_ingredient",
        GraphQLFieldDefinition.newFieldDefinition()
            .name("identifier_ingredient")
            .type(Scalars.GraphQLString)
            .build()),
        "partOf", mockSelectedField("partOf", GraphQLFieldDefinition.newFieldDefinition()
            .name("partOf")
            .type(GraphQLList.list(GraphQLObjectType.newObject()
                .name("beer")
                .build()))
            .build()));

    DataFetchingFieldSelectionSet selectionSet = mock(DataFetchingFieldSelectionSet.class);
    when(selectionSet.getFields("partOf/*.*")).thenReturn(new ArrayList<>(selectedFields.values()));
    when(mockQueryContext.getSelectionSet()).thenReturn(selectionSet);

    defaultSelectWrapperBuilder.addFields(selectContext, ingredientTypeConfiguration, createTable(), selectedFields);

    List<Field> selectColumns =  (List<Field>)(List<?>)  selectContext.getSelectColumns();
    assertThat(selectColumns, notNullValue());
    assertThat(selectColumns.size(), is(2));
  }

  private PostgresTypeConfiguration createIngredientTypeConfiguration() {
    PostgresTypeConfiguration ingredientTypeConfiguration = new PostgresTypeConfiguration();
    ingredientTypeConfiguration.setName("Ingredient");
    ingredientTypeConfiguration.setTable("db.ingredient");

    KeyConfiguration keyConfiguration = new KeyConfiguration();
    keyConfiguration.setField("identifier_ingredient");
    ingredientTypeConfiguration.setKeys(List.of(keyConfiguration));

    Map<String, PostgresFieldConfiguration> fieldsMap =
        new HashMap<>(Map.of("identifier_ingredient", new PostgresFieldConfiguration(), "partOf", createIngredientPartofField()));

    ingredientTypeConfiguration.setFields(fieldsMap);

    ingredientTypeConfiguration.init(typeMappingMock, newObjectTypeDefinition().name("Ingredient")
        .fieldDefinition(newFieldDefinition().name("identifier_ingredient")
            .type(newTypeName(Scalars.GraphQLString.getName()).build())
            .build())
        .fieldDefinition(newFieldDefinition().name("name")
            .type(newTypeName(Scalars.GraphQLString.getName()).build())
            .build())
        .fieldDefinition(newFieldDefinition().name("partOf")
            .type(newTypeName("Beer").build())
            .build())
        .build());
    return ingredientTypeConfiguration;
  }

  private PostgresTypeConfiguration createBeerTypeConfiguration() {
    PostgresTypeConfiguration ingredientTypeConfiguration = new PostgresTypeConfiguration();
    ingredientTypeConfiguration.setName("Beer");
    ingredientTypeConfiguration.setTable("db.beer");

    KeyConfiguration keyConfiguration = new KeyConfiguration();
    keyConfiguration.setField("identifier_beer");
    ingredientTypeConfiguration.setKeys(List.of(keyConfiguration));

    Map<String, PostgresFieldConfiguration> fieldsMap =
        new HashMap<>(Map.of("identifier_beer", new PostgresFieldConfiguration()));

    ingredientTypeConfiguration.setFields(fieldsMap);

    ingredientTypeConfiguration.init(typeMappingMock, newObjectTypeDefinition().name("Beer")
        .fieldDefinition(newFieldDefinition().name("identifier_beer")
            .type(newTypeName(Scalars.GraphQLString.getName()).build())
            .build())
        .fieldDefinition(newFieldDefinition().name("name")
            .type(newTypeName(Scalars.GraphQLString.getName()).build())
            .build())
        .build());
    return ingredientTypeConfiguration;
  }

  private PostgresFieldConfiguration createIngredientPartofField(){
    PostgresFieldConfiguration fieldConfiguration = new PostgresFieldConfiguration();
    JoinTable joinTable = new JoinTable();
    joinTable.setName("db.beer_ingredient");

    joinTable.setJoinColumns(List.of(createPartOfJoinColumn()));
    joinTable.setInverseJoinColumns(List.of(createPartOfInverseJoinColumn()));

    fieldConfiguration.setJoinTable(joinTable);

    return fieldConfiguration;
  }

  private JoinColumn createPartOfInverseJoinColumn(){
    return new JoinColumn("beer_identifier", "identifier_beer");
  }

  private JoinColumn createPartOfJoinColumn() {
    JoinColumn joinColumn = new JoinColumn();
    joinColumn.setName("ingredient_code");
    joinColumn.setReferencedColumn( "code");
    return joinColumn;
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

  private Table<Record> createTable() {
    return DSL.table("ingredient")
        .asTable("ingredient");
  }

}
