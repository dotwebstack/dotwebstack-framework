package org.dotwebstack.framework.core.datafetchers.aggregate;

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
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.STRING_JOIN_FIELD;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AggregateConfigurerTest {

  private AggregateConfigurer aggregateConfigurer;

  private TypeDefinitionRegistry dataFetchingEnvironment;

  @BeforeEach
  void beforeAll() {
    aggregateConfigurer = new AggregateConfigurer();
    dataFetchingEnvironment = new TypeDefinitionRegistry();
  }

  @Test
  @SuppressWarnings("rawtypes")
  void configureTypeDefinitionRegistry_addGeometryDef_always() {
    List<String> fieldNames = List.of(COUNT_FIELD, INT_SUM_FIELD, INT_MIN_FIELD, INT_MAX_FIELD, INT_AVG_FIELD,
        FLOAT_SUM_FIELD, FLOAT_MIN_FIELD, FLOAT_MAX_FIELD, FLOAT_AVG_FIELD, STRING_JOIN_FIELD);

    aggregateConfigurer.configureTypeDefinitionRegistry(dataFetchingEnvironment);

    Optional<TypeDefinition> optional = dataFetchingEnvironment.getType(AGGREGATE_TYPE);
    assertThat(optional.isPresent(), is(true));
    assertThat(optional.get(), instanceOf(ObjectTypeDefinition.class));
    ObjectTypeDefinition enumTypeDefinition = (ObjectTypeDefinition) optional.get();

    for (FieldDefinition field : enumTypeDefinition.getFieldDefinitions()) {
      assertThat(fieldNames.contains(field.getName()), is(Boolean.TRUE));
    }
  }
}
