package org.dotwebstack.framework.backend.postgres.query;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static graphql.language.TypeName.newTypeName;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.AGGREGATE_TYPE;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FIELD_ARGUMENT;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_AVG_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.STRING_JOIN_FIELD;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.SelectedField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.config.JoinColumn;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregateSelectWrapperBuilderTest {

  private static final String FIELD_IDENTIFIER = "identifier";

  private static final String FIELD_NAME = "name";

  private static final String FIELD_SOLD = "soldPerYear";

  private static final String FIELD_BREWERY = "brewery";

  private AggregateSelectWrapperBuilder aggregateSelectWrapperBuilder;

  @Mock
  private DSLContext dslContext;

  @Mock
  private AggregateFieldFactory aggregateFieldFactory;

  @BeforeEach
  void beforeEach() {
    aggregateSelectWrapperBuilder = new AggregateSelectWrapperBuilder(dslContext, aggregateFieldFactory);
  }

  @Test
  void addFields_throwsError_forNotNumeric() {
    SelectContext selectContext = new SelectContext(mock(QueryContext.class));
    PostgresTypeConfiguration typeConfiguration = createBeerTypeConfiguration();
    Map<String, SelectedField> selectedFields = Map.of("",
        mockSelectedAggregateField(INT_AVG_FIELD, GraphQLFieldDefinition.newFieldDefinition()
            .name(INT_AVG_FIELD)
            .type(Scalars.GraphQLInt)
            .build(), FIELD_NAME));

    Table<Record> table = createTable();
    assertThrows(IllegalArgumentException.class,
        () -> aggregateSelectWrapperBuilder.addFields(selectContext, typeConfiguration, table, selectedFields));
  }

  @Test
  void addFields_throwsError_forNotText() {
    SelectContext selectContext = new SelectContext(mock(QueryContext.class));
    PostgresTypeConfiguration typeConfiguration = createBeerTypeConfiguration();
    Map<String, SelectedField> selectedFields = Map.of("",
        mockSelectedAggregateField(STRING_JOIN_FIELD, GraphQLFieldDefinition.newFieldDefinition()
            .name(STRING_JOIN_FIELD)
            .type(Scalars.GraphQLInt)
            .build(), FIELD_SOLD));

    Table<Record> table = createTable();
    assertThrows(IllegalArgumentException.class,
        () -> aggregateSelectWrapperBuilder.addFields(selectContext, typeConfiguration, table, selectedFields));
  }

  @Test
  void addFields_throwsError_forUnsupportedFunction() {
    SelectContext selectContext = new SelectContext(mock(QueryContext.class));
    PostgresTypeConfiguration typeConfiguration = createBeerTypeConfiguration();
    Map<String, SelectedField> selectedFields = Map.of("",
        mockSelectedAggregateField("monkey", GraphQLFieldDefinition.newFieldDefinition()
            .name("monkey")
            .type(Scalars.GraphQLInt)
            .build(), FIELD_NAME));

    Table<Record> table = createTable();
    assertThrows(IllegalArgumentException.class,
        () -> aggregateSelectWrapperBuilder.addFields(selectContext, typeConfiguration, table, selectedFields));
  }


  private SelectedField mockSelectedAggregateField(String name, GraphQLFieldDefinition fieldDefinition,
      String fieldArgument) {
    SelectedField selectedField = mock(SelectedField.class);
    when(selectedField.getName()).thenReturn(name);
    Map<String, Object> arguments = Map.of(FIELD_ARGUMENT, fieldArgument);
    when(selectedField.getArguments()).thenReturn(arguments);
    lenient().when(selectedField.getFieldDefinition())
        .thenReturn(fieldDefinition);
    lenient().when(selectedField.getFullyQualifiedName())
        .thenReturn(name);
    GraphQLObjectType type = GraphQLObjectType.newObject()
        .name(AGGREGATE_TYPE)
        .build();
    when(selectedField.getObjectType()).thenReturn(type);
    return selectedField;
  }

  private PostgresTypeConfiguration createBeerTypeConfiguration() {
    PostgresTypeConfiguration typeConfiguration = new PostgresTypeConfiguration();
    KeyConfiguration keyConfiguration = new KeyConfiguration();
    keyConfiguration.setField(FIELD_IDENTIFIER);
    typeConfiguration.setKeys(List.of(keyConfiguration));

    PostgresFieldConfiguration breweryFieldConfiguration = new PostgresFieldConfiguration();
    JoinColumn joinColumn = new JoinColumn();
    joinColumn.setName("brewery");
    joinColumn.setReferencedField(FIELD_IDENTIFIER);
    breweryFieldConfiguration.setJoinColumns(List.of(joinColumn));

    typeConfiguration.setFields(new HashMap<>(
        Map.of(FIELD_IDENTIFIER, new PostgresFieldConfiguration(), FIELD_BREWERY, breweryFieldConfiguration)));

    typeConfiguration.setTable("db.beer");

    typeConfiguration.init(mock(DotWebStackConfiguration.class), newObjectTypeDefinition().name("Beer")
        .fieldDefinition(newFieldDefinition().name(FIELD_IDENTIFIER)
            .type(newTypeName(Scalars.GraphQLString.getName()).build())
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_NAME)
            .type(newTypeName(Scalars.GraphQLString.getName()).build())
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_BREWERY)
            .type(newTypeName("Brewery").build())
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_SOLD)
            .type(newTypeName(Scalars.GraphQLFloat.getName()).build())
            .build())
        .build());

    return typeConfiguration;
  }

  private Table<Record> createTable() {
    return DSL.table("name")
        .asTable("name");
  }
}
