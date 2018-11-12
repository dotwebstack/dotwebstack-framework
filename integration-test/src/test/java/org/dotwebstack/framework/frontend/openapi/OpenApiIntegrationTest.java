package org.dotwebstack.framework.frontend.openapi;

import static io.swagger.v3.oas.models.PathItem.HttpMethod.GET;
import static io.swagger.v3.oas.models.PathItem.HttpMethod.HEAD;
import static io.swagger.v3.oas.models.PathItem.HttpMethod.OPTIONS;
import static org.dotwebstack.framework.test.DBEERPEDIA.NUMBER_OF_FTE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.ORIGIN;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

  private static final String BASEPATH = "/dbp/api/v1/";
  private WebTarget target;

  @LocalServerPort
  private int port;

  @Autowired
  private HttpConfiguration httpConfiguration;
  private String apiBasePath;

  @Before
  public void setUp() {
    String host = String.format("http://localhost:%d", this.port);
    target = ClientBuilder.newClient(httpConfiguration).target(host);
    apiBasePath = host + BASEPATH;
    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    SparqlHttpStub.start();
  }

  @Test
  public void get_GetSpec_ThroughOpenApi() throws Exception {
    // Arrange

    // Act
    Response response = target //
        .path(BASEPATH) //
        .request() //
        .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString()) //
        .get();

    // Assert
    assertThat(response.getHeaderString("Content-Type"), equalTo("text/yaml"));
    String responseYaml = response.readEntity(String.class);
    // the response should be valid Yaml
    YAMLMapper mapper = new YAMLMapper();
    mapper.readTree(responseYaml);
    // the x-dotwebstack should have been removed
    assertFalse("x- header found in: " + responseYaml, responseYaml.contains("x-"));
    assertTrue("Swagger spec definition not found in: " + responseYaml,
        responseYaml.contains("openapi: \"3.0.0\""));
    assertTrue("DBeerPedia not found in: " + responseYaml, responseYaml.contains("DBeerPedia API"));
  }


  @Test
  public void get_GetBreweryCollection_ThroughOpenApi() throws JSONException {
    // Arrange
    SparqlHttpStub.returnTuple(
        new TupleQueryResultBuilder("naam", "sinds", "fte", "oprichting", "plaats")//
            .resultSet(//
                DBEERPEDIA.BROUWTOREN_NAME, DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION,
                DBEERPEDIA.BROUWTOREN_FTE, DBEERPEDIA.BROUWTOREN_DATE_OF_FOUNDATION,
                DBEERPEDIA.BROUWTOREN_PLACE)//
            .resultSet(//
                DBEERPEDIA.MAXIMUS_NAME, DBEERPEDIA.MAXIMUS_YEAR_OF_FOUNDATION,
                DBEERPEDIA.MAXIMUS_FTE, DBEERPEDIA.MAXIMUS_DATE_OF_FOUNDATION,
                DBEERPEDIA.MAXIMUS_PLACE));

    // Act
    Response response = target.path(BASEPATH + "breweries").queryParam("fte", NUMBER_OF_FTE)//
        .request()//
        .accept(MediaType.APPLICATION_JSON_TYPE)//
        .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())//
        .get();//

    // Assert
    assertResponseOkAndJson(response);

    String expected = new JSONArray()//
        .put(new JSONObject()//
            .put("naam", DBEERPEDIA.BROUWTOREN_NAME.stringValue())//
            .put("sinds", DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION.integerValue())//
            .put("fte", DBEERPEDIA.BROUWTOREN_FTE.doubleValue())//
            .put("oprichting", DBEERPEDIA.BROUWTOREN_DATE_OF_FOUNDATION.stringValue())//
            .put("plaats", DBEERPEDIA.BROUWTOREN_PLACE.stringValue()))//
        .put(new JSONObject()//
            .put("naam", DBEERPEDIA.MAXIMUS_NAME.stringValue())//
            .put("sinds", DBEERPEDIA.MAXIMUS_YEAR_OF_FOUNDATION.integerValue())//
            .put("fte", DBEERPEDIA.MAXIMUS_FTE.doubleValue())//
            .put("oprichting", DBEERPEDIA.MAXIMUS_DATE_OF_FOUNDATION.stringValue())//
            .put("plaats", DBEERPEDIA.MAXIMUS_PLACE.stringValue()))//
        .toString();//

    JSONAssert.assertEquals(expected, response.readEntity(String.class), true);
  }

  @Test
  public void get_GetSingleBreweryWithId_ThroughOpenApi() throws JSONException {
    // Arrange
    SparqlHttpStub.returnTuple(
        new TupleQueryResultBuilder("naam", "sinds", "fte", "oprichting", "plaats")//
            .resultSet(
                DBEERPEDIA.MAXIMUS_NAME, DBEERPEDIA.MAXIMUS_YEAR_OF_FOUNDATION,
                DBEERPEDIA.MAXIMUS_FTE, DBEERPEDIA.MAXIMUS_DATE_OF_FOUNDATION,
                DBEERPEDIA.MAXIMUS_PLACE));

    // Act
    Response response = target.path(BASEPATH + "breweries/" + DBEERPEDIA.MAXIMUS.getLocalName())//
        .request()//
        .accept(MediaType.APPLICATION_JSON_TYPE)//
        .get();

    // Assert
    assertResponseOkAndJson(response);

    String expected = new JSONObject()//
        .put("naam", DBEERPEDIA.MAXIMUS_NAME.stringValue())//
        .put("sinds", DBEERPEDIA.MAXIMUS_YEAR_OF_FOUNDATION.integerValue())//
        .put("fte", DBEERPEDIA.MAXIMUS_FTE.doubleValue())//
        .put("oprichting", DBEERPEDIA.MAXIMUS_DATE_OF_FOUNDATION.stringValue())//
        .put("plaats", DBEERPEDIA.MAXIMUS_PLACE.stringValue())//
        .toString();

    JSONAssert.assertEquals(expected, response.readEntity(String.class), true);
  }

  @Test
  public void get_ValidationFails_WhenWrongParameterType() {
    // Act
    Response response = target.path(BASEPATH + "breweries")//
        .queryParam("fte", "foo")//
        .request()//
        .accept(MediaType.APPLICATION_JSON_TYPE)//
        .get();//

    // Assert
    assertThat(response.getStatus(), equalTo(Status.BAD_REQUEST.getStatusCode()));
  }

  @Test
  public void get_GetAllowedMethods_ForOptionsRequest() {
    // Act
    Response response = target.path(BASEPATH + "breweries").request().options();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));

    assertThat(response.getHeaders(), hasKey(ACCESS_CONTROL_ALLOW_ORIGIN));
    assertThat(response.getHeaders().getFirst(ACCESS_CONTROL_ALLOW_ORIGIN), equalTo("*"));

    String allowedMethods = response.getHeaders().getFirst(ACCESS_CONTROL_ALLOW_METHODS).toString();
    assertThat(Splitter.on(",").splitToList(allowedMethods),
        containsInAnyOrder(GET.toString(), HEAD.toString(), OPTIONS.toString()));
  }

  @Test
  public void get_GetCorsPolicy_ForOptionsRequestWithOriginAndRequestMethod() {
    // Act
    Response response = target.path(BASEPATH + "breweries").request()//
        .header(ORIGIN, "http://foo")//
        .header(ACCESS_CONTROL_REQUEST_METHOD, "GET")//
        .options();//

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));

    assertThat(response.getHeaders(), hasKey(ACCESS_CONTROL_ALLOW_ORIGIN));
    assertThat(response.getHeaders().getFirst(ACCESS_CONTROL_ALLOW_ORIGIN), equalTo("*"));

    String allowedMethods = response.getHeaders().getFirst(ACCESS_CONTROL_ALLOW_METHODS).toString();
    assertThat(Splitter.on(",").splitToList(allowedMethods),
        containsInAnyOrder(GET.toString(), HEAD.toString(), OPTIONS.toString()));
  }

  @Test
  public void get_GetCorrectHead_ThroughOpenApi() {
    // Arrange
    TupleQueryResultBuilder builder =
        new TupleQueryResultBuilder("naam", "sinds", "fte", "oprichting", "plaats").resultSet(
            DBEERPEDIA.MAXIMUS_NAME,
            DBEERPEDIA.MAXIMUS_YEAR_OF_FOUNDATION,
            DBEERPEDIA.MAXIMUS_FTE,
            DBEERPEDIA.MAXIMUS_DATE_OF_FOUNDATION,
            DBEERPEDIA.MAXIMUS_PLACE);
    SparqlHttpStub.returnTuple(builder);

    // Act
    Response response = target.path(BASEPATH + "breweries").queryParam("fte", NUMBER_OF_FTE)//
        .request()//
        .accept(MediaType.APPLICATION_JSON_TYPE)//
        .head();

    // Assert
    assertResponseOkAndJson(response);
  }

  @Test
  public void get_ResourceNotFound_WhenResourceIsNotDefined() {
    // Act
    Response response = target.path(BASEPATH + "foo").request().get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  public void get_MethodNotAllowed_WhenDelete() {
    // Act
    Response response = target.path(BASEPATH + "breweries").request().delete();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.METHOD_NOT_ALLOWED.getStatusCode()));
  }

  @Test
  public void get_NotAcceptable_WhenRequestingWrongMediaType() {
    // Act
    Response response = target //
        .path(BASEPATH + "breweries") //
        .request(MediaType.APPLICATION_OCTET_STREAM) //
        .get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.NOT_ACCEPTABLE.getStatusCode()));
  }

  @Test
  public void get_GraphGetBreweryCollection_ThroughOpenApi() throws JSONException {
    // Arrange
    Model model = new ModelBuilder() //
        .subject(DBEERPEDIA.BROUWTOREN) //
        .add(RDF.TYPE, DBEERPEDIA.BREWERY_TYPE) //
        .add(DBEERPEDIA.NAME, DBEERPEDIA.BROUWTOREN_NAME) //
        .add(DBEERPEDIA.FTE, DBEERPEDIA.BROUWTOREN_FTE) //
        .add(DBEERPEDIA.FOUNDATION, DBEERPEDIA.BROUWTOREN_DATE_OF_FOUNDATION) //
        .add(DBEERPEDIA.PLACE, DBEERPEDIA.BROUWTOREN_PLACE) //
        .add(DBEERPEDIA.FTE, DBEERPEDIA.BROUWTOREN_FTE) //
        .add(DBEERPEDIA.SINCE, DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION) //
        .subject(DBEERPEDIA.MAXIMUS) //
        .add(RDF.TYPE, DBEERPEDIA.BREWERY_TYPE) //
        .add(DBEERPEDIA.NAME, DBEERPEDIA.MAXIMUS_NAME) //
        .add(DBEERPEDIA.FTE, DBEERPEDIA.MAXIMUS_FTE) //
        .add(DBEERPEDIA.FOUNDATION, DBEERPEDIA.MAXIMUS_DATE_OF_FOUNDATION) //
        .add(DBEERPEDIA.PLACE, DBEERPEDIA.MAXIMUS_PLACE) //
        .add(DBEERPEDIA.SINCE, DBEERPEDIA.MAXIMUS_YEAR_OF_FOUNDATION) //
        .build();

    SparqlHttpStub.returnGraph(model);
    String endpoint = "graph-breweries";

    // Act
    Response response = target //
        .path(BASEPATH + endpoint) //
        .queryParam("page", 2) //
        .request() //
        .accept(MediaType.APPLICATION_JSON_TYPE) //
        .get();

    // Assert
    assertResponseOkAndJson(response);

    String expected = new JSONObject(//
        ImmutableMap.of(//
            "_embedded", ImmutableMap.of(
                "breweries", new JSONArray().put(//
                    new JSONObject() //
                        .put("naam", DBEERPEDIA.BROUWTOREN_NAME.stringValue()) //
                        .put("sinds", DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION.intValue()) //
                        .put("fte", DBEERPEDIA.BROUWTOREN_FTE.doubleValue()) //
                        .put("oprichting", DBEERPEDIA.BROUWTOREN_DATE_OF_FOUNDATION.stringValue())//
                        .put("plaats", DBEERPEDIA.BROUWTOREN_PLACE.stringValue())) //
                    .put(new JSONObject()//
                        .put("naam", DBEERPEDIA.MAXIMUS_NAME.stringValue())//
                        .put("sinds", DBEERPEDIA.MAXIMUS_YEAR_OF_FOUNDATION.intValue())//
                        .put("fte", DBEERPEDIA.MAXIMUS_FTE.doubleValue())//
                        .put("oprichting", DBEERPEDIA.MAXIMUS_DATE_OF_FOUNDATION.stringValue())//
                        .put("plaats", DBEERPEDIA.MAXIMUS_PLACE.stringValue()))), //
            "_links", ImmutableMap.of(//
                "self", ImmutableMap.of("href", apiBasePath + endpoint + "?page=2"), //
                "prev", ImmutableMap.of("href", apiBasePath + endpoint + "?page=1"))))//
                    .toString();

    JSONAssert.assertEquals(expected, response.readEntity(String.class), true);
  }

  @Test
  public void get_GraphGetBreweryCollection_ThroughOpenApi_Returns404ForEmptyQueryResult() {
    // Arrange
    Model model = new ModelBuilder().build();
    SparqlHttpStub.returnGraph(model);

    // Act
    Response response = target.path(BASEPATH + "graph-breweries") //
        .request() //
        .accept(MediaType.APPLICATION_JSON_TYPE) //
        .get(); //

    // Assert
    assertThat(response.getStatus(), equalTo(Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  public void get_GraphGetSingleBrewery_ThroughOpenApi() throws JSONException {
    // Arrange
    Model model = new ModelBuilder() //
        .subject(DBEERPEDIA.BROUWTOREN) //
        .add(RDF.TYPE, DBEERPEDIA.BREWERY_TYPE) //
        .add(DBEERPEDIA.NAME, DBEERPEDIA.BROUWTOREN_NAME) //
        .add(DBEERPEDIA.FTE, DBEERPEDIA.BROUWTOREN_FTE) //
        .add(DBEERPEDIA.FOUNDATION, DBEERPEDIA.BROUWTOREN_DATE_OF_FOUNDATION) //
        .add(DBEERPEDIA.FIRSTBEER, DBEERPEDIA.BROUWTOREN_DATETIME_OF_FIRST_BEER) //
        .add(DBEERPEDIA.PLACE, DBEERPEDIA.BROUWTOREN_PLACE) //
        .add(DBEERPEDIA.FTE, DBEERPEDIA.BROUWTOREN_FTE) //
        .add(DBEERPEDIA.SINCE, DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION) //
        .build();

    SparqlHttpStub.returnGraph(model);

    // Act
    String endpoint = "graph-breweries/";
    String id = "900e5c1c-d292-48c8-b9bd-1baf02ee2d2c";
    Response response = target.path(BASEPATH + endpoint + id) //
        .request() //
        .accept(MediaType.APPLICATION_JSON_TYPE) //
        .get(); //

    // Assert
    assertResponseOkAndJson(response);

    ZonedDateTime dateTime =
        ZonedDateTime.parse(DBEERPEDIA.BROUWTOREN_DATETIME_OF_FIRST_BEER.stringValue());
    String firstBeerDateTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime);

    String expected = new JSONObject(ImmutableMap.<String, Object>builder() //
        .put("naam", DBEERPEDIA.BROUWTOREN_NAME.stringValue()) //
        .put("sinds", DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION.intValue()) //
        .put("fte", DBEERPEDIA.BROUWTOREN_FTE.doubleValue()) //
        .put("oprichting", DBEERPEDIA.BROUWTOREN_DATE_OF_FOUNDATION.stringValue()) //
        .put("plaats", DBEERPEDIA.BROUWTOREN_PLACE.stringValue()) //
        .put("eersteBier", firstBeerDateTime) //
        .put("_links", ImmutableMap.of("self", //
            ImmutableMap.of("href", apiBasePath + endpoint + id))) //
        .build()) //
            .toString();

    JSONAssert.assertEquals(expected, response.readEntity(String.class), true);
  }

  private void assertResponseOkAndJson(Response response) {
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.APPLICATION_JSON_TYPE));
  }
}
