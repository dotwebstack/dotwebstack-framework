package org.dotwebstack.framework.service.openapi.param;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.List;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.service.openapi.requestbody.RequestBodyHandlerRouter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParameterResolverFactoryTest {
  @Mock
  JexlEngine jexlEngine;

  @Mock
  RequestBodyHandlerRouter requestBodyHandlerRouter;

  @Mock
  ParamHandlerRouter paramHandlerRouter;

  @Test
  void createSucceeds_withEmptyParameters() {
    Operation operation = mock(Operation.class, Answers.RETURNS_DEEP_STUBS);
    List<Parameter> parameters = List.of();
    when(operation.getParameters()).thenReturn(parameters);

    ParameterResolverFactory parameterResolverFactory =
        new ParameterResolverFactory(jexlEngine, requestBodyHandlerRouter, paramHandlerRouter);
    assertDoesNotThrow(() -> parameterResolverFactory.create(operation));
  }

  @Test
  void createSucceeds_withNonEmptyParameters() {
    Operation operation = mock(Operation.class, Answers.RETURNS_DEEP_STUBS);
    List<Parameter> parameters = List.of(mock(Parameter.class), mock(Parameter.class));
    when(operation.getParameters()).thenReturn(parameters);

    ParameterResolverFactory parameterResolverFactory =
        new ParameterResolverFactory(jexlEngine, requestBodyHandlerRouter, paramHandlerRouter);
    assertDoesNotThrow(() -> parameterResolverFactory.create(operation));
  }
}
