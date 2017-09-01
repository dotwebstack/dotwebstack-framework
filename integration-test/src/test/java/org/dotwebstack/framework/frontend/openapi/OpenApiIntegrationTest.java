package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
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

  WebTarget target;

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
  public void getBreweryCollection() {
    // Act
    Response response = target.path("/dbp/api/v1/breweries").request().get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.valueOf("text/turtle")));
    assertThat(response.readEntity(String.class), containsString(DBEERPEDIA.BREWERIES.stringValue()));
  }

  @Test
  public void getBreweryCollectionNotAcceptableForJson() {
    // Act
    Response response = target.path("/dbp/api/v1/breweries").request(MediaType.APPLICATION_JSON).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.NOT_ACCEPTABLE.getStatusCode()));
  }

  @Test
  public void getBreweryCollectionInRdfXml() {
    // Act
    String mediaType = "application/rdf+xml";
    Response response = target.path("/dbp/api/v1/breweries").request(mediaType).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.valueOf(mediaType)));
    String entity = response.readEntity(String.class);
    assertThat(entity, containsString(DBEERPEDIA.BREWERIES.stringValue()));
    assertThat(entity, containsString("?xml"));
  }

  @Test
  public void getBreweryCollectionInLdJson() {
    // Act
    String mediaType = "application/ld+json";
    Response response = target.path("/dbp/api/v1/breweries").request(mediaType).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.valueOf(mediaType)));
    String entity = response.readEntity(String.class);
    assertThat(entity, containsString(DBEERPEDIA.BREWERIES.stringValue()));
    assertThat(entity, containsString("[{"));
  }

  @Test
  public void getBreweryCollectionInTrig() {
    // Act
    String mediaType = "application/trig";;
    Response response = target.path("/dbp/api/v1/breweries").request(mediaType).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.valueOf(mediaType)));
    String entity = response.readEntity(String.class);
    assertThat(entity, containsString(DBEERPEDIA.BREWERIES.stringValue()));
    assertThat(entity, containsString("<" + DBEERPEDIA.BREWERIES.stringValue() + ">"));
  }

  @Test
  public void getBreweryCollectionInTurtle() {
    // Act
    String mediaType = "text/turtle";
    Response response = target.path("/dbp/api/v1/breweries").request(mediaType).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.valueOf(mediaType)));
    String entity = response.readEntity(String.class);
    assertThat(entity, containsString(DBEERPEDIA.BREWERIES.stringValue()));
    assertThat(entity, containsString("<" + DBEERPEDIA.BREWERIES.stringValue() + ">"));
  }

  @Test
  public void getBrewery() {
    // Act
    Response response = target.path(String.format("/dbp/api/v1/breweries/%s",
        DBEERPEDIA.BROUWTOREN.getLocalName())).request().get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.valueOf("text/turtle")));
    assertThat(response.readEntity(String.class), containsString(DBEERPEDIA.BREWERIES.stringValue()));
  }

  @Test
  public void optionsMethod() {
    // Act
    Response response =
        target.path("/dbp/api/v1/breweries").request().options();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.TEXT_HTML_TYPE));
    assertThat(response.readEntity(String.class), equalTo("HEAD, GET, OPTIONS"));
    assertThat(response.getHeaderString("allow"), equalTo("HEAD,GET,OPTIONS"));
  }

  @Test
  public void headMethod() {
    // Act
    Response response = target.path("/dbp/api/v1/breweries").request().head();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.valueOf("text/turtle")));
    assertThat(response.readEntity(String.class), isEmptyString());
  }

  @Test
  public void resourceNotFound() {
    // Act
    Response response = target.path("/dbp/api/v1/foo").request().get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  public void methodNotAllowed() {
    // Act
    Response response = target.path("/dbp/api/v1/breweries").request().delete();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.METHOD_NOT_ALLOWED.getStatusCode()));
  }

  @Test
  public void notAcceptable() {
    // Act
    Response response =
        target.path("/dbp/api/v1/breweries").request(MediaType.APPLICATION_OCTET_STREAM).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.NOT_ACCEPTABLE.getStatusCode()));
  }

}
