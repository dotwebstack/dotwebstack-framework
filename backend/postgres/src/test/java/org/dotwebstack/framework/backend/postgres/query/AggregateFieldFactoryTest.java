package org.dotwebstack.framework.backend.postgres.query;

import graphql.schema.SelectedField;
import org.jooq.Field;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AggregateFieldFactoryTest {

  private AggregateFieldFactory aggregateFieldFactory = new AggregateFieldFactory();

  @Test
  void create_returnsSumField_ForArgumentIntSum() {
    SelectedField mockedField = mockSelectedField("intSum");
    Field<?> actualAggregateField = aggregateFieldFactory.create(mockedField, "beers", "soldPerYear");

    Field<?> expectedAggregateField = DSL.coalesce(DSL.sum(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class)), BigDecimal.ZERO)
        .cast(Integer.class);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentIntMin() {
    SelectedField mockedField = mockSelectedField("intMin");
    Field<?> actualAggregateField = aggregateFieldFactory.create(mockedField, "beers", "soldPerYear");

    Field<?> expectedAggregateField = DSL.coalesce(DSL.min(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class)), BigDecimal.ZERO)
        .cast(Integer.class);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentIntMax() {
    SelectedField mockedField = mockSelectedField("intMax");
    Field<?> actualAggregateField = aggregateFieldFactory.create(mockedField, "beers", "soldPerYear");

    Field<?> expectedAggregateField = DSL.coalesce(DSL.max(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class)), BigDecimal.ZERO)
        .cast(Integer.class);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentIntAvg() {
    SelectedField mockedField = mockSelectedField("intAvg");
    Field<?> actualAggregateField = aggregateFieldFactory.create(mockedField, "beers", "soldPerYear");

    Field<?> expectedAggregateField = DSL.coalesce(DSL.avg(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class)), BigDecimal.ZERO)
        .cast(Integer.class);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentFloatSum() {
    SelectedField mockedField = mockSelectedField("floatSum");
    Field<?> actualAggregateField = aggregateFieldFactory.create(mockedField, "beers", "soldPerYear");

    Field<?> expectedAggregateField = DSL.coalesce(DSL.sum(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class)), BigDecimal.ZERO);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentFloatMin() {
    SelectedField mockedField = mockSelectedField("floatMin");
    Field<?> actualAggregateField = aggregateFieldFactory.create(mockedField, "beers", "soldPerYear");

    Field<?> expectedAggregateField = DSL.coalesce(DSL.min(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class)), BigDecimal.ZERO);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentFloatMax() {
    SelectedField mockedField = mockSelectedField("floatMax");
    Field<?> actualAggregateField = aggregateFieldFactory.create(mockedField, "beers", "soldPerYear");

    Field<?> expectedAggregateField = DSL.coalesce(DSL.max(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class)), BigDecimal.ZERO);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentFloatAvg() {
    SelectedField mockedField = mockSelectedField("floatAvg");
    Field<?> actualAggregateField = aggregateFieldFactory.create(mockedField, "beers", "soldPerYear");

    Field<?> expectedAggregateField = DSL.coalesce(DSL.avg(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class)), BigDecimal.ZERO);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_throwsException_ForUnknownArgument(){
    assertThrows(IllegalArgumentException.class,
        () -> {
          SelectedField unknownField = mockSelectedField("unknownField");
          aggregateFieldFactory.create(unknownField, "beers", "soldPerYear");
      });
  }

  private SelectedField mockSelectedField(String fieldName){
    SelectedField selectedField = mock(SelectedField.class);
    when(selectedField.getName()).thenReturn(fieldName);
    return selectedField;
  }
}
