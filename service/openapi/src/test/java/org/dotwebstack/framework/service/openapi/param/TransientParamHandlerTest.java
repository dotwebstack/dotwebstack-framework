package org.dotwebstack.framework.service.openapi.param;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_TRANSIENT;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_TYPE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransientParamHandlerTest {

  private OpenAPI openApi;

  private ParamHandler paramHandler;

  @Mock
  private Parameter parameter;

  @BeforeEach
  public void setup() {
    this.openApi = TestResources.openApi();
    this.paramHandler = new TransientParamHandler(this.openApi);
  }

  @Test
  public void test_supports_TransientExtension() {
    // Arrange
    when(this.parameter.getExtensions()).thenReturn(ImmutableMap.of(X_DWS_TRANSIENT, Boolean.TRUE));

    // Act & Assert
    assertTrue(this.paramHandler.supports(this.parameter));
  }

  @Test
  public void test_not_supports_OtherExtension() {
    // Arrange
    when(this.parameter.getExtensions()).thenReturn(ImmutableMap.of(X_DWS_TYPE, "SOMETHING ELSE"));

    // Act & Assert
    assertFalse(this.paramHandler.supports(this.parameter));
  }
}
