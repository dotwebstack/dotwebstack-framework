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
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_SUBJECT_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_SUBJECT_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.INGREDIENTS_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.INGREDIENTS_NAME_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.SCHEMA_NAME;
import static org.dotwebstack.framework.backend.rdf4j.Constants.SUPPLEMENTS_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.SUPPLEMENTS_NAME_FIELD;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import graphql.ExecutionResult;
import graphql.GraphQL;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.helpers.ObjectHelper;
import org.dotwebstack.framework.test.TestApplication;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SuppressWarnings("unchecked")
@SpringBootTest(classes = TestApplication.class)
class Rdf4jIntegrationTest {

  @Autowired
  private GraphQL graphQL;

  @Test
  void graphqlQuery_ReturnsMap_ForObjectQueryField() {
    // Arrange
    String query = "{ brewery(identifier: \"123\") { identifier, name, subject, founded }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();
    assertThat(data,
        hasEntry(BREWERY_FIELD,
            ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, BREWERY_IDENTIFIER_EXAMPLE_1.stringValue(), BREWERY_NAME_FIELD,
                BREWERY_NAME_EXAMPLE_1.stringValue(), BREWERY_SUBJECT_FIELD, BREWERY_SUBJECT_EXAMPLE_1,
                BREWERY_FOUNDED_FIELD, ZonedDateTime.parse(BREWERY_FOUNDED_EXAMPLE_1.stringValue()))));
  }

  @Test
  void graphqlQuery_ReturnsMap_ForNestedListNonNullQuery() {
    // Arrange
    String query = "{ beer(identifier: \"6\") { identifier, beerTypesRaw { name }  }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();
    assertThat(data,
        hasEntry(BEER_FIELD,
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
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();
    assertThat(data,
        hasEntry(BREWERY_FIELD,
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
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();

    assertThat(data, hasEntry(BREWERIES_FIELD, ImmutableList.of(
        ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "123", BREWERY_NAME_FIELD, BREWERY_NAME_EXAMPLE_1.stringValue()))));
  }

  @Test
  void graphqlQuery_ReturnsResult_forQueryWithNesting() {
    // Arrange
    String query = "{ breweries(name: \"Alfa Brouwerij\"){ beers { ingredients { name }}}}";
    Map<String, Object> nullMap = new HashMap<>();
    nullMap.put(INGREDIENTS_NAME_FIELD, null);

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();

    assertThat(data,
        hasEntry(BREWERIES_FIELD,
            ImmutableList.of(ImmutableMap.of(BEERS_FIELD,
                ImmutableList.of(ImmutableMap.of(INGREDIENTS_FIELD,
                    ImmutableList.of(ImmutableMap.of(INGREDIENTS_NAME_FIELD, "Sinasappel"),
                        ImmutableMap.of(INGREDIENTS_NAME_FIELD, "Hop"),
                        ImmutableMap.of(INGREDIENTS_NAME_FIELD, "Gerst"))))))));
  }


  @Test
  void graphqlQuery_ReturnsMap_ForQueryWithFilterOnDateTimeField() {
    // Arrange
    String query = "{ breweries(foundedAfter: \"2018-05-29T09:30:10+02:00\") { identifier, name }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();

    assertThat(data, hasEntry(BREWERIES_FIELD, ImmutableList.of(
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
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();

    assertThat(data,
        hasEntry(BREWERIES_FIELD,
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
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();

    assertThat(data,
        hasEntry("breweriesWithInputObject",
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
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();

    assertThat(data,
        hasEntry("breweries",
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
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();

    assertThat(data,
        hasEntry("breweries",
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
    assertThat(result.getErrors(), hasSize(1));
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
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();

    assertThat(data, hasEntry("breweries", ImmutableList.of(ImmutableMap.of(BREWERY_NAME_FIELD, "Heineken Nederland"),
        ImmutableMap.of(BREWERY_NAME_FIELD, "Brouwerij Het 58e Genot i.o."),
        ImmutableMap.of(BREWERY_NAME_FIELD, "Brouwerij De Leckere"),
        ImmutableMap.of(BREWERY_NAME_FIELD, "Brouwerij 1923"), ImmutableMap.of(BREWERY_NAME_FIELD, "Alfa Brouwerij"))));

  }

  @Test
  void graphqlQuery_ReturnsMap_ForSortQueryWithNameNestedSorting() {
    // Arrange
    String query = "{ breweries(sort: [{field: \"address.postalCode\", order: DESC}]){ identifier }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();

    assertThat(data,
        hasEntry("breweries",
            ImmutableList.of(ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "456"),
                ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "789"), ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "123"),
                ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "1"), ImmutableMap.of(BREWERY_IDENTIFIER_FIELD, "2"))));
  }

  @Test()
  void graphqlQuery_ReturnsMap_ForSortQueryWithNameMultipleSorting() {
    // Arrange
    String query = "{ breweries(sort: [{field: \"address.postalCode\", order: DESC}, {field: \"name\", order: ASC}]){"
        + " identifier }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();

    assertThat(data,
        hasEntry("breweries",
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
    assertThat(result.getErrors(), hasSize(1));
    assertThat(result.getErrors()
        .get(0)
        .getMessage(),
        is("Exception while fetching data (/breweries) : No property shape found for name 'unexisting' "
            + "nodeshape 'Brewery'"));
  }

  @Test
  void graphqlQuery_ReturnsMap_ForSortQueryWithUnexistingSortOrder() {
    // Arrange
    String query = "{ breweries(sort: [{field: \"unexisting\", order: unexisting}]){ identifier }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertThat(result.getErrors(), hasSize(1));
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
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();

    assertThat(data, hasEntry(BREWERIES_FIELD,
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
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();

    assertThat(data,
        hasEntry(BREWERIES_FIELD,
            ImmutableList.of(ImmutableMap.of(BREWERY_NAME_FIELD, "Alfa Brouwerij", BEERS_FIELD,
                ImmutableList.of(ImmutableMap.of(BEER_NAME_FIELD, "Alfa Edel Pils", INGREDIENTS_FIELD,
                    ImmutableList.of(ImmutableMap.of(INGREDIENTS_NAME_FIELD, "Hop"))))))));
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void graphqlQuery_ReturnsMap_WithFiltersOnShOrField() {
    // Arrange
    String query = "{breweries(name: \"Alfa Brouwerij\"){name, beers(ingredient: [\"Hop\", \"Gerst\"], supplement: "
        + "\"Gist\"){name, ingredients{ name }, supplements{ name }}}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();

    assertThat(data,
        hasEntry(BREWERIES_FIELD,
            ImmutableList.of(ImmutableMap.of(BREWERY_NAME_FIELD, "Alfa Brouwerij", BEERS_FIELD,
                ImmutableList.of(ImmutableMap.of(BEER_NAME_FIELD, "Alfa Edel Pils", INGREDIENTS_FIELD,
                    ImmutableList.of(ImmutableMap.of(INGREDIENTS_NAME_FIELD, "Hop"),
                        ImmutableMap.of(INGREDIENTS_NAME_FIELD, "Gerst")),
                    SUPPLEMENTS_FIELD, ImmutableList.of(ImmutableMap.of(SUPPLEMENTS_NAME_FIELD, "Gist"))))))));

    List breweries = (List<Map<String, Object>>) data.get(BREWERIES_FIELD);
    Map<String, Object> edelPils =
        (Map<String, Object>) ((List<Object>) ((Map<String, Object>) breweries.get(0)).get(BEERS_FIELD)).get(0);
    List<String> ingredients = (List<String>) edelPils.get(INGREDIENTS_FIELD);

    assertThat(ingredients, IsCollectionWithSize.hasSize(2));
  }

  @Test
  void graphQlQuery_ReturnLocalName_WithConfiguredLanguage() {
    // Arrange
    String query = "{breweries(name: \"Heineken Nederland\"){name, localName}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();

    assertThat(data, hasEntry(BREWERIES_FIELD,
        ImmutableList.of(ImmutableMap.of("name", "Heineken Nederland", "localName", "Heineken Niederlande"))));
  }

  @Test
  @SuppressWarnings("unchecked")
  void graphQlQuery_ReturnOwners_WithSortAnnotation() {
    List<String> expected =
        List.of("A. de Bruijn", "I. Verhoef", "J. v. Hees", "J. v. Jansen", "L. du Clou", "M. Kuijpers", "Z. v. Marke");

    // Arrange
    String query = "{breweries(name: \"Heineken Nederland\"){owners}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();
    List<String> owners = (List<String>) ((List<Map<String, Object>>) data.get("breweries")).get(0)
        .get("owners");

    assertThat(owners, is(equalTo(expected)));
  }

  @Test
  @SuppressWarnings("unchecked")
  void graphQlQuery_ReturnIngredients_WithDescSortAnnotation() {
    // Arrange
    String query = "{beer(identifier: 6){ingredients{ name }}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();
    List<String> ingredients =
        ((List<Map<String, String>>) ((Map<String, Object>) data.get("beer")).get("ingredients")).stream()
            .map(entry -> entry.get("name"))
            .collect(Collectors.toList());

    assertThat(ingredients, hasSize(3));
    assertThat(ingredients.get(0), is(equalTo("Sinasappel")));
    assertThat(ingredients.get(1), is(equalTo("Hop")));
    assertThat(ingredients.get(2), is(equalTo("Gerst")));
  }

  @Test
  @SuppressWarnings("unchecked")
  void graphQlQuery_ReturnBreweries_WithNullValuesInAscSort() {
    // Arrange
    String query = "{breweries(sort: [{field: \"number\", order:ASC}]){number}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();
    List<Integer> numbers = ((List<Map<String, Integer>>) data.get("breweries")).stream()
        .map(map -> map.get("number"))
        .collect(Collectors.toList());
    assertThat(numbers, hasSize(5));
    assertThat(numbers.get(0), is(equalTo(null)));
    assertThat(numbers.get(1), is(1));
    assertThat(numbers.get(2), is(2));
    assertThat(numbers.get(3), is(20));
    assertThat(numbers.get(4), is(100));

  }

  @Test
  @SuppressWarnings("unchecked")
  void graphQlQuery_ReturnBreweries_WithNullValuesInDescSort() {
    // Arrange
    String query = "{breweries(sort: [{field: \"number\", order:DESC}]){number}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();
    List<Integer> numbers = ((List<Map<String, Integer>>) data.get("breweries")).stream()
        .map(map -> map.get("number"))
        .collect(Collectors.toList());
    assertThat(numbers, hasSize(5));
    assertThat(numbers.get(0), is(100));
    assertThat(numbers.get(1), is(20));
    assertThat(numbers.get(2), is(2));
    assertThat(numbers.get(3), is(1));
    assertThat(numbers.get(4), is(equalTo(null)));
  }

  private void assertResultHasNoErrors(ExecutionResult result) {
    assertThat(result.getErrors(), is(empty()));
  }


  @Test
  void graphQlQuery_ReturnsBreweries_WithCount() {
    // Arrange
    String query = "{breweries(name: \"Brouwerij 1923\"){beerCount}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();
    Map<String, Object> brewery = ((List<Map<String, Object>>) data.get("breweries")).get(0);
    assertThat(brewery.get("beerCount"), is(2));
  }

  @Test
  void graphQlQuery_ReturnesBreweries_SortedByBeerCountDesc() {
    // Arrange
    String query = "{breweries(sort: [{field: \"beerCount\", order: DESC}]){beerCount}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();
    List<Integer> beerCounts = ((List<Map<String, Integer>>) data.get("breweries")).stream()
        .map(map -> map.get("beerCount"))
        .collect(Collectors.toList());

    assertThat(beerCounts, is(ImmutableList.of(2, 1, 0, 0, 0)));
  }

  @Test
  void graphQlQuery_ReturnesBreweries_SortedByBeerCountAsc() {
    // Arrange
    String query = "{breweries(sort: [{field: \"beerCount\", order: ASC}]){beerCount}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();
    List<Integer> beerCounts = ((List<Map<String, Integer>>) data.get("breweries")).stream()
        .map(map -> map.get("beerCount"))
        .collect(Collectors.toList());

    assertThat(beerCounts, is(ImmutableList.of(0, 0, 0, 1, 2)));
  }

  @Test
  void graphQlQuery_ReturnesBreweries_FilteredByBeerCount2() {
    // Arrange
    String query = "{breweries(beerCount: 2){name, beerCount}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();
    List<String> names = ((List<Map<String, String>>) data.get("breweries")).stream()
        .map(map -> map.get("name"))
        .collect(Collectors.toList());

    assertThat(names, is(ImmutableList.of("Brouwerij 1923")));
  }

  @Test
  void graphQlQuery_ReturnesBreweries_FilteredByBeerCount0() {
    // Arrange
    String query = "{breweries(beerCount: 0){name, beerCount}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();
    List<String> names = ((List<Map<String, String>>) data.get("breweries")).stream()
        .map(map -> map.get("name"))
        .collect(Collectors.toList());

    assertThat(names,
        is(ImmutableList.of("Brouwerij De Leckere", "Brouwerij Het 58e Genot i.o.", "Heineken Nederland")));
  }

  @Test
  void graphQlQuery_ReturnesBreweries_FilteredByBeerCount2MissingEdge() {
    // Arrange
    String query = "{breweries(beerCount: 2){name}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();
    List<String> names = ((List<Map<String, String>>) data.get("breweries")).stream()
        .map(map -> map.get("name"))
        .collect(Collectors.toList());

    assertThat(names, is(ImmutableList.of("Brouwerij 1923")));
  }

  @Test
  void graphQlQuery_ReturnsBreweries_WithTransformedAggregate() {
    // Arrange
    String query = "{breweries(sort: [{field: \"name\", order: ASC}]){name, beerCount, hasBeers}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();
    List<Boolean> hasBeers = ((List<Map<String, Boolean>>) data.get("breweries")).stream()
        .map(map -> map.get("hasBeers"))
        .collect(Collectors.toList());

    assertThat(hasBeers, is(ImmutableList.of(true, true, false, false, false)));
  }

  @Test
  void graphqlQuery_ReturnsBrewery_FilteredBySubject() {
    // Arrange
    String query =
        "{ brewery_with_subject(subject: \"https://github.com/dotwebstack/beer/id/brewery/123\") { subject }}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertResultHasNoErrors(result);
    Map<String, Object> data = result.getData();
    assertThat(data,
        hasEntry("brewery_with_subject", ImmutableMap.of(BREWERY_SUBJECT_FIELD, BREWERY_SUBJECT_EXAMPLE_1)));
  }

  @Test
  void graphQlQuery_ReturnsBreweries_SortedOnSubjectAsc() {
    // Arrange
    String query = "{breweries(sort: [{field: \"subject\", order: ASC}]){subject}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();

    List<String> subjects = ((List<Map<String, String>>) (data.get("breweries"))).stream()
        .map(entry -> entry.get("subject"))
        .collect(Collectors.toList());

    assertThat(subjects, hasSize(5));
    assertThat(subjects.get(0), is(equalTo("https://github.com/dotwebstack/beer/id/brewery/1")));
    assertThat(subjects.get(1), is(equalTo("https://github.com/dotwebstack/beer/id/brewery/123")));
    assertThat(subjects.get(2), is(equalTo("https://github.com/dotwebstack/beer/id/brewery/2")));
    assertThat(subjects.get(3), is(equalTo("https://github.com/dotwebstack/beer/id/brewery/456")));
  }

  @Test
  void graphQlQuery_ReturnsBreweries_SortedOnSubjectDesc() {
    // Arrange
    String query = "{breweries(sort: [{field: \"subject\", order: DESC}]){subject}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();

    List<String> subjects = ((List<Map<String, String>>) (data.get("breweries"))).stream()
        .map(entry -> entry.get("subject"))
        .collect(Collectors.toList());

    assertThat(subjects, hasSize(5));
    assertThat(subjects.get(0), is(equalTo("https://github.com/dotwebstack/beer/id/brewery/789")));
    assertThat(subjects.get(1), is(equalTo("https://github.com/dotwebstack/beer/id/brewery/456")));
    assertThat(subjects.get(2), is(equalTo("https://github.com/dotwebstack/beer/id/brewery/2")));
    assertThat(subjects.get(3), is(equalTo("https://github.com/dotwebstack/beer/id/brewery/123")));
  }

  @Test
  void graphQlQuery_throwsException_SortedOnListBeerSubjectDesc() {
    // Arrange
    String query = "{breweries(sort: [{field: \"beers.subject\", order: DESC}]){beers { subject }}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertFalse(result.getErrors()
        .isEmpty());
    assertEquals(1, result.getErrors()
        .size());
  }

  @Test
  void graphQlQuery_returnsBreweries_SortedOnAddressSubjectDesc() {
    // Arrange
    String query = "{breweries(sort: [{field: \"address.subject\", order: DESC}]){address { subject }}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    Map<String, Object> data = result.getData();
    List<String> subjects = ((List<Object>) data.get("breweries")).stream()
        .map(ObjectHelper::castToMap)
        .map(map -> map.get("address"))
        .filter(Objects::nonNull)
        .map(ObjectHelper::castToMap)
        .map(map -> map.get("subject")
            .toString())
        .collect(Collectors.toList());

    assertTrue(result.getErrors()
        .isEmpty());
    assertEquals(3, subjects.size());
    assertThat(subjects, contains("https://github.com/dotwebstack/beer/id/address/3",
        "https://github.com/dotwebstack/beer/id/address/2", "https://github.com/dotwebstack/beer/id/address/1"));
  }

  @Test
  void graphQlQuery_returnsBreweries_FilterOnAddressSubject() {
    // Arrange
    String query =
        "{breweries{address(subject : \"https://github.com/dotwebstack/beer/id/address/1\") { subject, resource }}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    Map<String, Object> data = result.getData();
    List<String> subjects = ((List<Object>) data.get("breweries")).stream()
        .map(ObjectHelper::castToMap)
        .map(map -> map.get("address"))
        .filter(Objects::nonNull)
        .map(ObjectHelper::castToMap)
        .map(map -> map.get("subject")
            .toString())
        .collect(Collectors.toList());

    assertTrue(result.getErrors()
        .isEmpty());
    assertEquals(1, subjects.size());
    assertThat(subjects, contains("https://github.com/dotwebstack/beer/id/address/1"));
  }
}
