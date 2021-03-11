package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.schema.SelectedField;
import java.math.BigDecimal;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregateFieldFactoryTest {

  private final AggregateFieldFactory aggregateFieldFactory = new AggregateFieldFactory();

  @Test
  void create_returnsSumField_ForArgumentIntSum() {
    SelectedField mockedField = mockSelectedField("intSum");
    Field<?> actualAggregateField = aggregateFieldFactory.create(mockedField, "beers", "soldPerYear");

    Field<?> expectedAggregateField = DSL.sum(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(Integer.class);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentIntMin() {
    SelectedField mockedField = mockSelectedField("intMin");
    Field<?> actualAggregateField = aggregateFieldFactory.create(mockedField, "beers", "soldPerYear");

    Field<?> expectedAggregateField = DSL.min(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(Integer.class);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentIntMax() {
    SelectedField mockedField = mockSelectedField("intMax");
    Field<?> actualAggregateField = aggregateFieldFactory.create(mockedField, "beers", "soldPerYear");

    Field<?> expectedAggregateField = DSL.max(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(Integer.class);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentIntAvg() {
    SelectedField mockedField = mockSelectedField("intAvg");
    Field<?> actualAggregateField = aggregateFieldFactory.create(mockedField, "beers", "soldPerYear");

    Field<?> expectedAggregateField = DSL.avg(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class))
        .cast(Integer.class);
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentFloatSum() {
    SelectedField mockedField = mockSelectedField("floatSum");
    Field<?> actualAggregateField = aggregateFieldFactory.create(mockedField, "beers", "soldPerYear");

    Field<?> expectedAggregateField = DSL.sum(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class));
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentFloatMin() {
    SelectedField mockedField = mockSelectedField("floatMin");
    Field<?> actualAggregateField = aggregateFieldFactory.create(mockedField, "beers", "soldPerYear");

    Field<?> expectedAggregateField = DSL.min(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class));
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentFloatMax() {
    SelectedField mockedField = mockSelectedField("floatMax");
    Field<?> actualAggregateField = aggregateFieldFactory.create(mockedField, "beers", "soldPerYear");

    Field<?> expectedAggregateField = DSL.max(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class));
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_returnsSumField_ForArgumentFloatAvg() {
    SelectedField mockedField = mockSelectedField("floatAvg");
    Field<?> actualAggregateField = aggregateFieldFactory.create(mockedField, "beers", "soldPerYear");

    Field<?> expectedAggregateField = DSL.avg(DSL.field(DSL.name("beers", "soldPerYear"), BigDecimal.class));
    assertThat(actualAggregateField, is(expectedAggregateField));
  }

  @Test
  void create_throwsException_ForUnknownArgument() {
    assertThrows(IllegalArgumentException.class, () -> {
      SelectedField unknownField = mockSelectedField("unknownField");
      aggregateFieldFactory.create(unknownField, "beers", "soldPerYear");
    });
  }

  private SelectedField mockSelectedField(String fieldName) {
    SelectedField selectedField = mock(SelectedField.class);
    when(selectedField.getName()).thenReturn(fieldName);
    return selectedField;
  }
}