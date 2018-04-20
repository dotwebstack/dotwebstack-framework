package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import javax.ws.rs.container.ContainerRequestContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OpenApiSpecHandlerTest {

  @Mock
  private ContainerRequestContext mockRequestContext;

  @Test
  public void apply_ReturnsYamlSpec() throws Exception {

    // Arrange
    String yaml = "swagger: 2.0\n" + //
        "info:\n" + //
        "  title: DBeerPedia API\n" + //
        "  version: 1.0\n" + //
        "basePath: /dbp/api/v1\n" + //
        "produces:\n" + //
        "  - application/json\n" + //
        // these should all be removed
        "x-dotwebstack-ldpath-namespaces:\n" + //
        "  dbeerpedia: http://dbeerpedia.org#\n" + //
        "  elmo: http://dotwebstack.org/def/elmo#\n" + //
        "  rdfs: http://www.w3.org/2000/01/rdf-schema#\n" + //
        "  xsd: http://www.w3.org/2001/XMLSchema#\n" + //
        "paths:\n" + //
        "  /breweries:\n" + //
        "    get:\n" + //
        // and this should be removed too
        "      x-dotwebstack-information-product: \"http://dbeerpedia.org#TupleBreweries\"";

    // Act
    OpenApiSpecHandler handler = new OpenApiSpecHandler(yaml);
    String result = handler.apply(mockRequestContext);

    // Assert
    YAMLMapper mapper = new YAMLMapper();
    // the result is valid yaml;
    mapper.readTree(result);
    assertFalse(result.contains("x-dotwebstack"));
    String expectedYaml = "---\n" + //
        "swagger: 2.0\n" + //
        "info:\n" + //
        "  title: \"DBeerPedia API\"\n" + //
        "  version: 1.0\n" + //
        "basePath: \"/dbp/api/v1\"\n" + //
        "produces:\n" + //
        "- \"application/json\"\n" + //
        "paths:\n" + //
        "  /breweries:\n" + //
        "    get: {}\n"; //
    assertEquals(expectedYaml, result);

  }

}
