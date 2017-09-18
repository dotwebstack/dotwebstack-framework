package org.dotwebstack.framework.frontend.ld;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.dotwebstack.framework.SparqlHttpStub;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class LdIntegrationTest {
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

  @BeforeClass
  public static void startStub() {
    SparqlHttpStub.start();
  }

  @AfterClass
  public static void stopStub() {
    SparqlHttpStub.stop();
  }

  @Test
  public void getBreweryCollection() {
    // Arrange
    Model model = new ModelBuilder().subject(DBEERPEDIA.BREWERIES).add(RDFS.LABEL,
        DBEERPEDIA.BREWERIES_LABEL).build();
    SparqlHttpStub.returnModel(model);
    MediaType mediaType = MediaType.valueOf("text/turtle");

    // Act
    Response response = target.path("/dbp/ld/v1/breweries").request().accept(mediaType).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(mediaType));
    assertThat(response.getLength(), greaterThan(0));
    assertThat(response.readEntity(String.class),
        containsString(DBEERPEDIA.BREWERIES_LABEL.stringValue()));
  }

  @Test
  public void optionsMethod() {
    // Act
    Response response =
        target.path("/dbp/ld/v1/breweries").request(MediaType.TEXT_PLAIN_TYPE).options();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.TEXT_PLAIN_TYPE));
    assertThat(response.readEntity(String.class), equalTo("HEAD, GET, OPTIONS"));
    assertThat(response.getHeaderString("allow"), equalTo("HEAD,GET,OPTIONS"));
  }

  @Test
  public void headMethod() {
    // Arrange
    Model model = new ModelBuilder().subject(DBEERPEDIA.BREWERIES).add(RDFS.LABEL,
        DBEERPEDIA.BREWERIES_LABEL).build();
    SparqlHttpStub.returnModel(model);

    // Act
    Response response = target.path("/dbp/ld/v1/breweries").request("application/ld+json").head();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.valueOf("application/ld+json")));
    assertThat(response.getLength(), greaterThan(0));
    assertThat(response.readEntity(String.class), isEmptyString());
  }

  @Test
  public void resourceNotFound() {
    // Act
    Response response = target.path("/dbp/ld/v1/foo").request().get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  public void methodNotAllowed() {
    // Act
    Response response = target.path("/dbp/ld/v1/breweries").request().delete();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.METHOD_NOT_ALLOWED.getStatusCode()));
  }

  @Test
  public void notAcceptable() {
    // Act
    Response response =
        target.path("/dbp/ld/v1/breweries").request(MediaType.APPLICATION_OCTET_STREAM).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.NOT_ACCEPTABLE.getStatusCode()));
  }
}
