package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
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
  }

  @Test
  @Ignore
  public void get_GetBreweryCollection_ThroughOpenApi() {
    // Act
    Response response = target.path("/dbp/api/v1/breweries").request().get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.TEXT_PLAIN_TYPE));
    assertThat(response.getLength(), equalTo(31));
    assertThat(response.readEntity(String.class), equalTo(DBEERPEDIA.BREWERIES.stringValue()));
  }

  @Test
  @Ignore
  public void get_GetSingleBrewery_ThroughLdApi() {
    // Act
    Response response = target.path(String.format("/dbp/api/v1/breweries/%s",
        DBEERPEDIA.BROUWTOREN.getLocalName())).request().get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.TEXT_PLAIN_TYPE));
    assertThat(response.readEntity(String.class), equalTo(DBEERPEDIA.BREWERIES.stringValue()));
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
  @Ignore
  public void get_GetCorrectHead_ThroughOpenApi() {
    // Act
    Response response = target.path("/dbp/api/v1/breweries").request().head();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.TEXT_PLAIN_TYPE));
    assertThat(response.getLength(), equalTo(31));
    assertThat(response.readEntity(String.class), isEmptyString());
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
