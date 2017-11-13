package org.dotwebstack.framework.frontend.openapi.entity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import io.swagger.models.Swagger;
import java.util.Map;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilderContext;
import org.dotwebstack.framework.frontend.openapi.entity.builder.OasVendorExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
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
    EntityBuilderContext builderCtx = mock(EntityBuilderContext.class);
    Swagger swagger = mock(Swagger.class);
    Map<String, Object> vendorExtensions = Maps.newHashMap();

    /* we'd expect x-ldpath-namespaces extension to be of type map */
    vendorExtensions.put(OasVendorExtensions.LDPATH_NAMESPACES, "this is not a map");
    when(swagger.getVendorExtensions()).thenReturn(vendorExtensions);
    when(builderCtx.getSwagger()).thenReturn(swagger);

    QueryResult queryResult = mock(QueryResult.class);
    Model model =
        new ModelBuilder().subject("generic:subj").add("predicate:is", "object:obj").build();
    when(queryResult.getModel()).thenReturn(model);
    when(builderCtx.getQueryResult()).thenReturn(queryResult);

    exception.expect(RuntimeException.class);
    exception.expectMessage(String.format(
        "Vendor extension '%s' should contain a map of namespaces (eg. "
            + "{ \"rdfs\": \"http://www.w3.org/2000/01/rdf-schema#\", "
            + "\"rdf\": \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"})",
        OasVendorExtensions.LDPATH_NAMESPACES));

    new LdPathExecutor(builderCtx);
  }

}
