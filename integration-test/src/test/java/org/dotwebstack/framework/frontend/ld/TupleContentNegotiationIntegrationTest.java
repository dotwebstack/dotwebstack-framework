package org.dotwebstack.framework.frontend.ld;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.IOException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.dotwebstack.framework.SparqlHttpStub;
import org.dotwebstack.framework.SparqlHttpStub.TupleQueryResultBuilder;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.MediaTypes;
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
public class TupleContentNegotiationIntegrationTest {
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
  public void get_GetGraphInSparqlResultsJson_ThroughLdApi() throws IOException {
    // Act
    String response = getResultWithMediaType(MediaTypes.SPARQL_RESULTS_JSON_TYPE);

    // Assert
    assertThat(isValidJson(response), is(true));
    assertThat(isValidXml(response), is(false));
    assertContainsCorrectData(response);
  }

  @Test
  public void get_GetGraphInPlainJson_ThroughLdApi() {
    // Act
    String response = getResultWithMediaType(MediaType.APPLICATION_JSON_TYPE);

    // Assert
    assertThat(isValidJson(response), is(true));
    assertThat(isValidXml(response), is(false));
    assertContainsCorrectData(response);
  }

  @Test
  public void get_GetGraphInSparqlResultsXml_ThroughLdApi() {
    // Act
    String response = getResultWithMediaType(MediaTypes.SPARQL_RESULTS_XML_TYPE);

    // Assert
    assertThat(isValidXml(response), is(true));
    assertThat(isValidJson(response), is(false));
    assertContainsCorrectData(response);
  }

  @Test
  public void get_SparqlResultJsonAndPlainJson_AreDifferent() {
    // Act
    String sparqlResultJson = getResultWithMediaType(MediaTypes.SPARQL_RESULTS_JSON_TYPE);
    String plainJson = getResultWithMediaType(MediaType.APPLICATION_JSON_TYPE);

    // Assert
    assertThat(isValidJson(sparqlResultJson), is(true));
    assertThat(isValidJson(plainJson), is(true));
    assertThat(plainJson, is(not(equalTo(sparqlResultJson))));
  }

  @Test
  public void get_SparqlResultJsonAndPlainXml_AreDifferent() {
    // Act
    String sparqlResultXml = getResultWithMediaType(MediaTypes.SPARQL_RESULTS_XML_TYPE);
    String plainXml = getResultWithMediaType(MediaType.APPLICATION_XML_TYPE);

    // Assert
    assertThat(isValidXml(sparqlResultXml), is(true));
    assertThat(isValidXml(plainXml), is(true));
    assertThat(plainXml, is(not(equalTo(sparqlResultXml))));
  }

  @Test
  public void get_GetGraphInPlainXml_ThroughLdApi() {
    // Act
    String response = getResultWithMediaType(MediaType.APPLICATION_XML_TYPE);

    // Assert
    assertThat(isValidXml(response), is(true));
    assertThat(isValidJson(response), is(false));
    assertContainsCorrectData(response);
  }

  private String getResultWithMediaType(MediaType mediaType) {
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
    Response response = target.path("/dbp/ld/v1/tuple-breweries").request().accept(mediaType).get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(mediaType));
    assertThat(response.getLength(), greaterThan(0));

    return response.readEntity(String.class);
  }

  private void assertContainsCorrectData(String response) {
    assertThat(response, containsString(DBEERPEDIA.BROUWTOREN_NAME.stringValue()));
    assertThat(response, containsString(DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION.stringValue()));
    assertThat(response, containsString(DBEERPEDIA.BROUWTOREN_PLACE.stringValue()));
    assertThat(response, containsString(DBEERPEDIA.BROUWTOREN_FTE.stringValue()));
    assertThat(response, containsString(DBEERPEDIA.BROUWTOREN_DATE_OF_FOUNDATION.stringValue()));
    assertThat(response, containsString(DBEERPEDIA.MAXIMUS_NAME.stringValue()));
    assertThat(response, containsString(DBEERPEDIA.MAXIMUS_YEAR_OF_FOUNDATION.stringValue()));
    assertThat(response, containsString(DBEERPEDIA.MAXIMUS_PLACE.stringValue()));
    assertThat(response, containsString(DBEERPEDIA.MAXIMUS_FTE.stringValue()));
    assertThat(response, containsString(DBEERPEDIA.MAXIMUS_DATE_OF_FOUNDATION.stringValue()));
  }

  private boolean isValidJson(String json) {
    try {
      final ObjectMapper mapper = new ObjectMapper();
      mapper.readTree(json);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  private boolean isValidXml(String xml) {
    try {
      final XmlMapper mapper = new XmlMapper();
      mapper.readTree(xml);
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
