package org.dotwebstack.framework.frontend.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
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
public class HttpConfigurationTest {

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
  public void get_GivesValidResponse_ForStaticAsset() {
    // Act
    Response response = target.path("/robots.txt").request().get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.TEXT_PLAIN_TYPE));
    assertThat(response.readEntity(String.class), equalTo("User-agent: *\nDisallow:\n"));
  }

}
