package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.dotwebstack.framework.SparqlHttpStub;
import org.dotwebstack.framework.SparqlHttpStub.TupleQueryResultBuilder;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.test.DBEERPEDIA;
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

    SparqlHttpStub.start();
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
    Response response = target.path("/dbp/api/v1/breweries").request().get();

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
        DBEERPEDIA.MAXIMUS.getLocalName())).request().get();

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
  public void get_GetCorrectOptions_ThroughOpenApi() {
    // Act
    Response response =
        target.path("/dbp/api/v1/breweries").request(MediaType.TEXT_PLAIN_TYPE).options();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.TEXT_PLAIN_TYPE));
    assertThat(response.readEntity(String.class), equalTo("HEAD, GET, OPTIONS"));
    assertThat(response.getHeaderString("allow"), equalTo("HEAD,GET,OPTIONS"));
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
    Response response = target.path("/dbp/api/v1/breweries").request().head();

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

}
