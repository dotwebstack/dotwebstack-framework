package org.dotwebstack.framework.frontend.openapi.entity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.swagger.models.Swagger;
import java.util.Map;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class LdPathExecutorTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testWeirdLdNamespaces() {
    GraphEntityContext builderCtx = mock(GraphEntityContext.class);
    Swagger swagger = mock(Swagger.class);
    Map<String, Object> vendorExtensions = Maps.newHashMap();

    /* we'd expect x-ldpath-namespaces extension to be of type map */
    vendorExtensions.put(OpenApiSpecificationExtensions.LDPATH_NAMESPACES, "this is not a map");
    when(swagger.getVendorExtensions()).thenReturn(vendorExtensions);
      Map<String, io.swagger.models.Model> namespaces = ImmutableMap.of();
    when(builderCtx.getSwaggerDefinitions()).thenReturn(namespaces);

    Model model =
        new ModelBuilder().subject("generic:subj").add("predicate:is", "object:obj").build();

    when(builderCtx.getModel()).thenReturn(model);

    exception.expect(RuntimeException.class);
    exception.expectMessage(String.format(
        "Vendor extension '%s' should contain a map of namespaces (eg. "
            + "{ \"rdfs\": \"http://www.w3.org/2000/01/rdf-schema#\", "
            + "\"rdf\": \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"})",
        OpenApiSpecificationExtensions.LDPATH_NAMESPACES));

    new LdPathExecutor(builderCtx);
  }

}
