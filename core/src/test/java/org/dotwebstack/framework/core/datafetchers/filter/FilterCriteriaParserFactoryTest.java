package org.dotwebstack.framework.core.datafetchers.filter;

import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputType;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilterCriteriaParserFactoryTest {

  @Mock
  private FilterCriteriaParser filterCriteriaParser;

  private FilterCriteriaParserFactory filterCriteriaParserFactory;

  @BeforeEach
  void doBefore() {
    filterCriteriaParserFactory = new FilterCriteriaParserFactory(List.of(filterCriteriaParser));
  }

  @Test
  void getFilterCriteriaParser_returnsParser_forSupportedInputObjectField() {
    when(filterCriteriaParser.supports(any(GraphQLInputObjectField.class))).thenReturn(true);

    GraphQLInputObjectField inputObjectField = newInputObjectField().name("test")
        .type(mock(GraphQLInputType.class))
        .build();

    FilterCriteriaParser result = filterCriteriaParserFactory.getFilterCriteriaParser(inputObjectField);

    assertThat(result, equalTo(filterCriteriaParser));
  }

  @Test
  void getFilterCriteriaParser_throwsException_forUnsupportedInputObjectField() {
    when(filterCriteriaParser.supports(any(GraphQLInputObjectField.class))).thenReturn(false);

    GraphQLInputObjectField inputObjectField = newInputObjectField().name("test")
        .type(mock(GraphQLInputType.class))
        .build();

    assertThrows(NoSuchElementException.class,
        () -> filterCriteriaParserFactory.getFilterCriteriaParser(inputObjectField));
  }
}
