package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.HttpMethod;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.http.entity.ContentType;
import org.dotwebstack.framework.SparqlHttpStub;
import org.dotwebstack.framework.SparqlHttpStub.TupleQueryResultBuilder;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class OpenApiIntegrationTest {

  private WebTarget target;

  @LocalServerPort
  private int port;

  @Autowired
  private HttpConfiguration httpConfiguration;

  @Before
  public void setUp() throws IOException {
    target = ClientBuilder.newClient(httpConfiguration).target(
        String.format("http://localhost:%d", this.port));

    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    SparqlHttpStub.start();
  }

  @Test
  public void get_GetSpec_ThroughOpenApi() throws Exception {
    // Arrange

    // Act
    Response response = target.path("/dbp/api/v1/").request().header(HttpHeaders.CONTENT_TYPE,
        ContentType.APPLICATION_JSON.toString()).get();

    // Assert
    assertThat(response.getHeaderString("Content-Type"), equalTo("text/yaml"));
    String responseYaml = response.readEntity(String.class);
    // the response should be valid Yaml
    YAMLMapper mapper = new YAMLMapper();
    mapper.readTree(responseYaml);
    // the x-dotwebstack should have been removed
    assertFalse("x- header found in: " + responseYaml, responseYaml.contains("x-"));
    assertTrue("Swagger spec definition not found in: " + responseYaml,
        responseYaml.contains("swagger: 2.0"));
    assertTrue("DBeerPedia not found in: " + responseYaml, responseYaml.contains("DBeerPedia API"));
  }


  @Test
  public void get_GetBreweryCollection_ThroughOpenApi() throws JSONException {
    // Arrange
    TupleQueryResultBuilder builder =
        new TupleQueryResultBuilder("naam", "sinds", "fte", "oprichting", "plaats").resultSet(
            DBEERPEDIA.BROUWTOREN_NAME, DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION,
            DBEERPEDIA.BROUWTOREN_FTE, DBEERPEDIA.BROUWTOREN_DATE_OF_FOUNDATION,
            DBEERPEDIA.BROUWTOREN_PLACE).resultSet(DBEERPEDIA.MAXIMUS_NAME,
                DBEERPEDIA.MAXIMUS_YEAR_OF_FOUNDATION, DBEERPEDIA.MAXIMUS_FTE,
                DBEERPEDIA.MAXIMUS_DATE_OF_FOUNDATION, DBEERPEDIA.MAXIMUS_PLACE);
    SparqlHttpStub.returnTuple(builder);

    // Act
    Response response = target.path("/dbp/api/v1/breweries").request().accept(
        MediaType.APPLICATION_JSON_TYPE).header(HttpHeaders.CONTENT_TYPE,
            ContentType.APPLICATION_JSON.toString()).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.APPLICATION_JSON_TYPE));

    JSONArray expected = new JSONArray();
    expected.put(new JSONObject().put("naam", DBEERPEDIA.BROUWTOREN_NAME.stringValue()).put("sinds",
        DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION.integerValue()).put("fte",
            DBEERPEDIA.BROUWTOREN_FTE.doubleValue()).put("oprichting",
                DBEERPEDIA.BROUWTOREN_DATE_OF_FOUNDATION.stringValue()).put("plaats",
                    DBEERPEDIA.BROUWTOREN_PLACE.stringValue()));
    expected.put(new JSONObject().put("naam", DBEERPEDIA.MAXIMUS_NAME.stringValue()).put("sinds",
        DBEERPEDIA.MAXIMUS_YEAR_OF_FOUNDATION.integerValue()).put("fte",
            DBEERPEDIA.MAXIMUS_FTE.doubleValue()).put("oprichting",
                DBEERPEDIA.MAXIMUS_DATE_OF_FOUNDATION.stringValue()).put("plaats",
                    DBEERPEDIA.MAXIMUS_PLACE.stringValue()));

    String result = response.readEntity(String.class);
    JSONAssert.assertEquals(expected.toString(), result, true);
  }

  @Test
  public void get_GetSingleBreweryWithId_ThroughOpenApi() throws JSONException {
    // Arrange
    TupleQueryResultBuilder builder =
        new TupleQueryResultBuilder("naam", "sinds", "fte", "oprichting", "plaats").resultSet(
            DBEERPEDIA.MAXIMUS_NAME, DBEERPEDIA.MAXIMUS_YEAR_OF_FOUNDATION, DBEERPEDIA.MAXIMUS_FTE,
            DBEERPEDIA.MAXIMUS_DATE_OF_FOUNDATION, DBEERPEDIA.MAXIMUS_PLACE);
    SparqlHttpStub.returnTuple(builder);

    // Act
    Response response = target.path(String.format("/dbp/api/v1/breweries/%s",
        DBEERPEDIA.MAXIMUS.getLocalName())).request().accept(MediaType.APPLICATION_JSON_TYPE).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.APPLICATION_JSON_TYPE));

    JSONObject expected =
        new JSONObject().put("naam", DBEERPEDIA.MAXIMUS_NAME.stringValue()).put("sinds",
            DBEERPEDIA.MAXIMUS_YEAR_OF_FOUNDATION.integerValue()).put("fte",
                DBEERPEDIA.MAXIMUS_FTE.doubleValue()).put("oprichting",
                    DBEERPEDIA.MAXIMUS_DATE_OF_FOUNDATION.stringValue()).put("plaats",
                        DBEERPEDIA.MAXIMUS_PLACE.stringValue());

    String result = response.readEntity(String.class);
    JSONAssert.assertEquals(expected.toString(), result, true);
  }

  @Test
  public void get_ValidationFails_WhenWrongParameterType() throws JSONException {
    // Act
    Response response =
        target.path("/dbp/api/v1/breweries").queryParam("fte", "foo").request().accept(
            MediaType.APPLICATION_JSON_TYPE).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void get_GetAllowedMethods_ForOptionsRequest() {
    // Act
    Response response = target.path("/dbp/api/v1/breweries").request().options();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    List<String> allowMethods =
        Splitter.on(",").splitToList(response.getHeaders().getFirst(HttpHeaders.ALLOW).toString());
    assertThat(allowMethods, containsInAnyOrder(HttpMethod.GET.toString(),
        HttpMethod.HEAD.toString(), HttpMethod.OPTIONS.toString()));
    assertThat(response.getHeaders().containsKey(
        org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is(false));
  }

  @Test
  public void get_GetCorsPolicy_ForOptionsRequestWithOriginAndRequestMethod() {
    // Act
    Response response = target.path("/dbp/api/v1/breweries").request().header(
        org.springframework.http.HttpHeaders.ORIGIN, "http://foo").header(
            org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET").options();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(
        response.getHeaders().getFirst(
            org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
        equalTo("http://foo"));
    List<String> allowMethods =
        Splitter.on(",").trimResults().splitToList(response.getHeaders().getFirst(
            org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS).toString());
    assertThat(allowMethods, containsInAnyOrder(HttpMethod.GET.toString(),
        HttpMethod.HEAD.toString(), HttpMethod.OPTIONS.toString()));
  }

  @Test
  public void get_GetCorrectHead_ThroughOpenApi() {
    // Arrange
    TupleQueryResultBuilder builder =
        new TupleQueryResultBuilder("naam", "sinds", "fte", "oprichting", "plaats").resultSet(
            DBEERPEDIA.MAXIMUS_NAME, DBEERPEDIA.MAXIMUS_YEAR_OF_FOUNDATION, DBEERPEDIA.MAXIMUS_FTE,
            DBEERPEDIA.MAXIMUS_DATE_OF_FOUNDATION, DBEERPEDIA.MAXIMUS_PLACE);
    SparqlHttpStub.returnTuple(builder);

    // Act
    Response response = target.path("/dbp/api/v1/breweries").request().accept(
        MediaType.APPLICATION_JSON_TYPE).head();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.APPLICATION_JSON_TYPE));
  }

  @Test
  public void get_ResourceNotFound_WhenResourceIsNotDefined() {
    // Act
    Response response = target.path("/dbp/api/v1/foo").request().get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  public void get_MethodNotAllowed_WhenDelete() {
    // Act
    Response response = target.path("/dbp/api/v1/breweries").request().delete();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.METHOD_NOT_ALLOWED.getStatusCode()));
  }

  @Test
  public void get_NotAcceptable_WhenRequestingWrongMediaType() {
    // Act
    Response response =
        target.path("/dbp/api/v1/breweries").request(MediaType.APPLICATION_OCTET_STREAM).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.NOT_ACCEPTABLE.getStatusCode()));
  }

  @Test
  public void get_GraphGetBreweryCollection_ThroughOpenApi() throws JSONException {
    // Arrange
    // @formatter:off
    Model model = new ModelBuilder()
        .subject(DBEERPEDIA.BROUWTOREN)
          .add(RDF.TYPE,DBEERPEDIA.BREWERY_TYPE)
          .add(DBEERPEDIA.NAME,DBEERPEDIA.BROUWTOREN_NAME)
          .add(DBEERPEDIA.FTE,DBEERPEDIA.BROUWTOREN_FTE)
          .add(DBEERPEDIA.FOUNDATION,DBEERPEDIA.BROUWTOREN_DATE_OF_FOUNDATION)
          .add(DBEERPEDIA.PLACE,DBEERPEDIA.BROUWTOREN_PLACE)
          .add(DBEERPEDIA.FTE,DBEERPEDIA.BROUWTOREN_FTE)
          .add(DBEERPEDIA.SINCE,DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION)
        .subject(DBEERPEDIA.MAXIMUS)
          .add(RDF.TYPE,DBEERPEDIA.BREWERY_TYPE)
          .add(DBEERPEDIA.NAME,DBEERPEDIA.MAXIMUS_NAME)
          .add(DBEERPEDIA.FTE,DBEERPEDIA.MAXIMUS_FTE)
          .add(DBEERPEDIA.FOUNDATION,DBEERPEDIA.MAXIMUS_DATE_OF_FOUNDATION)
          .add(DBEERPEDIA.PLACE,DBEERPEDIA.MAXIMUS_PLACE)
          .add(DBEERPEDIA.SINCE,DBEERPEDIA.MAXIMUS_YEAR_OF_FOUNDATION)
        .build();
    // @formatter:on

    SparqlHttpStub.returnGraph(model);

    // Act
    Response response =
        target.path("/dbp/api/v1/graph-breweries").queryParam("page", 2).request().accept(
            MediaType.APPLICATION_JSON_TYPE).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.APPLICATION_JSON_TYPE));

    JSONArray breweries = new JSONArray();
    breweries.put(new JSONObject().put("naam", DBEERPEDIA.MAXIMUS_NAME.stringValue()).put("sinds",
        DBEERPEDIA.MAXIMUS_YEAR_OF_FOUNDATION.intValue()).put("fte",
            DBEERPEDIA.MAXIMUS_FTE.doubleValue()).put("oprichting",
                DBEERPEDIA.MAXIMUS_DATE_OF_FOUNDATION.stringValue()).put("plaats",
                    DBEERPEDIA.MAXIMUS_PLACE.stringValue()));
    breweries.put(
        new JSONObject().put("naam", DBEERPEDIA.BROUWTOREN_NAME.stringValue()).put("sinds",
            DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION.intValue()).put("fte",
                DBEERPEDIA.BROUWTOREN_FTE.doubleValue()).put("oprichting",
                    DBEERPEDIA.BROUWTOREN_DATE_OF_FOUNDATION.stringValue()).put("plaats",
                        DBEERPEDIA.BROUWTOREN_PLACE.stringValue()));

    JSONObject expected = new JSONObject(ImmutableMap.of("_embedded",
        ImmutableMap.of("breweries", breweries), "_links",
        ImmutableMap.of("self",
            ImmutableMap.of("href",
                String.format("https://localhost:%d/dbp/api/v1/graph-breweries?page=2", port)),
            "prev", ImmutableMap.of("href",
                String.format("https://localhost:%d/dbp/api/v1/graph-breweries?page=1", port)))));

    String result = response.readEntity(String.class);
    JSONAssert.assertEquals(expected.toString(), result, true);
  }

  @Test
  public void get_GraphGetBreweryCollection_ThroughOpenApi_Returns404ForEmptyQueryResult() {
    // Arrange
    Model model = new ModelBuilder().build();
    SparqlHttpStub.returnGraph(model);

    // Act
    Response response = target.path("/dbp/api/v1/graph-breweries").request().accept(
        MediaType.APPLICATION_JSON_TYPE).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  public void get_GraphGetSingleBrewery_ThroughOpenApi() throws JSONException {
    // Arrange
    // @formatter:off
    Model model = new ModelBuilder()
        .subject(DBEERPEDIA.BROUWTOREN)
          .add(RDF.TYPE,DBEERPEDIA.BREWERY_TYPE)
          .add(DBEERPEDIA.NAME,DBEERPEDIA.BROUWTOREN_NAME)
          .add(DBEERPEDIA.FTE,DBEERPEDIA.BROUWTOREN_FTE)
          .add(DBEERPEDIA.FOUNDATION,DBEERPEDIA.BROUWTOREN_DATE_OF_FOUNDATION)
          .add(DBEERPEDIA.FIRSTBEER, DBEERPEDIA.BROUWTOREN_DATETIME_OF_FIRST_BEER)
          .add(DBEERPEDIA.PLACE,DBEERPEDIA.BROUWTOREN_PLACE)
          .add(DBEERPEDIA.FTE,DBEERPEDIA.BROUWTOREN_FTE)
          .add(DBEERPEDIA.SINCE,DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION)
        .build();
    // @formatter:on

    SparqlHttpStub.returnGraph(model);

    // Act
    Response response = target.path(
        "/dbp/api/v1/graph-breweries/900e5c1c-d292-48c8-b9bd-1baf02ee2d2c").request().accept(
            MediaType.APPLICATION_JSON_TYPE).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.APPLICATION_JSON_TYPE));

    ZonedDateTime dateTime =
        ZonedDateTime.parse(DBEERPEDIA.BROUWTOREN_DATETIME_OF_FIRST_BEER.stringValue());
    String firstBeerDateTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime);
    // @formatter:off
    JSONObject expected = new JSONObject(ImmutableMap.<String, Object>builder()
        .put("naam", DBEERPEDIA.BROUWTOREN_NAME.stringValue())
        .put("sinds", DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION.intValue())
        .put("fte", DBEERPEDIA.BROUWTOREN_FTE.doubleValue())
        .put("oprichting", DBEERPEDIA.BROUWTOREN_DATE_OF_FOUNDATION.stringValue())
        .put("plaats", DBEERPEDIA.BROUWTOREN_PLACE.stringValue())
        .put("eersteBier", firstBeerDateTime)
        .put("_links", ImmutableMap.of("self", ImmutableMap.of("href",
            String.format("https://localhost:%d/dbp/api/v1/graph-breweries/900e5c1c-d292-48c8-b9bd-1baf02ee2d2c", port))))
        .build());
    // @formatter:on

    String result = response.readEntity(String.class);
    JSONAssert.assertEquals(expected.toString(), result, true);
  }
}
