package org.dotwebstack.framework.service.openapi.helper;

import static graphql.Assert.assertTrue;
import static java.util.Collections.emptyMap;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsQueryName;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsQueryParameters;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.hasDwsExtensionWithValue;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.isTransient;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.supportsDwsType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.junit.jupiter.api.Test;


class DwsExtensionHelperTest {

  @Test
  void getDwsQueryName_returnsQueryName_whenShortForm() {
    Operation getShortForm = TestResources.openApi()
        .getPaths()
        .get("/query1")
        .getGet();

    assertEquals(Optional.of("query1"), getDwsQueryName(getShortForm));
  }

  @Test
  void getDwsQueryName_returnsField_whenAdvancedForm() {
    Operation getAdvancedForm = TestResources.openApi()
        .getPaths()
        .get("/query2")
        .getGet();

    assertEquals(Optional.of("query2"), getDwsQueryName(getAdvancedForm));
  }

  @Test
  void getDwsQueryName_returnsEmpty_withoutDwsQuery() {
    Operation getAdvancedForm = TestResources.openApi()
        .getPaths()
        .get("/query10")
        .getGet();

    assertEquals(Optional.empty(), getDwsQueryName(getAdvancedForm));
  }

  @Test
  void getDwsQueryParameters_returnsParameters_whenSpecified() {
    Operation getWithDwsParameters = TestResources.openApi()
        .getPaths()
        .get("/query5")
        .getGet();

    Map<String, String> dwsParameters = getDwsQueryParameters(getWithDwsParameters);

    assertEquals(1, dwsParameters.size());
    assertEquals("someJexlExpression", dwsParameters.get("someJexlParameter"));
  }

  @Test
  void getDwsQueryParameters_empty_whenNotSpecified() {
    Operation getWithoutDwsParameters = TestResources.openApi()
        .getPaths()
        .get("/query2")
        .getGet();

    assertEquals(emptyMap(), getDwsQueryParameters(getWithoutDwsParameters));
  }

  @Test
  void getDwsQueryParameters_empty_whenShortForm() {
    Operation getShortForm = TestResources.openApi()
        .getPaths()
        .get("/query1")
        .getGet();

    assertEquals(emptyMap(), getDwsQueryParameters(getShortForm));
  }

  @Test
  void getDwsQueryParameters_empty_withoutDwsQuery() {
    Operation getShortForm = TestResources.openApi()
        .getPaths()
        .get("/query10")
        .getGet();

    assertEquals(emptyMap(), getDwsQueryParameters(getShortForm));
  }

  @Test
  void isTransient_returnsFalse_withObject1() {
    Schema<?> schema = TestResources.openApi()
        .getComponents()
        .getSchemas()
        .get("Object1");

    assertFalse(isTransient(schema));
  }

  @Test
  void supportsDwsType_returnsFalse_forRequestBodyWithoutDwsType() {
    RequestBody requestBody = TestResources.openApi()
        .getPaths()
        .get("/query1")
        .getPost()
        .getRequestBody();

    assertFalse(supportsDwsType(requestBody, "specialtype"));
  }

  @Test
  void supportsDwsType_returnsTrue_forRequestBodyWithDwsType() {
    RequestBody requestBody = TestResources.openApi()
        .getPaths()
        .get("/query11")
        .getPost()
        .getRequestBody();

    assertTrue(supportsDwsType(requestBody, "specialtype"));
  }

  @Test
  void supportsDwsType_returnsFalse_forRequestBodyWithDwsTypeMismatch() {
    RequestBody requestBody = TestResources.openApi()
        .getPaths()
        .get("/query11")
        .getPost()
        .getRequestBody();

    assertFalse(supportsDwsType(requestBody, "anothertype"));
  }

  @Test
  void supportsDwsType_returnsTrue_forParameterWithoutDwsType() {
    Parameter parameter = TestResources.openApi()
        .getPaths()
        .get("/query3/{query3_param1}")
        .getGet()
        .getParameters()
        .get(0);

    assertFalse(supportsDwsType(parameter, "specialtype"));
  }

