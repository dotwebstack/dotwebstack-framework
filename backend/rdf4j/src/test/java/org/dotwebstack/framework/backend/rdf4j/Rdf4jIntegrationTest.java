package org.dotwebstack.framework.backend.rdf4j;

import static org.dotwebstack.framework.backend.rdf4j.Constants.BEERS_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEERTYPES_RAW_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEER_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEER_IDENTIFIER_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEER_NAME_FIELD;
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
import static org.dotwebstack.framework.backend.rdf4j.Constants.SUPPLEMENTS_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.SUPPLEMENTS_NAME_FIELD;
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
  void graphqlQuery_ReturnsResult_forQueryWithNesting() {
    // Arrange
    String query = "{ breweries(name: \"Alfa Brouwerij\"){ beers { ingredients { name }}}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .isEmpty(), is(equalTo(true)));
    Map<String, Object> data = result.getData();

    assertThat(data,
        IsMapContaining.hasEntry(BREWERIES_FIELD,
            ImmutableList.of(ImmutableMap.of(BEERS_FIELD,
                ImmutableList.of(ImmutableMap.of(INGREDIENTS_FIELD,
                    ImmutableList.of(ImmutableMap.of(INGREDIENTS_NAME_FIELD, "Hop"),
                        ImmutableMap.of(INGREDIENTS_NAME_FIELD, "Gerst"),
                        ImmutableMap.of(INGREDIENTS_NAME_FIELD, "Sinasappel"))))))));
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
            ImmutableList.of(ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "2", BREWERY_NAME_FIELD, "Brouwerij De Leckere"),
                ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "456", BREWERY_NAME_FIELD, "Brouwerij Het 58e Genot i.o."))));
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

  @Test
  void graphqlQuery_ReturnsMap_ForSortQueryWithDefaultSorting() {
    // Arrange
    String query = "{ breweries{ name }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .isEmpty(), is(true));
    Map<String, Object> data = result.getData();

    assertThat(data,
        IsMapContaining.hasEntry("breweries",
            ImmutableList.of(ImmutableMap.of(BREWERY_NAME_FIELD, "Alfa Brouwerij"),
                ImmutableMap.of(BREWERY_NAME_FIELD, "Brouwerij 1923"),
                ImmutableMap.of(BREWERY_NAME_FIELD, "Brouwerij De Leckere"),
                ImmutableMap.of(BREWERY_NAME_FIELD, "Brouwerij Het 58e Genot i.o."),
                ImmutableMap.of(BREWERY_NAME_FIELD, "Heineken Nederland"))));
  }

  @Test
  void graphqlQuery_ReturnsMap_ForSortQueryWithIdentifierAscSorting() {
    // Arrange
    String query = "{ breweries(sort: [{field: \"identifier\", order: ASC }]){ identifier }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .isEmpty(), is(true));
    Map<String, Object> data = result.getData();

    assertThat(data,
        IsMapContaining.hasEntry("breweries",
            ImmutableList.of(ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "1"),
                ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "123"), ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "2"),
                ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "456"), ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "789"))));
  }

  @Test
  void graphqlQuery_ReturnsMap_ForSortQueryWithIdentifierNoSortingValue() {
    // Arrange
    String query = "{ breweries(sort: [{field: \"identifier\" }]){ identifier }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .size(), is(1));
    assertThat(result.getErrors()
        .get(0)
        .getMessage(),
        is("Validation error of type WrongType: argument 'sort[0]' with value "
            + "'ArrayValue{values=[ObjectValue{objectFields=[ObjectField{name='field', "
            + "value=StringValue{value='identifier'}}]}]}' is missing required fields '[order]' @ 'breweries'"));
  }

  @Test
  void graphqlQuery_ReturnsMap_ForSortQueryWithNameDescSorting() {
    // Arrange
    String query = "{ breweries(sort: [{field: \"name\", order: DESC}]){ name }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .isEmpty(), is(true));
    Map<String, Object> data = result.getData();

    assertThat(data,
        IsMapContaining.hasEntry("breweries",
            ImmutableList.of(ImmutableMap.of(BREWERY_NAME_FIELD, "Heineken Nederland"),
                ImmutableMap.of(BREWERY_NAME_FIELD, "Brouwerij Het 58e Genot i.o."),
                ImmutableMap.of(BREWERY_NAME_FIELD, "Brouwerij De Leckere"),
                ImmutableMap.of(BREWERY_NAME_FIELD, "Brouwerij 1923"),
                ImmutableMap.of(BREWERY_NAME_FIELD, "Alfa Brouwerij"))));

  }

  @Test
  void graphqlQuery_ReturnsMap_ForSortQueryWithNameNestedSorting() {
    // Arrange
    String query = "{ breweries(sort: [{field: \"address.postalCode\", order: DESC}]){ identifier }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .isEmpty(), is(true));
    Map<String, Object> data = result.getData();

    assertThat(data,
        IsMapContaining.hasEntry("breweries",
            ImmutableList.of(ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "456"),
                ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "789"), ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "123"),
                ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "1"), ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "2"))));
  }

  @Test
  void graphqlQuery_ReturnsMap_ForSortQueryWithNameMultipleSorting() {
    // Arrange
    String query = "{ breweries(sort: [{field: \"address.postalCode\", order: DESC}, {field: \"name\", order: ASC}]){"
        + " identifier }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .isEmpty(), is(true));
    Map<String, Object> data = result.getData();

    assertThat(data,
        IsMapContaining.hasEntry("breweries",
            ImmutableList.of(ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "789"),
                ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "456"), ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "123"),
                ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "2"), ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "1"))));
  }

  @Test
  void graphqlQuery_ReturnsMap_ForSortQueryWithUnexistingSortField() {
    // Arrange
    String query = "{ breweries(sort: [{field: \"unexisting\", order: DESC}]){ identifier }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .size(), is(1));
    assertThat(result.getErrors()
        .get(0)
        .getMessage(),
        is("Exception while fetching data (/breweries) : No property shape found for name 'unexisting'"));
  }

  @Test
  void graphqlQuery_ReturnsMap_ForSortQueryWithUnexistingSortOrder() {
    // Arrange
    String query = "{ breweries(sort: [{field: \"unexisting\", order: unexisting}]){ identifier }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .size(), is(1));
    assertThat(result.getErrors()
        .get(0)
        .getMessage(),
        is("Validation error of type WrongType: argument 'sort[0].order' with value "
            + "'EnumValue{name='unexisting'}' is not a valid 'SortOrder' @ 'breweries'"));
    Map<String, Object> data = result.getData();
  }

  @Test
  void graphqlQuery_ReturnsMap_ForQueryWithNestedFilter() {
    // Arrange
    String query =
        "{ breweries(name: \"Alfa Brouwerij\"){ beers { ingredients(ingredientName: [\"Hop\", \"Gerst\"]){ name }}}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .isEmpty(), is(true));
    Map<String, Object> data = result.getData();

    assertThat(data, IsMapContaining.hasEntry(BREWERIES_FIELD,
        ImmutableList.of(ImmutableMap.of(BEERS_FIELD, ImmutableList.of(ImmutableMap.of(INGREDIENTS_FIELD, ImmutableList
            .of(ImmutableMap.of(INGREDIENTS_NAME_FIELD, "Hop"), ImmutableMap.of(INGREDIENTS_NAME_FIELD, "Gerst"))))))));
  }

  /*
   * Supplement is a list under a Beer, the path in the query to beers was optional, as was the path
   * from beers to supplements. When the filter was applied, the filter was ignored, since it was
   * optional. For that reason the whole path to the filter is made non optional. This test will test
   * for this specific use case.
   */
  @Test
  void graphqlQuery_ReturnsMap_WithNonOptionalFields() {
    // Arrange
    String query = "{breweries(name: \"Alfa Brouwerij\"){name, beers(ingredient: \"Hop\"){name, ingredients{ name }}}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .isEmpty(), is(true));
    Map<String, Object> data = result.getData();

    assertThat(data,
        IsMapContaining.hasEntry(BREWERIES_FIELD,
            ImmutableList.of(ImmutableMap.of(BREWERY_NAME_FIELD, "Alfa Brouwerij", BEERS_FIELD,
                ImmutableList.of(ImmutableMap.of(BEER_NAME_FIELD, "Alfa Edel Pils", INGREDIENTS_FIELD,
                    ImmutableList.of(ImmutableMap.of(INGREDIENTS_NAME_FIELD, "Hop"))))))));
  }

  @Test
  void graphqlQuery_ReturnsMap_WithFiltersOnShOrField() {
    // Arrange
    String query = "{breweries(name: \"Alfa Brouwerij\"){name, beers(ingredient: [\"Hop\", \"Gerst\"], supplement: "
        + "\"Gist\"){name, ingredients{ name }, supplements{ name }}}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors()
        .isEmpty(), is(true));
    Map<String, Object> data = result.getData();

    assertThat(data,
        IsMapContaining.hasEntry(BREWERIES_FIELD,
            ImmutableList.of(ImmutableMap.of(BREWERY_NAME_FIELD, "Alfa Brouwerij", BEERS_FIELD,
                ImmutableList.of(ImmutableMap.of(BEER_NAME_FIELD, "Alfa Edel Pils", INGREDIENTS_FIELD,
                    ImmutableList.of(ImmutableMap.of(INGREDIENTS_NAME_FIELD, "Hop"),
                        ImmutableMap.of(INGREDIENTS_NAME_FIELD, "Gerst")),
                    SUPPLEMENTS_FIELD, ImmutableList.of(ImmutableMap.of(SUPPLEMENTS_NAME_FIELD, "Gist"))))))));
  }
}
