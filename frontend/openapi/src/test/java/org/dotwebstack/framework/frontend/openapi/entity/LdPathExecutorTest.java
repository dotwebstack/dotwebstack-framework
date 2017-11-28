package org.dotwebstack.framework.frontend.openapi.entity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
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
    Map<String, io.swagger.models.Model> namespaces = ImmutableMap.of();
    when(builderCtx.getSwaggerDefinitions()).thenReturn(namespaces);

    Model model =
        new ModelBuilder().subject("generic:subj").add("predicate:is", "object:obj").build();

    when(builderCtx.getModel()).thenReturn(model);
    LdPathExecutor executor = new LdPathExecutor(builderCtx);
    exception.expect(LdPathExecutorRuntimeException.class);
    executor.ldPathQuery(null, "");
  }

}
