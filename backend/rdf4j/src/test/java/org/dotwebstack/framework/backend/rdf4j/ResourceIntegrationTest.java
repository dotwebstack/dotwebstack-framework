package org.dotwebstack.framework.backend.rdf4j;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.test.TestApplication;
import org.eclipse.rdf4j.model.IRI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
class ResourceIntegrationTest {

  @Autowired
  private GraphQL graphQL;

  @Test
  void graphQlQuery_ReturnBreweriesSorted_WithSubject() {
    // Arrange
     String query = "{ breweries(sort: [{field: \"subject\", order:DESC}]) { identifier, subject, "
      + "beers{ identifier, subject, ingredients { subject } }}}";

    // Act
    ExecutionResult result = graphQL.execute(query);
    System.out.println(result);

    // Assert
    assertThat(result.getErrors(), hasSize(0));
    Map<String, List<Map<String, IRI>>> data = result.getData();
    List<String> subjects = data.get("breweries")
        .stream()
        .map(map -> map.get("subject"))
        .map(IRI::stringValue)
        .collect(Collectors.toList());
    assertThat(subjects, hasSize(5));
    assertThat(subjects,
        allOf(hasItem("https://github.com/dotwebstack/beer/id/brewery/1"),
            hasItem("https://github.com/dotwebstack/beer/id/brewery/123"),
            hasItem("https://github.com/dotwebstack/beer/id/brewery/2"),
            hasItem("https://github.com/dotwebstack/beer/id/brewery/456"),
            hasItem("https://github.com/dotwebstack/beer/id/brewery/789")));
  }

  @Test
  void graphQlQuery_ReturnBreweriesSorted_WithIngredientTestSubject() {
    // Arrange
     String query = "{ breweries(sort: [{field: \"subject\", order:DESC}]) { identifier, subject, beers{ identifier, subject, brewery, ingredients { subject , test { subject } } }}}";

    // Act
    ExecutionResult result = graphQL.execute(query);
    System.out.println(result);

    // Assert
    assertThat(result.getErrors(), hasSize(0));
    Map<String, List<Map<String, IRI>>> data = result.getData();
    List<String> subjects = data.get("breweries")
        .stream()
        .map(map -> map.get("subject"))
        .map(IRI::stringValue)
        .collect(Collectors.toList());
    assertThat(subjects, hasSize(5));
    assertThat(subjects,
        allOf(hasItem("https://github.com/dotwebstack/beer/id/brewery/1"),
            hasItem("https://github.com/dotwebstack/beer/id/brewery/123"),
            hasItem("https://github.com/dotwebstack/beer/id/brewery/2"),
            hasItem("https://github.com/dotwebstack/beer/id/brewery/456"),
            hasItem("https://github.com/dotwebstack/beer/id/brewery/789")));
  }

  @Test
  void graphQlQuery_ReturnBreweriesSortedOnBeers_WithSubject() {
    // Arrange
    String query =
        "{ breweries(sort: [{field: \"beers.subject\", order:DESC}]) { identifier, subject, beers{ identifier, subject }}}";

    // Act
    ExecutionResult result = graphQL.execute(query);
    System.out.println(result);

    // Assert
    assertThat(result.getErrors(), hasSize(0));
    Map<String, List<Map<String, IRI>>> data = result.getData();
    List<String> subjects = data.get("breweries")
        .stream()
        .map(map -> map.get("subject"))
        .map(IRI::stringValue)
        .collect(Collectors.toList());
    assertThat(subjects, hasSize(5));
    assertThat(subjects,
        allOf(hasItem("https://github.com/dotwebstack/beer/id/brewery/1"),
            hasItem("https://github.com/dotwebstack/beer/id/brewery/123"),
            hasItem("https://github.com/dotwebstack/beer/id/brewery/2"),
            hasItem("https://github.com/dotwebstack/beer/id/brewery/456"),
            hasItem("https://github.com/dotwebstack/beer/id/brewery/789")));
  }

  @Test
  void graphQlQuery_ReturnBreweriesFiltered_OnSubject() {
    // Arrange
    String query =
        "{ filterBreweriesSubject(subject: \"https://github.com/dotwebstack/beer/id/brewery/789\" ) { subject } }";

    // Act
    ExecutionResult result = graphQL.execute(query);
    System.out.println(result);

    // Assert
    assertThat(result.getErrors(), hasSize(0));
    Map<String, Map<String, IRI>> data = result.getData();
    String subject = data.get("filterBreweriesSubject").get("subject").stringValue();
    assertThat(subject, is("https://github.com/dotwebstack/beer/id/brewery/789"));
  }

  @Configuration
  public static class TestTypeDefinitionRegistry {

    @Profile("test")
    @Bean
    public TypeDefinitionRegistry testTypeDefinitionRegistry(@NonNull ResourceLoader resourceLoader) throws IOException {
      System.out.println("scheme1.graphqls");
      Reader reader = new InputStreamReader(resourceLoader.getResource(URI.create("classpath:/config/")
          .resolve("resourceSchema.graphqls")
          .toString())
          .getInputStream());

      return new SchemaParser().parse(reader);
    }
  }
}

