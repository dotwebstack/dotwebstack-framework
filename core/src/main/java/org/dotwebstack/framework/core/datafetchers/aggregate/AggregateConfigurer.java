package org.dotwebstack.framework.core.datafetchers.aggregate;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static graphql.language.TypeName.newTypeName;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.AGGREGATE_TYPE;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.COUNT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.DISTINCT_ARGUMENT;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FIELD_ARGUMENT;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_AVG_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_MAX_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_MIN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_SUM_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_AVG_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_MAX_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_MIN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_SUM_FIELD;

import graphql.Scalars;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeName;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;
import org.dotwebstack.framework.core.GraphqlConfigurer;
import org.springframework.stereotype.Component;

@Component
public class AggregateConfigurer implements GraphqlConfigurer {

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    registry.add(createAggregateObjectDefinition());
  }

  private ObjectTypeDefinition createAggregateObjectDefinition() {
    TypeName intType = newTypeName(Scalars.GraphQLInt.getName()).build();
    TypeName floatType = newTypeName(Scalars.GraphQLFloat.getName()).build();
    return newObjectTypeDefinition().name(AGGREGATE_TYPE)
        .fieldDefinition(createAggregateCountField(intType))
        .fieldDefinition(createAggregateField(INT_SUM_FIELD, intType))
        .fieldDefinition(createAggregateField(INT_MIN_FIELD, intType))
        .fieldDefinition(createAggregateField(INT_MAX_FIELD, intType))
        .fieldDefinition(createAggregateField(INT_AVG_FIELD, intType))
        .fieldDefinition(createAggregateField(FLOAT_SUM_FIELD, floatType))
        .fieldDefinition(createAggregateField(FLOAT_MIN_FIELD, floatType))
        .fieldDefinition(createAggregateField(FLOAT_MAX_FIELD, floatType))
        .fieldDefinition(createAggregateField(FLOAT_AVG_FIELD, floatType))
        .build();
  }

  private FieldDefinition createAggregateCountField(TypeName intType) {
    return newFieldDefinition().name(COUNT_FIELD)
        .type(intType)
        .inputValueDefinition(newInputValueDefinition().name(FIELD_ARGUMENT)
            .type(newTypeName(Scalars.GraphQLString.getName()).build())
            .build())
        .inputValueDefinition(newInputValueDefinition().name(DISTINCT_ARGUMENT)
            .type(newTypeName(Scalars.GraphQLBoolean.getName()).build())
            .build())
        .build();
  }

  private FieldDefinition createAggregateField(String name, TypeName type) {
    return newFieldDefinition().name(name)
        .type(type)
        .inputValueDefinition(newInputValueDefinition().name(FIELD_ARGUMENT)
            .type(newTypeName(Scalars.GraphQLString.getName()).build())
            .build())
        .build();
  }
}
