package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
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
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import javax.ws.rs.core.Response.Status;
import org.dotwebstack.framework.EnvironmentAwareResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.env.Environment;
import org.springframework.util.StreamUtils;

public class OpenApiSpecUtilsTest {

  private OpenAPI openApi;

  private String path;

  private PathItem method;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws IOException {
    openApi = createOpenApi();
    path = "/endpoint";
    method = new PathItem();
    method.setGet(new Operation());
  }

  @Test
  public void extractApiOperation_ReturnsValidApiOperation_WithSpecifiedGet() {
    path = "/endpoint";

    Collection<ApiOperation> apiOperations =
        OpenApiSpecUtils.extractApiOperations(openApi, path, method);

    ApiOperation apiOperation = apiOperations.stream().findFirst().get();
    Operation operation = apiOperation.getOperation();

    assertThat(apiOperation.getMethod(), is(HttpMethod.GET));
    assertThat(apiOperation.getApiPath().normalised(), is(path));
    assertThat(operation.getParameters(), hasSize(2));
    assertThat(operation.getResponses(), hasKey(Integer.toString(Status.OK.getStatusCode())));
  }

  @Test
  public void extractApiOperation_ReturnsNull_WithUnspecifiedGet() {
    path = "/unknown";

    Collection<ApiOperation> apiOperations =
        OpenApiSpecUtils.extractApiOperations(openApi, path, method);

    assertThat(apiOperations, empty());
  }

  private static OpenAPI createOpenApi() throws IOException {
    String oasSpecContent = StreamUtils.copyToString(
        OpenApiSpecUtilsTest.class.getResourceAsStream(
            OpenApiSpecUtilsTest.class.getSimpleName() + ".yml"),
        Charset.forName("UTF-8"));
    return new OpenAPIV3Parser().readContents(oasSpecContent).getOpenAPI();
  }

  @Test
  public void removeVendorExtensions_removesAll() throws Exception {
    // Arrange
    Environment mockEnvironment = mock(Environment.class);
    when(mockEnvironment.getProperty(anyString())).thenReturn("some-environment-value");

    YAMLMapper mapper = new YAMLMapper();

    InputStream originalInputStream =
        new EnvironmentAwareResource(
            OpenApiSpecUtilsTest.class.getResourceAsStream("dbeerpedia.yml"),
            mockEnvironment).getInputStream();
    ObjectNode specNode = mapper.readValue(originalInputStream, ObjectNode.class);
    String original = mapper.writer().writeValueAsString(specNode);
    assertTrue("The original should have vendor extensions", original.contains("x-dotwebstack"));

    InputStream testInputStream =
        new ByteArrayInputStream(original.getBytes(Charset.forName("UTF-8")));

    // Act
    ObjectNode removedNode = OpenApiSpecUtils.removeVendorExtensions(testInputStream, mapper);

    // Assert
    String stripped = mapper.writer().writeValueAsString(removedNode);

    assertNotEquals(original, stripped);
    assertFalse("No vendor extensions should exist, but some are found!",
        stripped.contains("x-dotwebstack"));
  }

}
