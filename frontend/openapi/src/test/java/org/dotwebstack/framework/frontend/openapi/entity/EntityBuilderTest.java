package org.dotwebstack.framework.frontend.openapi.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import io.swagger.parser.SwaggerParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.InternalServerErrorException;
import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.frontend.http.jackson.ObjectMapperProvider;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilder;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilderContext;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.dotwebstack.framework.frontend.openapi.entity.properties.ArrayPropertyHandler;
import org.dotwebstack.framework.frontend.openapi.entity.properties.ObjectPropertyHandler;
import org.dotwebstack.framework.frontend.openapi.entity.properties.PropertyHandler;
import org.dotwebstack.framework.frontend.openapi.entity.properties.PropertyHandlerRegistry;
import org.dotwebstack.framework.frontend.openapi.entity.properties.StringPropertyHandler;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class EntityBuilderTest {

  private static final String BASE_URI = "http://someUri/path";

  private static final IRI IRI_JOHN_DOE =
      SimpleValueFactory.getInstance().createIRI("http://example.com#johndoe");
  private static final IRI IRI_JANE_DOE =
      SimpleValueFactory.getInstance().createIRI("http://example.com#janedoe");
  private static final IRI IRI_PETER_PAN =
      SimpleValueFactory.getInstance().createIRI("http://example.com#peterpan");

  private static Model MODEL;

  static {
    try {
      MODEL = Rio.parse(EntityBuilderTest.class.getResourceAsStream("personsModel.ttl"), "",
          RDFFormat.TURTLE);
    } catch (IOException ex) {
      MODEL = null;
    }
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private EntityBuilder entityBuilder;
  private PropertyHandlerRegistry registry;
  private Swagger swaggerValidConfig;
  private QueryResult queryResultZeroItems;
  private QueryResult queryResultThreeItems;
  private QueryResult queryResultOneItem;

  @Before
  public void setUp() {

    initializePropertyHandlerRegistry();
    entityBuilder = new EntityBuilder();

    swaggerValidConfig =
        new SwaggerParser().read("/org/dotwebstack/framework/frontend/openapi/entity/personsOasspec.yml");

    queryResultZeroItems = new QueryResult.Builder().withModel(MODEL, ImmutableList.of()).build();

    queryResultOneItem =
        new QueryResult.Builder().withModel(MODEL, ImmutableList.of(IRI_JOHN_DOE)).build();

    queryResultThreeItems = new QueryResult.Builder().withModel(MODEL,
        ImmutableList.of(IRI_JOHN_DOE, IRI_JANE_DOE, IRI_PETER_PAN)).build();
  }

  private void initializePropertyHandlerRegistry() {
    registry = new PropertyHandlerRegistry();

    List<PropertyHandler<? extends Property>> propertyHandlers = new ArrayList<>();

    propertyHandlers.add(new StringPropertyHandler());
    //propertyHandlers.add(new RefPropertyHandler());
    propertyHandlers.add(new ObjectPropertyHandler());
    propertyHandlers.add(new ArrayPropertyHandler());

    registry.setPropertyHandlers(propertyHandlers);
  }

  @Test
  public void resource() {

    String path = "/persons/{name}";
    EntityBuilderContext builderContext =
        new EntityBuilderContext.Builder(path).baseUri(BASE_URI).swagger(swaggerValidConfig).queryResult(
            queryResultOneItem).build();
    Map<String, Object> resource =
        entityBuilder.build(getSchemaPropertyForPath(path), registry, builderContext);

    assertThat(resource.get("firstName"), is(Optional.of("John")));
    assertThat(resource.get("lastName"), is(Optional.of("Doe")));

  }

  @SuppressWarnings("unchecked")
  private Map<String, Optional<Object>> getSelfLink(Map<String, Object> resource) {
    Map<String, Object> links = ((Optional<Map<String, Object>>) resource.get("_links")).get();
    return ((Optional<Map<String, Optional<Object>>>) links.get("self")).get();
  }

  @Ignore
  @Test
  @SuppressWarnings("unchecked")
  public void collectionWithZeroResults() {

    String path = "/persons";
    EntityBuilderContext builderContext =
        new EntityBuilderContext.Builder(path).baseUri(BASE_URI).swagger(swaggerValidConfig).queryResult(
            queryResultZeroItems).build();
    Map<String, Object> resource =
        entityBuilder.build(getSchemaPropertyForPath(path), registry, builderContext);

    Optional<List<Object>> persons =
        ((Optional<Map<String, Optional<List<Object>>>>) resource.get("_embedded")).get().get(
            "persons");

    assertThat(persons.isPresent(), is(true));
    assertThat(persons.get().size(), is(0));
  }

  @Ignore
  @Test
  @SuppressWarnings("unchecked")
  public void collectionWithSingleResult() {

    String path = "/persons";
    EntityBuilderContext builderContext =
        new EntityBuilderContext.Builder(path).baseUri(BASE_URI).swagger(swaggerValidConfig).queryResult(
            queryResultOneItem).build();
    Map<String, Object> resource =
        entityBuilder.build(getSchemaPropertyForPath(path), registry, builderContext);

    List<Map<String, Object>> persons =
        ((Optional<Map<String, Optional<List<Map<String, Object>>>>>) resource.get(
            "_embedded")).get().get("persons").get();

    assertThat(persons, notNullValue());
    assertThat(persons.size(), is(1));

    assertThat(persons.get(0).get("firstName"), is(Optional.of("John")));
    assertThat(persons.get(0).get("lastName"), is(Optional.of("Doe")));
    assertThat(getSelfLink(persons.get(0)).get("href"),
        is(Optional.of(BASE_URI + "/persons/John Doe")));
  }

  @Ignore
  @Test
  @SuppressWarnings("unchecked")
  public void collectionWithMultipleResults() {

    String path = "/persons";
    EntityBuilderContext builderContext =
        new EntityBuilderContext.Builder(path).baseUri(BASE_URI).swagger(swaggerValidConfig).queryResult(
            queryResultThreeItems).build();
    Map<String, Object> resource =
        entityBuilder.build(getSchemaPropertyForPath(path), registry, builderContext);

    List<Map<String, Object>> persons =
        ((Optional<Map<String, Optional<List<Map<String, Object>>>>>) resource.get(
            "_embedded")).get().get("persons").get();

    assertThat(persons, notNullValue());
    assertThat(persons.size(), is(3));

    assertThat(persons.get(0).get("firstName"), is(Optional.of("John")));
    assertThat(persons.get(0).get("lastName"), is(Optional.of("Doe")));
    assertThat(getSelfLink(persons.get(0)).get("href"),
        is(Optional.of(BASE_URI + "/persons/John Doe")));

    assertThat(persons.get(1).get("firstName"), is(Optional.of("Jane")));
    assertThat(persons.get(1).get("lastName"), is(Optional.of("Doe")));
    assertThat(getSelfLink(persons.get(1)).get("href"),
        is(Optional.of(BASE_URI + "/persons/Jane Doe")));

    assertThat(persons.get(2).get("firstName"), is(Optional.of("Peter")));
    assertThat(persons.get(2).get("lastName"), is(Optional.of("Pan")));
    assertThat(getSelfLink(persons.get(2)).get("href"),
        is(Optional.of(BASE_URI + "/persons/Peter Pan")));
  }


  @Ignore
  @Test
  public void resourceWithMultipeResultsThrowsException() {

    expectedException.expect(InternalServerErrorException.class);
    expectedException.expectMessage(
        "There has to be exactly one query result for instance resources.");

    String path = "/persons/{name}";
    EntityBuilderContext builderContext =
        new EntityBuilderContext.Builder(path).baseUri(BASE_URI).swagger(swaggerValidConfig).queryResult(
            queryResultThreeItems).build();
    entityBuilder.build(getSchemaPropertyForPath(path), registry, builderContext);
  }



  private Property getSchemaPropertyForPath(String path) {
    return swaggerValidConfig.getPath(path).getGet().getResponses().get("200").getSchema();
  }

  @Ignore
  @Test
  public void collectionEndpoint() throws IOException, JSONException {

    String path = "/persons";
    EntityBuilderContext builderContext =
        new EntityBuilderContext.Builder(path).baseUri(BASE_URI).swagger(swaggerValidConfig).queryResult(
            queryResultThreeItems).build();
    Map<String, Object> resource =
        entityBuilder.build(getSchemaPropertyForPath(path), registry, builderContext);

    ObjectMapperProvider objectMapperProvider = new ObjectMapperProvider();
    ObjectMapper objectMapper = objectMapperProvider.getContext(null);

    String actualJson = objectMapper.writeValueAsString(resource);
    String expectedJson =
        IOUtils.toString(getClass().getResourceAsStream("collectionEndpoint.json"));

    JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.STRICT);
  }

  @Test
  public void resourceEndpoint() throws IOException, JSONException {

    String path = "/persons/{name}";
    EntityBuilderContext builderContext =
        new EntityBuilderContext.Builder(path).baseUri(BASE_URI).swagger(swaggerValidConfig).queryResult(
            queryResultOneItem).build();
    Map<String, Object> resource =
        entityBuilder.build(getSchemaPropertyForPath(path), registry, builderContext);

    ObjectMapperProvider objectMapperProvider = new ObjectMapperProvider();
    ObjectMapper objectMapper = objectMapperProvider.getContext(null);

    String actualJson = objectMapper.writeValueAsString(resource);
    String expectedJson = IOUtils.toString(getClass().getResourceAsStream("resourceEndpoint.json"));

    JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.STRICT);
  }

//  @Test
//  public void testEmptySwaggerDefinitions() {
//    EntityBuilderContext.Builder builder = new EntityBuilderContext.Builder("");
//
//    QueryResult queryResult = QueryResult.builder().withModel(MODEL,ImmutableList.of()).build();
//    EntityBuilderContext ctx =
//        builder.swagger(new Swagger()).queryResult(queryResult).baseUri("").build();
//    assertThat("Swagger definitions are not empty", ctx.getSwagger().size(), is(0));
//  }
}
