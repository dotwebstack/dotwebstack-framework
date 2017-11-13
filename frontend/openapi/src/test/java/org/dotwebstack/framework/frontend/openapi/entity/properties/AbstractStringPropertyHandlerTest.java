package org.dotwebstack.framework.frontend.openapi.entity.properties;

import static org.mockito.Mockito.mock;

import io.swagger.models.properties.StringProperty;
import java.util.Collections;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilderContext;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractStringPropertyHandlerTest {

  static final String DUMMY_EXPR = "dummyExpr()";
  static final Literal VALUE_1 = SimpleValueFactory.getInstance().createLiteral("a");
  static final Literal VALUE_2 = SimpleValueFactory.getInstance().createLiteral("b");

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  EntityBuilderContext entityBuilderContext;

  @Mock
  Value context;

  LdPathExecutor ldPathExecutor;

  PropertyHandlerRegistry registry;

  PropertyHandler<?> handler;
  StringProperty stringProperty;

  AbstractStringPropertyHandlerTest() {
    entityBuilderContext = mock(EntityBuilderContext.class);
    ldPathExecutor = mock(LdPathExecutor.class);
  }

  @Before
  public void setUp() {
    handler = new StringPropertyHandler();
    stringProperty = new StringProperty();
    registry = new PropertyHandlerRegistry();
    registry.setPropertyHandlers(Collections.singleton(handler));
  }

}
