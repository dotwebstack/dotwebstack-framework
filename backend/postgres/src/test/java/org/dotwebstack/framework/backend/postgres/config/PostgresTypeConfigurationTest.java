// package org.dotwebstack.framework.backend.postgres.config;
//
// import static graphql.language.FieldDefinition.newFieldDefinition;
// import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
// import static graphql.language.TypeName.newTypeName;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.when;
//
// import graphql.Scalars;
// import graphql.language.ObjectTypeDefinition;
// import graphql.schema.DataFetchingEnvironment;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import org.dotwebstack.framework.core.InvalidConfigurationException;
// import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
// import org.dotwebstack.framework.core.config.KeyConfiguration;
// import org.dotwebstack.framework.core.datafetchers.KeyCondition;
// import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;
// import org.junit.jupiter.api.Disabled;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
//
// @ExtendWith(MockitoExtension.class)
// class PostgresTypeConfigurationTest {
//
// private static final String FIELD_IDENTIFIER = "identifier";
//
// private static final String FIELD_NAME = "name";
//
// private static final String FIELD_INGREDIENTS = "ingredients";
//
// @Mock
// Map<String, AbstractTypeConfiguration<?>> typeMappingMock;
//
// @Test
// @Disabled
// void init_throwsException_forAggregateOf() {
// PostgresTypeConfiguration typeConfiguration = createIngredientTypeConfiguration();
//
// ObjectTypeDefinition objectTypeDefinition = createObjectTypeDefinition();
//
//
// when(typeMappingMock.get(anyString())).thenReturn(get());
// assertThrows(InvalidConfigurationException.class,
// () -> typeConfiguration.init(typeMappingMock, objectTypeDefinition));
// }
//
// AbstractTypeConfiguration get() {
// return new AbstractTypeConfiguration<>() {
// @Override
// public KeyCondition getKeyCondition(DataFetchingEnvironment environment) {
// return null;
// }
//
// @Override
// public KeyCondition getKeyCondition(String fieldName, Map<String, Object> source) {
// return null;
// }
//
// @Override
// public KeyCondition invertKeyCondition(MappedByKeyCondition mappedByKeyCondition, Map<String,
// Object> source) {
// return null;
// }
// };
// }
//
// private ObjectTypeDefinition createObjectTypeDefinition() {
// return newObjectTypeDefinition().name("Ingredient")
// .fieldDefinition(newFieldDefinition().name(FIELD_IDENTIFIER)
// .type(newTypeName(Scalars.GraphQLString.getName()).build())
// .build())
// .fieldDefinition(newFieldDefinition().name(FIELD_NAME)
// .type(newTypeName(Scalars.GraphQLString.getName()).build())
// .build())
// .fieldDefinition(newFieldDefinition().name("partOf")
// .type(newTypeName("Beer").build())
// .build())
// .build();
// }
//
// private PostgresTypeConfiguration createIngredientTypeConfiguration() {
// PostgresTypeConfiguration typeConfiguration = new PostgresTypeConfiguration();
// KeyConfiguration keyConfiguration = new KeyConfiguration();
// keyConfiguration.setField(FIELD_IDENTIFIER);
// typeConfiguration.setKeys(List.of(keyConfiguration));
//
// PostgresFieldConfiguration ingredientsFieldConfiguration = new PostgresFieldConfiguration();
// JoinTable joinTable = new JoinTable();
// joinTable.setName("db.beer_ingredient");
//
// JoinColumn joinColumn = new JoinColumn();
// joinColumn.setName("ingredient_code");
// joinColumn.setReferencedColumn("code");
//
// JoinColumn inverseJoinColumn = new JoinColumn();
// joinColumn.setName("beer_identifier");
// joinColumn.setReferencedField("identifier_beer");
//
// joinTable.setJoinColumns(List.of(joinColumn));
// joinTable.setInverseJoinColumns(List.of(inverseJoinColumn));
// ingredientsFieldConfiguration.setJoinTable(joinTable);
//
// typeConfiguration.setFields(new HashMap<>(
// Map.of(FIELD_IDENTIFIER, new PostgresFieldConfiguration(), FIELD_INGREDIENTS,
// ingredientsFieldConfiguration)));
//
// typeConfiguration.setTable("db.ingredient");
//
// return typeConfiguration;
// }
// }
