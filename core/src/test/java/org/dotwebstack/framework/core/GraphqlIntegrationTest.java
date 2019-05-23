package org.dotwebstack.framework.core;

import static org.dotwebstack.framework.core.Constants.BREWERY_FIELD;
import static org.dotwebstack.framework.core.Constants.BREWERY_FOUNDED_AT_YEAR_EXAMPLE_1;
import static org.dotwebstack.framework.core.Constants.BREWERY_FOUNDED_AT_YEAR_FIELD;
import static org.dotwebstack.framework.core.Constants.BREWERY_FOUNDED_EXAMPLE_1;
import static org.dotwebstack.framework.core.Constants.BREWERY_FOUNDED_FIELD;
import static org.dotwebstack.framework.core.Constants.BREWERY_IDENTIFIER_EXAMPLE_1;
import static org.dotwebstack.framework.core.Constants.BREWERY_IDENTIFIER_FIELD;
import static org.dotwebstack.framework.core.Constants.BREWERY_NAME_EXAMPLE_1;
import static org.dotwebstack.framework.core.Constants.BREWERY_NAME_FIELD;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableMap;
import graphql.ExecutionResult;
import graphql.GraphQL;
import java.util.Map;

import org.dotwebstack.framework.test.TestApplication;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestApplication.class)
class GraphqlIntegrationTest {

  @Autowired
  private GraphQL graphQL;

  @Test
  void graphqlQuery_ReturnsMap_ForObjectQueryField() {
    // Arrange
    String query = "{ brewery(identifier: \"123\") { identifier, name, founded, foundedAtYear }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .isEmpty(), is(equalTo(true)));
    Map<String, Object> data = result.getData();

    assertThat(data,
        IsMapContaining.hasEntry(BREWERY_FIELD,
            ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, BREWERY_IDENTIFIER_EXAMPLE_1, BREWERY_NAME_FIELD,
                BREWERY_NAME_EXAMPLE_1, BREWERY_FOUNDED_FIELD, BREWERY_FOUNDED_EXAMPLE_1, BREWERY_FOUNDED_AT_YEAR_FIELD,
                BREWERY_FOUNDED_AT_YEAR_EXAMPLE_1)));
  }

}
