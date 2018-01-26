package org.dotwebstack.framework.frontend.ld;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.StringReader;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.dotwebstack.framework.SparqlHttpStub;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
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
public class GraphContentNegotiationIntegrationTest {

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
  public void get_GetGraphInTurtle_ThroughLdApi() throws IOException {
    // Act
    String response = getResultWithMediaType(MediaTypes.TURTLE_TYPE);

    // Assert
    Model model = Rio.parse(new StringReader(response), "", RDFFormat.TURTLE);
    assertModel(model);
  }

  @Test
  public void get_GetGraphInTrig_ThroughLdApi() throws IOException {
    // Act
    String response = getResultWithMediaType(MediaTypes.TRIG_TYPE);

    // Assert
    Model model = Rio.parse(new StringReader(response), "", RDFFormat.TRIG);
    assertModel(model);
  }

  @Test
  public void get_GetGraphInLdJson_ThroughLdApi() throws IOException {
    // Act
    String response = getResultWithMediaType(MediaTypes.LDJSON_TYPE);

    // Assert
    Model model = Rio.parse(new StringReader(response), "", RDFFormat.JSONLD);
    assertModel(model);
  }

  @Test
  public void get_GetGraphInPlainXml_ThroughLdApi() {
    // Act
    String response = getResultWithMediaType(MediaType.APPLICATION_XML_TYPE);

    // Assert
    assertThat(response, containsString(DBEERPEDIA.BREWERIES_LABEL.stringValue()));
    assertThat(response, containsString("<rdf:RDF"));
    assertThat(response, containsString("<rdf:Description"));
  }

  @Test
  public void get_GetGraphInRdfXml_ThroughLdApi() {
    // Act
    String response = getResultWithMediaType(MediaTypes.RDFXML_TYPE);

    // Assert
    assertThat(response, containsString(DBEERPEDIA.BREWERIES_LABEL.stringValue()));
    assertThat(response, containsString("<rdf:RDF"));
    assertThat(response, containsString("<rdf:Description"));
  }

  @Test
  public void get_GetGraphInPlainJson_ThroughLdApi() throws IOException {
    // Act
    String response = getResultWithMediaType(MediaType.APPLICATION_JSON_TYPE);

    // Assert
    Model model = Rio.parse(new StringReader(response), "", RDFFormat.JSONLD);
    assertModel(model);
  }

  private String getResultWithMediaType(MediaType mediaType) {
    // Arrange
    Model model = new ModelBuilder().subject(DBEERPEDIA.BREWERIES).add(RDFS.LABEL,
        DBEERPEDIA.BREWERIES_LABEL).build();
    SparqlHttpStub.returnGraph(model);

    // Act
    Response response = target.path("/dbp/ld/v1/graph-breweries").request().accept(mediaType).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(mediaType));
    assertThat(response.getLength(), greaterThan(0));

    return response.readEntity(String.class);
  }

  private void assertModel(Model model) {
    assertThat(model.subjects(), hasSize(1));
    assertThat(model.subjects().stream().findFirst().get(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(model.predicates(), hasSize(1));
    assertThat(model.predicates().stream().findFirst().get(), equalTo(RDFS.LABEL));
    assertThat(model.objects(), hasSize(1));
    assertThat(model.objects().stream().findFirst().get(), equalTo(DBEERPEDIA.BREWERIES_LABEL));
  }

}
