package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
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

  private Path method;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws IOException {
    swagger = createSwagger();
    path = "/endpoint";
    method = new Path();
    method.set("get",new Operation());

  }

  @Test
  public void extractApiOperation_ReturnsValidApiOperation_WithSpecifiedGet() throws IOException {
    path = "/endpoint";

    ApiOperation apiOperation = SwaggerUtils.extractApiOperation(swagger, path, method);

    Operation operation = apiOperation.getOperation();

    assertThat(apiOperation.getMethod(), is(HttpMethod.GET));
    assertThat(apiOperation.getApiPath().normalised(), is(path));
    assertThat(operation.getParameters(), hasSize(2));
    assertThat(operation.getResponses(), hasKey(Integer.toString(Status.OK.getStatusCode())));
  }

  @Test
  public void extractApiOperation_ReturnsNull_WithUnspecifiedGet() throws IOException {
    path = "/unknown";

    ApiOperation apiOperation = SwaggerUtils.extractApiOperation(swagger, path, method);

    assertThat(apiOperation, nullValue());
  }

  private static Swagger createSwagger() throws IOException {
    String oasSpecContent = StreamUtils.copyToString(
        SwaggerUtilsTest.class.getResourceAsStream(SwaggerUtilsTest.class.getSimpleName() + ".yml"),
        Charset.forName("UTF-8"));
    return new SwaggerParser().parse(oasSpecContent);
  }

}
