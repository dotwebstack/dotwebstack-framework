package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlassian.oai.validator.model.ApiOperation;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import javax.ws.rs.core.Response.Status;
import org.dotwebstack.framework.EnvironmentAwareResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.env.Environment;
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
    method.set("get", new Operation());

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

  @Test
  public void removeVendorExtensions_removesAll() throws Exception {

    // Arrange
    Environment mockEnvironment = mock(Environment.class);
    when(mockEnvironment.getProperty(anyString())).thenReturn("some-environment-value");

    YAMLMapper mapper = new YAMLMapper();

    InputStream originalInputStream =
        new EnvironmentAwareResource(SwaggerUtilsTest.class.getResourceAsStream("dbeerpedia.yml"),
            mockEnvironment).getInputStream();
    ObjectNode specNode = mapper.readValue(originalInputStream, ObjectNode.class);
    String original = mapper.writer().writeValueAsString(specNode);
    assertTrue("The original should have vendor extensions", original.contains("x-dotwebstack"));

    InputStream testInputStream =
        new ByteArrayInputStream(original.getBytes(Charset.forName("UTF-8")));

    // Act
    ObjectNode removedNode = SwaggerUtils.removeVendorExtensions(testInputStream, mapper);

    // Assert
    String stripped = mapper.writer().writeValueAsString(removedNode);

    assertNotEquals(original, stripped);
    assertFalse("No vendor extensions should exist, but some are found!",
        stripped.contains("x-dotwebstack"));
  }

}
