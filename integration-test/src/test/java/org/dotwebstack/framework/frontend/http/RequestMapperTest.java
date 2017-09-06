package org.dotwebstack.framework.frontend.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;

import java.io.IOException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
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
public class RequestMapperTest {

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
  public void getBreweriesTest() {
    // Act
    Response response = target.path("/dbp/breweries").request().get();

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getMediaType(), equalTo(MediaType.TEXT_HTML_TYPE));
    assertThat(response.readEntity(String.class),
        equalTo(DBEERPEDIA.BREWERY_LIST_REPRESENTATION.stringValue()));
  }

  @Test
  public void optionsMethodTest() {
    // Act
    Response response =
        target.path("/dbp/breweries").request(MediaType.TEXT_HTML_TYPE).options();

    // Assert
    Assert.assertThat(response.getStatus(), CoreMatchers.equalTo(Status.OK.getStatusCode()));
    Assert.assertThat(response.getMediaType(), CoreMatchers.equalTo(MediaType.TEXT_HTML_TYPE));
    Assert
        .assertThat(response.readEntity(String.class), CoreMatchers.equalTo(""));
    Assert
        .assertThat(response.getHeaderString("allow"), CoreMatchers.equalTo("HEAD,GET,OPTIONS"));
  }

  @Test
  public void headMethodTest() {
    // Act
    Response response = target.path("/dbp/breweries").request().head();

    // Assert
    Assert.assertThat(response.getStatus(), CoreMatchers.equalTo(Status.OK.getStatusCode()));
    Assert.assertThat(response.getMediaType(), CoreMatchers.equalTo(MediaType.TEXT_HTML_TYPE));
    Assert.assertThat(response.getLength(), CoreMatchers.equalTo(47));
    Assert.assertThat(response.readEntity(String.class), isEmptyString());
  }

  @Test
  public void resourceNotFoundTest() {
    // Act
    Response response = target.path("/dbp/xxx").request().get();

    // Assert
    Assert.assertThat(response.getStatus(), CoreMatchers.equalTo(Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  public void methodNotAllowedTest() {
    // Act
    Response response = target.path("/dbp/breweries").request().delete();

    // Assert
    Assert.assertThat(response.getStatus(),
        CoreMatchers.equalTo(Status.METHOD_NOT_ALLOWED.getStatusCode()));
  }
}