  @Test
  void supportsDwsType_returnsTrue_forParameterWithDwsType() {
    Parameter parameter = TestResources.openApi()
        .getPaths()
        .get("/query6")
        .getGet()
        .getParameters()
        .get(0);

    assertTrue(supportsDwsType(parameter, "specialtype"));
  }

  @Test
  void supportsDwsType_returnsFalse_forParameterWithDwsTypeMismatch() {
    Parameter parameter = TestResources.openApi()
        .getPaths()
        .get("/query6")
        .getGet()
        .getParameters()
        .get(0);

    assertFalse(supportsDwsType(parameter, "anothertype"));
  }

  @Test
  void hasDwsExtensionWithValue_returnsTrue_forParameterWithDwsType() {
    Parameter parameter = TestResources.openApi()
        .getPaths()
        .get("/query6")
        .getGet()
        .getParameters()
        .get(0);

    assertTrue(hasDwsExtensionWithValue(parameter, OasConstants.X_DWS_TYPE, "specialtype"));
  }

  @Test
  void getJexlExpression_givenStringExpression_returnsCorrectly() {
    var expression = "foo";
    var context = "context";
    UnaryOperator<String> expressionValueAdapter = foo -> foo + "bar";

    var optionalJexlExpression = DwsExtensionHelper.getJexlExpression(expression, context, expressionValueAdapter);

    assertThat(optionalJexlExpression.isPresent(), is(true));

    var jexlExpression = optionalJexlExpression.get();
    assertThat(jexlExpression.getValue(), is("foobar"));
    assertThat(jexlExpression.getFallback(), is(Optional.empty()));
  }

  @Test
  void getJexlExpression_givenExpressionObject_returnsCorrectly() {
    var expression = Map.<String, Object>of("value", "foo", "fallback", "baz");
    var context = "context";
    UnaryOperator<String> expressionValueAdapter = foo -> foo + "bar";

    var optionalJexlExpression = DwsExtensionHelper.getJexlExpression(expression, context, expressionValueAdapter);

    assertThat(optionalJexlExpression.isPresent(), is(true));

    var jexlExpression = optionalJexlExpression.get();
    assertThat(jexlExpression.getValue(), is("foobar"));
    assertThat(jexlExpression.getFallback(), is(Optional.of("baz")));
  }

  @Test
  void getJexlExpression_givenStringExpressionWithUnsupportedValueType_throwsException() {
    var expression = List.of();
    var context = "context";
    UnaryOperator<String> expressionValueAdapter = foo -> foo + "bar";

    var exception = assertThrows(InvalidConfigurationException.class,
        () -> DwsExtensionHelper.getJexlExpression(expression, context, expressionValueAdapter));

    assertThat(exception.getMessage(), is("Unsupported value [] for x-dws-expr found in context"));
  }

  @Test
  void getJexlExpression_givenExpressionObjectWithUnsupportedValueType_throwsException() {
    var expression = Map.<String, Object>of("value", List.of(), "fallback", "baz");
    var context = "context";
    UnaryOperator<String> expressionValueAdapter = foo -> foo + "bar";

    var exception = assertThrows(InvalidConfigurationException.class,
        () -> DwsExtensionHelper.getJexlExpression(expression, context, expressionValueAdapter));

    assertThat(exception.getMessage(), is("Unsupported value [] for x-dws-expr.value found in context"));
  }

  @Test
  void getJexlExpression_givenExpressionObjectWithUnsupportedFallbackType_throwsException() {
    var expression = Map.<String, Object>of("value", "foo", "fallback", List.of());
    var context = "context";
    UnaryOperator<String> expressionValueAdapter = foo -> foo + "bar";

    var exception = assertThrows(InvalidConfigurationException.class,
        () -> DwsExtensionHelper.getJexlExpression(expression, context, expressionValueAdapter));

    assertThat(exception.getMessage(), is("Unsupported value [] for x-dws-expr.fallback found in context"));
  }
}
