package org.dotwebstack.framework.core;

import static org.dotwebstack.framework.core.Constants.BUILDING_BUILT_AT_EXAMPLE_1;
import static org.dotwebstack.framework.core.Constants.BUILDING_BUILT_AT_FIELD;
import static org.dotwebstack.framework.core.Constants.BUILDING_BUILT_AT_YEAR_EXAMPLE_1;
import static org.dotwebstack.framework.core.Constants.BUILDING_BUILT_AT_YEAR_FIELD;
import static org.dotwebstack.framework.core.Constants.BUILDING_FIELD;
import static org.dotwebstack.framework.core.Constants.BUILDING_HEIGHT_EXAMPLE_1;
import static org.dotwebstack.framework.core.Constants.BUILDING_HEIGHT_FIELD;
import static org.dotwebstack.framework.core.Constants.BUILDING_IDENTIFIER_EXAMPLE_1;
import static org.dotwebstack.framework.core.Constants.BUILDING_IDENTIFIER_FIELD;
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
    String query = "{ building(identifier: \"123\") { identifier, height, builtAt, builtAtYear }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors().isEmpty(), is(equalTo(true)));
    Map<String, Object> data = result.getData();
    assertThat(data, IsMapContaining.hasEntry(BUILDING_FIELD, ImmutableMap
        .of(BUILDING_IDENTIFIER_FIELD, BUILDING_IDENTIFIER_EXAMPLE_1, BUILDING_HEIGHT_FIELD,
            BUILDING_HEIGHT_EXAMPLE_1, BUILDING_BUILT_AT_FIELD, BUILDING_BUILT_AT_EXAMPLE_1,
            BUILDING_BUILT_AT_YEAR_FIELD, BUILDING_BUILT_AT_YEAR_EXAMPLE_1)));
  }

}
