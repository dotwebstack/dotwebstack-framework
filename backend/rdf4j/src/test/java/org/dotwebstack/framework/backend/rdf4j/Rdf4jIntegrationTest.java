package org.dotwebstack.framework.backend.rdf4j;

import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_BUILT_AT_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_BUILT_AT_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_HEIGHT_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_HEIGHT_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_IDENTIFIER_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_IDENTIFIER_FIELD;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableMap;
import graphql.ExecutionResult;
import graphql.GraphQL;
import java.time.ZonedDateTime;
import org.dotwebstack.framework.test.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestApplication.class)
class Rdf4jIntegrationTest {

  @Autowired
  private GraphQL graphQL;

  @Test
  void graphqlQuery_ReturnsMap_ForObjectQueryField() {
    // Arrange
    String query = "{ building(identifier: \"123\") { identifier, height, builtAt }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors().isEmpty(), is(equalTo(true)));
    assertThat(result.getData(), is(equalTo(ImmutableMap.of(
        Constants.BUILDING_FIELD,
        ImmutableMap
            .of(BUILDING_IDENTIFIER_FIELD, BUILDING_IDENTIFIER_EXAMPLE_1.stringValue(),
                BUILDING_HEIGHT_FIELD, BUILDING_HEIGHT_EXAMPLE_1.intValue(),
                BUILDING_BUILT_AT_FIELD,
                ZonedDateTime.parse(BUILDING_BUILT_AT_EXAMPLE_1.stringValue()))))));
  }

}
