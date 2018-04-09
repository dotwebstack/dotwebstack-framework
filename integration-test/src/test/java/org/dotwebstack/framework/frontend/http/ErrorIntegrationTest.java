package org.dotwebstack.framework.frontend.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
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
public class ErrorIntegrationTest {

  private WebTarget target;

  @LocalServerPort
  private int port;

  @Autowired
  private HttpConfiguration httpConfiguration;

  @Before
  public void setUp() throws IOException {
    target = ClientBuilder.newClient(httpConfiguration).target(
        String.format("http://localhost:%d", port));
  }

  @Test
  public void get_ReturnsNotFoundResponse_ForNonExistingPath() throws Exception {
    // Act
    Response response = target.path("/some/non-existing-path").request().get();

    // Assert
    assertThat(response.getStatusInfo(), equalTo(Status.NOT_FOUND));
    assertThat(response.getMediaType().toString(), equalTo("application/problem+json"));

    JsonNode jsonNode = new ObjectMapper().readTree(response.readEntity(String.class));
    assertEquals(Status.NOT_FOUND.getStatusCode(), jsonNode.get("status").asInt());
    assertEquals(Status.NOT_FOUND.getReasonPhrase(), jsonNode.get("title").asText());
    assertEquals("HTTP 404 Not Found", jsonNode.get("detail").asText());
  }

  @Test
  public void get_ReturnsNotFoundResponse_ForNonExistingAsset() throws Exception {
    // Act
    Response response = target.path("/assets/non-existing-asset").request().get();
    // Assert
    assertThat(response.getStatusInfo(), equalTo(Status.NOT_FOUND));
    assertThat(response.getMediaType().toString(), equalTo("application/problem+json"));
    JsonNode jsonNode = new ObjectMapper().readTree(response.readEntity(String.class));
    assertEquals(Status.NOT_FOUND.getStatusCode(), jsonNode.get("status").asInt());
    assertEquals(Status.NOT_FOUND.getReasonPhrase(), jsonNode.get("title").asText());
    assertEquals("HTTP 404 Not Found", jsonNode.get("detail").asText());
  }

  @Test
  public void get_ReturnsErrorResponseWithoutExposingDetails_ForUnexpectedRuntimeException()
      throws Exception {
    // Act
    Response response = target.path("/runtime-exception").request().get();

    // Assert
    assertThat(response.getStatusInfo(), equalTo(Status.INTERNAL_SERVER_ERROR));
    assertThat(response.getMediaType().toString(), equalTo("application/problem+json"));
    JsonNode jsonNode = new ObjectMapper().readTree(response.readEntity(String.class));
    assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), jsonNode.get("status").asInt());
    assertEquals(Status.INTERNAL_SERVER_ERROR.getReasonPhrase(), jsonNode.get("title").asText());
    assertTrue(
        jsonNode.get("detail").asText()
            + " did not contain 'An error occured from which the server was unable to recover.'",
        jsonNode.get("detail").asText().contains(
            "An error occured from which the server was unable to recover."));

  }

}
