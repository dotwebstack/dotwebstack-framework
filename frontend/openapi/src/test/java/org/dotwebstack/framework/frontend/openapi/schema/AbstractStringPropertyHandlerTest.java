package org.dotwebstack.framework.frontend.openapi.schema;

import static org.mockito.Mockito.mock;

import io.swagger.models.properties.StringProperty;
import java.util.Arrays;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.eclipse.rdf4j.model.Value;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractStringPropertyHandlerTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  GraphEntityContext entityBuilderContextMock;

  @Mock
  Value contextMock;

  LdPathExecutor ldPathExecutorMock;

  SchemaMapperAdapter registry;

  SchemaMapper handler;
  StringProperty stringProperty;

  AbstractStringPropertyHandlerTest() {
    entityBuilderContextMock = mock(GraphEntityContext.class);
    ldPathExecutorMock = mock(LdPathExecutor.class);
  }

  @Before
  public void setUp() {
    handler = new StringSchemaMapper();
    stringProperty = new StringProperty();
    registry = new SchemaMapperAdapter(Arrays.asList(handler));
  }

}
