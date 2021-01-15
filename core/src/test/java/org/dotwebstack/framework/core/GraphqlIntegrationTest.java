package org.dotwebstack.framework.core;


import graphql.GraphQL;
import org.dotwebstack.framework.test.TestApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestApplication.class)
class GraphqlIntegrationTest {

  @Autowired
  private GraphQL graphQL;

  // @Test
  // void graphqlQuery_ReturnsMap_ForObjectQueryField() {
  // // Arrange
  // String query = "{ brewery(identifier: \"123\") { identifier, name, founded, foundedAtYear }}";
  //
  // // Act
  // ExecutionResult result = graphQL.execute(query);
  //
  // // Assert
  // assertThat(result.getErrors()
  // .isEmpty(), is(equalTo(true)));
  // Map<String, Object> data = result.getData();
  //
  // assertThat(data,
  // IsMapContaining.hasEntry(BREWERY_FIELD,
  // ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, BREWERY_IDENTIFIER_EXAMPLE_1, BREWERY_NAME_FIELD,
  // BREWERY_NAME_EXAMPLE_1, BREWERY_FOUNDED_FIELD, BREWERY_FOUNDED_EXAMPLE_1,
  // BREWERY_FOUNDED_AT_YEAR_FIELD,
  // BREWERY_FOUNDED_AT_YEAR_EXAMPLE_1)));
  // }

}
