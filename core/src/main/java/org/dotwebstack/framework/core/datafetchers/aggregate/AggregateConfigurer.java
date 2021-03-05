package org.dotwebstack.framework.core.datafetchers.aggregate;

import graphql.language.FieldDefinition;
import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static graphql.language.TypeName.newTypeName;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.AGGREGATE_TYPE;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.COUNT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_AVG_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_MAX_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_MIN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_SUM_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_AVG_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_MAX_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_MIN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_SUM_FIELD;

import graphql.Scalars;
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

    // TODO? add argument validation???

    return newObjectTypeDefinition().name(AGGREGATE_TYPE)
        .fieldDefinition(newFieldDefinition().name(COUNT_FIELD)
            .type(intType)
            .build())
        .fieldDefinition(newFieldDefinition().name(INT_SUM_FIELD)
            .type(intType)
            .build())
        .fieldDefinition(newFieldDefinition().name(INT_MIN_FIELD)
            .type(intType)
            .build())
        .fieldDefinition(newFieldDefinition().name(INT_MAX_FIELD)
            .type(intType)
            .build())
        .fieldDefinition(newFieldDefinition().name(INT_AVG_FIELD)
            .type(intType)
            .build())
        .fieldDefinition(newFieldDefinition().name(FLOAT_SUM_FIELD)
            .type(floatType)
            .build())
        .fieldDefinition(newFieldDefinition().name(FLOAT_MIN_FIELD)
            .type(floatType)
            .build())
        .fieldDefinition(newFieldDefinition().name(FLOAT_MAX_FIELD)
            .type(floatType)
            .build())
        .fieldDefinition(newFieldDefinition().name(FLOAT_AVG_FIELD)
            .type(floatType)
            .build())
        .build();
  }
}
