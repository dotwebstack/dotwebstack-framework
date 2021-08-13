package org.dotwebstack.framework.service.openapi.response;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.service.openapi.HttpMethodOperation;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR_VALUE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OasResponseBuilderTest {

  private OpenAPI openApi;

  @BeforeEach
  void setup() {
    this.openApi = TestResources.openApi();
  }

  @Test
  void go(){
    Schema<?> schema = openApi.getComponents().getSchemas().get("Object1");
    OasResponse response = new OasResponseBuilder(this.openApi).build(schema);
    System.out.println(OasResponseWriter.toString(response));
  }

}
