package org.dotwebstack.framework.integrationtest.graphqlrdf4j;

import static org.dotwebstack.framework.integrationtest.graphqlrdf4j.Constants.BREWERY_ADDRESS_FIELD;
import static org.dotwebstack.framework.integrationtest.graphqlrdf4j.Constants.BREWERY_FIELD;
import static org.dotwebstack.framework.integrationtest.graphqlrdf4j.Constants.BREWERY_FOUNDED_EXAMPLE_1;
import static org.dotwebstack.framework.integrationtest.graphqlrdf4j.Constants.BREWERY_FOUNDED_FIELD;
import static org.dotwebstack.framework.integrationtest.graphqlrdf4j.Constants.BREWERY_GEOMETRY_FIELD;
import static org.dotwebstack.framework.integrationtest.graphqlrdf4j.Constants.BREWERY_IDENTIFIER_EXAMPLE_1;
import static org.dotwebstack.framework.integrationtest.graphqlrdf4j.Constants.BREWERY_IDENTIFIER_FIELD;
import static org.dotwebstack.framework.integrationtest.graphqlrdf4j.Constants.BREWERY_NAME_EXAMPLE_1;
import static org.dotwebstack.framework.integrationtest.graphqlrdf4j.Constants.BREWERY_NAME_FIELD;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import graphql.ExecutionResult;
import graphql.GraphQL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.test.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SuppressWarnings("unchecked")
@SpringBootTest(classes = TestApplication.class)
public class GraphQlRdf4jIntegrationTest {

  @Autowired
  private GraphQL graphQL;

  @Test
  void graphQlQuery_ReturnsBreweries_Default() {
    String query = "{breweries{name}}";

    ExecutionResult result = graphQL.execute(query);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();

    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("breweries"));

    List<Map<String, Object>> breweries = ((List<Map<String, Object>>) data.get("breweries"));
    assertThat(breweries.size(), is(5));
    assertThat(breweries.get(0)
        .get("name"), is("Brouwerij 1923"));
  }

  @Test
  void graphqlQuery_ReturnsMap_ForObjectQueryField() {
    String query = "{ brewery(identifier: \"123\") { identifier, name, founded }}";

    ExecutionResult result = graphQL.execute(query);

    assertResultHasNoErrors(result);

    Map<String, Object> data = result.getData();
    Map<String, Object> resultMap = (Map<String, Object>) data.get(BREWERY_FIELD);

    assertThat(resultMap, hasEntry(BREWERY_IDENTIFIER_FIELD, BREWERY_IDENTIFIER_EXAMPLE_1.stringValue()));
    assertThat(resultMap, hasEntry(BREWERY_NAME_FIELD, BREWERY_NAME_EXAMPLE_1.stringValue()));
    assertThat(resultMap, hasEntry(BREWERY_FOUNDED_FIELD, LocalDate.parse(BREWERY_FOUNDED_EXAMPLE_1.stringValue())));
  }

  @Test()
  void graphqlQuery_ReturnsMap_ForObjectQueryNestedField() {
    String query = "{ brewery(identifier: \"123\") { identifier, name, address { postalCode }}}";

    ExecutionResult result = graphQL.execute(query);

    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();
    assertThat(data,
        hasEntry(BREWERY_FIELD,
            ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, BREWERY_IDENTIFIER_EXAMPLE_1.stringValue(), BREWERY_NAME_FIELD,
                BREWERY_NAME_EXAMPLE_1.stringValue(), BREWERY_ADDRESS_FIELD,
                ImmutableMap.of("postalCode", "2841 XB"))));
  }

  @Test
  void graphqlQuery_ReturnsGeometry_ForObjectQueryNestedField() {
    String query = "{ brewery(identifier: \"123\") { identifier, name, geometry { type, asWKT, asWKB }}}";

    ExecutionResult result = graphQL.execute(query);

    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();
    assertThat(data,
        hasEntry(BREWERY_FIELD,
            ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, BREWERY_IDENTIFIER_EXAMPLE_1.stringValue(), BREWERY_NAME_FIELD,
                BREWERY_NAME_EXAMPLE_1.stringValue(), BREWERY_GEOMETRY_FIELD,
                ImmutableMap.of("type", "POINT", "asWKB", "00000000014017eac6e4232933404a1bcbd2b403c4", "asWKT",
                    "POINT (5.979274334569982 52.21715768613606)"))));
  }

  private void assertResultHasNoErrors(ExecutionResult result) {
    assertThat(result.getErrors(), is(empty()));
  }
}
