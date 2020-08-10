package org.dotwebstack.framework.backend.rdf4j.query.helper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import java.util.Optional;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.query.model.Aggregate;
import org.dotwebstack.framework.backend.rdf4j.query.model.AggregateType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregateHelperTest {

  @Mock
  private GraphQLFieldDefinition fieldDefinitionMock;

  @Mock
  private GraphQLDirective aggregateDirective;

  @Mock
  private GraphQLArgument aggregateArgument;

  @Test
  void resolveAggregate_returnsEmpty_forFieldWithoutAggregateDirective() {
    // Arrange
    when(fieldDefinitionMock.getDirective(Rdf4jDirectives.AGGREGATE_NAME)).thenReturn(null);

    // Act & Assert
    assertThat(AggregateHelper.resolveAggregate(fieldDefinitionMock, any()), is(equalTo(Optional.empty())));
  }

  @Test
  void resolveAggregate_returnsEmpty_forFieldWithoutAggregateType() {
    // Arrange
    when(fieldDefinitionMock.getDirective(Rdf4jDirectives.AGGREGATE_NAME)).thenReturn(aggregateDirective);
    when(aggregateDirective.getArgument(Rdf4jDirectives.AGGREGATE_TYPE)).thenReturn(null);

    // Act & Assert
    assertThat(AggregateHelper.resolveAggregate(fieldDefinitionMock, any()), is(equalTo(Optional.empty())));
  }

  @Test
  void resolveAggregate_returnsAggregate_forFieldWithAggregateDirective() {
    // Arrange
    when(fieldDefinitionMock.getDirective(Rdf4jDirectives.AGGREGATE_NAME)).thenReturn(aggregateDirective);
    when(aggregateDirective.getArgument(Rdf4jDirectives.AGGREGATE_TYPE)).thenReturn(aggregateArgument);
    when(aggregateArgument.getValue()).thenReturn(AggregateType.COUNT);

    // Act & Assert
    assertThat(AggregateHelper.resolveAggregate(fieldDefinitionMock, any()), is(equalTo(Optional.of(Aggregate.builder()
        .type(AggregateType.COUNT)
        .build()))));
  }

}
