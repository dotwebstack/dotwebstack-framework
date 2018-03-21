package org.dotwebstack.framework.frontend.ld;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.http.HttpStatus;
import org.dotwebstack.framework.SparqlHttpStub;
import org.dotwebstack.framework.SparqlHttpStub.TupleQueryResultBuilder;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.glassfish.jersey.client.ClientProperties;
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
public class LdIntegrationTest {

  private WebTarget target;

  @LocalServerPort
  private int port;

  @Autowired
  private HttpConfiguration httpConfiguration;

  @Before
  public void setUp() throws IOException {
    target = ClientBuilder.newClient(httpConfiguration).target(
        String.format("http://localhost:%d", this.port)).property(ClientProperties.FOLLOW_REDIRECTS,
            Boolean.FALSE);

    SparqlHttpStub.start();
  }

  @Test
  public void get_GetBreweryCollection_ThroughLdApi() {
    // Arrange
    Model model = new ModelBuilder().subject(DBEERPEDIA.BREWERIES).add(RDFS.LABEL,
        DBEERPEDIA.BREWERIES_LABEL).build();
    SparqlHttpStub.returnGraph(model);
    MediaType mediaType = MediaType.valueOf("text/turtle");

    // Act
    Response response = target.path("/dbp/ld/v1/graph-breweries").request().accept(mediaType).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(mediaType));
    assertThat(response.getLength(), greaterThan(0));
    assertThat(response.readEntity(String.class),
        containsString(DBEERPEDIA.BREWERIES_LABEL.stringValue()));
  }

  @Test
  public void get_InternalServerError_WhenRequiredIsMissing() {
    // Act
    Response response = target.path("/dbp/ld/v1/tuple-brewery").request().get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.INTERNAL_SERVER_ERROR.getStatusCode()));
  }

  @Test
  public void get_GetOneBreweryWithParameter_ThroughLdApi() {
    // Arrange
    TupleQueryResultBuilder builder =
        new TupleQueryResultBuilder("naam", "sinds", "fte", "oprichting", "plaats").resultSet(
            DBEERPEDIA.BROUWTOREN_NAME, DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION,
            DBEERPEDIA.BROUWTOREN_FTE, DBEERPEDIA.BROUWTOREN_DATE_OF_FOUNDATION,
            DBEERPEDIA.BROUWTOREN_PLACE).resultSet(DBEERPEDIA.MAXIMUS_NAME,
                DBEERPEDIA.MAXIMUS_YEAR_OF_FOUNDATION, DBEERPEDIA.MAXIMUS_FTE,
                DBEERPEDIA.MAXIMUS_DATE_OF_FOUNDATION, DBEERPEDIA.MAXIMUS_PLACE);
    SparqlHttpStub.returnTuple(builder);
    MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;

    // Act
    Response response = target.path("/dbp/ld/v1/tuple-brewery").queryParam("id",
        DBEERPEDIA.MAXIMUS_NAME).request().accept(mediaType).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(mediaType));
    assertThat(response.getLength(), greaterThan(0));
  }

  @Test
  public void get_GetCorrectOptions_ThroughLdApi() {
    // Act
    Response response =
        target.path("/dbp/ld/v1/graph-breweries").request(MediaType.TEXT_PLAIN_TYPE).options();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.TEXT_PLAIN_TYPE));
    assertThat(response.readEntity(String.class), equalTo("HEAD, GET, OPTIONS"));
    assertThat(response.getHeaderString("allow"), equalTo("HEAD,GET,OPTIONS"));
  }

  @Test
  public void get_GetCorrectHead_ThroughLdApi() {
    // Arrange
    Model model = new ModelBuilder().subject(DBEERPEDIA.BREWERIES).add(RDFS.LABEL,
        DBEERPEDIA.BREWERIES_LABEL).build();
    SparqlHttpStub.returnGraph(model);

    // Act
    Response response =
        target.path("/dbp/ld/v1/graph-breweries").request("application/ld+json").head();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.valueOf("application/ld+json")));
    assertThat(response.getLength(), greaterThan(0));
    assertThat(response.readEntity(String.class), isEmptyString());
  }

  @Test
  public void get_GetRedirection_ThroughLdApi() throws URISyntaxException {
    // Act
    Response response = target.path("/dbp/ld/v1/id/breweries").request().get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.SEE_OTHER.getStatusCode()));
    assertThat(response.getLocation().getPath(), equalTo("/dbp/ld/v1/doc/breweries"));
    assertThat(response.readEntity(String.class), isEmptyString());
  }

  @Test
  public void get_ResourceNotFound_WhenResourceIsNotDefined() {
    // Act
    Response response = target.path("/dbp/ld/v1/foo").request().get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  public void get_MethodNotAllowed_WhenDelete() {
    // Act
    Response response = target.path("/dbp/ld/v1/graph-breweries").request().delete();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.METHOD_NOT_ALLOWED.getStatusCode()));
  }

  @Test
  public void get_NotAcceptable_WhenRequestingWrongMediaType() {
    // Arrange
    Model model = new ModelBuilder().subject(DBEERPEDIA.BREWERIES).add(RDFS.LABEL,
        DBEERPEDIA.BREWERIES_LABEL).build();
    SparqlHttpStub.returnGraph(model);

    // Act
    Response response =
        target.path("/dbp/ld/v1/graph-breweries").request(MediaType.APPLICATION_OCTET_STREAM).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.NOT_ACCEPTABLE.getStatusCode()));
  }

  @Test
  public void get_GetDocResource_ThroughLdApi() {
    // Arrange
    Model model = new ModelBuilder().subject(DBEERPEDIA.BREWERIES).add(RDFS.LABEL,
        DBEERPEDIA.BREWERIES_LABEL).build();
    SparqlHttpStub.returnGraph(model);
    MediaType mediaType = MediaType.valueOf("text/turtle");

    // Act
    Response response = target.path("/dbp/ld/v1/doc/breweries").request().accept(mediaType).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(mediaType));
    assertThat(response.getLength(), greaterThan(0));
    assertThat(response.readEntity(String.class),
        containsString(DBEERPEDIA.BREWERIES_LABEL.stringValue()));
  }

  @Test
  public void post_PostRdf_ThroughLdApi() {
    // Arrange
    String rdf = "<?xml version=\"1.0\"?>\n" + "\n" + "<rdf:RDF\n"
        + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
        + "xmlns:si=\"https://www.w3schools.com/rdf/\">\n" + "\n"
        + "<rdf:Description rdf:about=\"https://www.w3schools.com\">\n"
        + " <si:title>W3Schools</si:title>\n" + " <si:author>Jan Egil Refsnes</si:author>\n"
        + "</rdf:Description>\n" + "\n" + "</rdf:RDF> ";
    SparqlHttpStub.setResponseCode(HttpStatus.SC_OK);

    // Act
    Response response =
        target.path("/dbp/ld/v1/add-concept").request().post(Entity.entity(rdf, MediaTypes.RDFXML));

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
  }

  @Test
  public void post_WhenBackendFails_ThroughLdApi() {
    // Arrange
    String rdf = "<?xml version=\"1.0\"?>\n" + "\n" + "<rdf:RDF\n"
        + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
        + "xmlns:si=\"https://www.w3schools.com/rdf/\">\n" + "\n"
        + "<rdf:Description rdf:about=\"https://www.w3schools.com\">\n"
        + "  <si:title>W3Schools</si:title>\n" + "  <si:author>Jan Egil Refsnes</si:author>\n"
        + "</rdf:Description>\n" + "\n" + "</rdf:RDF> ";
    SparqlHttpStub.setResponseCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);

    // Act
    Response response =
        target.path("/dbp/ld/v1/add-concept").request().post(Entity.entity(rdf, MediaTypes.RDFXML));

    // Assert
    assertThat(response.getStatus(), equalTo(Status.INTERNAL_SERVER_ERROR.getStatusCode()));
  }

}
