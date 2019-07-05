package org.dotwebstack.framework.backend.rdf4j;

import static org.dotwebstack.framework.backend.rdf4j.Constants.BEERS_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEERTYPES_RAW_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEER_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEER_IDENTIFIER_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERIES_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_ADDRESS_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_FOUNDED_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_FOUNDED_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_IDENTIFIER_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_IDENTIFIER_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_NAME_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_NAME_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.INGREDIENTS_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.INGREDIENTS_NAME_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.SCHEMA_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import graphql.ExecutionResult;
import graphql.GraphQL;
import java.time.ZonedDateTime;
import java.util.Map;
import org.dotwebstack.framework.test.TestApplication;
import org.hamcrest.collection.IsMapContaining;
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
    String query = "{ brewery(identifier: \"123\") { identifier, name, founded }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .isEmpty(), is(equalTo(true)));
    Map<String, Object> data = result.getData();
    assertThat(data,
        IsMapContaining.hasEntry(BREWERY_FIELD,
            ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, BREWERY_IDENTIFIER_EXAMPLE_1.stringValue(), BREWERY_NAME_FIELD,
                BREWERY_NAME_EXAMPLE_1.stringValue(), BREWERY_FOUNDED_FIELD,
                ZonedDateTime.parse(BREWERY_FOUNDED_EXAMPLE_1.stringValue()))));
  }

  @Test
  void graphqlQuery_ReturnsMap_ForNestedListNonNullQuery() {
    // Arrange
    String query = "{ beer(identifier: \"6\") { identifier, beerTypesRaw { name }  }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .isEmpty(), is(equalTo(true)));
    Map<String, Object> data = result.getData();
    assertThat(data,
        IsMapContaining.hasEntry(BEER_FIELD,
            ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, BEER_IDENTIFIER_EXAMPLE_1.stringValue(), BEERTYPES_RAW_FIELD,
                ImmutableList.of(ImmutableMap.of(SCHEMA_NAME.getLocalName(), "Bitter"),
                    ImmutableMap.of(SCHEMA_NAME.getLocalName(), "Ale")))));
  }

  @Test
  void graphqlQuery_ReturnsMap_ForObjectQueryNestedField() {
    // Arrange
    String query = "{ brewery(identifier: \"123\") { identifier, name, address { postalCode }}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .isEmpty(), is(equalTo(true)));
    Map<String, Object> data = result.getData();
    assertThat(data,
        IsMapContaining.hasEntry(BREWERY_FIELD,
            ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, BREWERY_IDENTIFIER_EXAMPLE_1.stringValue(), BREWERY_NAME_FIELD,
                BREWERY_NAME_EXAMPLE_1.stringValue(), BREWERY_ADDRESS_FIELD,
                ImmutableMap.of("postalCode", "2841 XB"))));
  }

  @Test
  void graphqlQuery_ReturnsMap_ForQueryWithFilterWithoutOperatorOnStringField() {
    // Arrange
    String query = "{ breweries(name: \"Brouwerij 1923\") { identifier, name }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .isEmpty(), is(true));
    Map<String, Object> data = result.getData();

    assertThat(data, IsMapContaining.hasEntry(BREWERIES_FIELD, ImmutableList.of(
        ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "123", BREWERY_NAME_FIELD, BREWERY_NAME_EXAMPLE_1.stringValue()))));
  }

  @Test
  void graphqlQuery_ReturnsResult_forNestedQuery() {
    // Arrange
    String query = "{ breweries(name: \"Brouwerij 1923\"){ beers { ingredients { name }}}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .isEmpty(), is(equalTo(true)));
    Map<String, Object> data = result.getData();

    assertThat(data,
        IsMapContaining.hasEntry(BREWERIES_FIELD, ImmutableList.of(ImmutableMap.of(BEERS_FIELD, ImmutableList.of(
            ImmutableMap.of(INGREDIENTS_FIELD, ImmutableList.of(ImmutableMap.of(INGREDIENTS_NAME_FIELD, "Hop"))))))));
  }


  @Test
  void graphqlQuery_ReturnsMap_ForQueryWithFilterOnDateTimeField() {
    // Arrange
    String query = "{ breweries(foundedAfter: \"2018-05-29T09:30:10+02:00\") { identifier, name }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .isEmpty(), is(true));
    Map<String, Object> data = result.getData();

    assertThat(data, IsMapContaining.hasEntry(BREWERIES_FIELD, ImmutableList.of(
        ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "123", BREWERY_NAME_FIELD, BREWERY_NAME_EXAMPLE_1.stringValue()))));
  }

  @Test
  void graphqlQuery_ReturnsMap_ForQueryWithTwoFiltersOnDateTimeField() {
    // Arrange
    String query =
        "{ breweries(foundedAfter: \"1990-01-01T00:00:00+02:00\", foundedBefore: \"2011-01-01T00:00:00+02:00\") "
            + "{ identifier, name }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .isEmpty(), is(true));
    Map<String, Object> data = result.getData();

    assertThat(data,
        IsMapContaining.hasEntry(BREWERIES_FIELD,
            ImmutableList.of(
                ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "456", BREWERY_NAME_FIELD, "Brouwerij Het 58e Genot i.o."),
                ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "2", BREWERY_NAME_FIELD, "Brouwerij De Leckere"))));
  }

  @Test
  void graphqlQuery_ReturnsMap_ForQueryWithFilterWithListInNestedInputObjects() {
    // Arrange
    String query = "{ breweriesWithInputObject(input: {nestedInput: {nestedNestedInput: {"
        + "name: [\"Heineken Nederland\", \"Brouwerij De Leckere\"]},"
        + "foundedAfter: \"1800-01-01T00:00:00+02:00\"}}) { identifier, name }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .isEmpty(), is(true));
    Map<String, Object> data = result.getData();

    assertThat(data,
        IsMapContaining.hasEntry("breweriesWithInputObject",
            ImmutableList.of(ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "1", BREWERY_NAME_FIELD, "Heineken Nederland"),
                ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "2", BREWERY_NAME_FIELD, "Brouwerij De Leckere"))));
  }
}
