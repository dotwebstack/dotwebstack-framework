package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import com.atlassian.oai.validator.interaction.RequestValidator;
import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.ws.rs.core.Response.Status;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.util.StreamUtils;

public class SwaggerUtilsTest {

  private Swagger swagger;

  private String path;

  private String method;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws IOException {
    swagger = createSwagger();
    path = "/endpoint";
    method = "get";

  }

  @Test
  public void createValidator_ThrowsException_WithMissingSwagger() {
    thrown.expect(NullPointerException.class);

    SwaggerUtils.createValidator(null);
  }

  @Test
  public void createValidator_ReturnsRequestValidator_ForValidSwagger() {
    RequestValidator requestValidator = SwaggerUtils.createValidator(swagger);

    assertThat(requestValidator, instanceOf(RequestValidator.class));
  }

  @Test
  public void extractApiOperation_ThrowsException_WithMissingSwagger() {
    thrown.expect(NullPointerException.class);

    SwaggerUtils.extractApiOperation(null, path, method);
  }

  @Test
  public void extractApiOperation_ThrowsException_WithMissingPath() {
    thrown.expect(NullPointerException.class);

    SwaggerUtils.extractApiOperation(swagger, null, method);
  }

  @Test
  public void extractApiOperation_ThrowsException_WithMissingMethod() {
    thrown.expect(NullPointerException.class);

    SwaggerUtils.extractApiOperation(swagger, path, null);
  }

  @Test
  public void extractApiOperation_ReturnsValidApiOperation_WithSpecifiedGet() throws IOException {
    path = "/endpoint";

    ApiOperation apiOperation = SwaggerUtils.extractApiOperation(swagger, path, method);

    Operation operation = apiOperation.getOperation();
    assertThat(operation, instanceOf(Operation.class));
    assertThat(apiOperation.getMethod(), equalTo(HttpMethod.GET));
    assertThat(apiOperation.getApiPath().normalised(), equalTo(path));
    assertThat(operation.getParameters().size(), equalTo(2));
    assertThat(operation.getResponses().containsKey(Integer.toString(Status.OK.getStatusCode())),
        equalTo(true));
  }

  @Test
  public void extractApiOperation_ReturnsNull_WithUnspecifiedGet() throws IOException {
    path = "/unknown";

    ApiOperation apiOperation = SwaggerUtils.extractApiOperation(swagger, path, method);

    assertThat(apiOperation, equalTo(null));
  }


  private Swagger createSwagger() throws IOException {
    String oasSpecContent = StreamUtils.copyToString(
        getClass().getResourceAsStream(getClass().getSimpleName() + ".yml"),
        Charset.forName("UTF-8"));
    return new SwaggerParser().parse(oasSpecContent);
  }

}
