package org.dotwebstack.framework.service.openapi.helper;

import static graphql.Assert.assertTrue;
import static java.util.Collections.emptyMap;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsQueryName;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsQueryParameters;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsQuerySettings;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.hasDwsExtensionWithValue;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.isDwsOperation;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.isTransient;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.supportsDwsType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  void getDwsRequiredFields_returnsEmptyList_withoutRequiredFields() {
    Operation operation = TestResources.openApi()
        .getPaths()
        .get("/query8")
        .getGet();

    assertEquals(List.of(), getDwsQuerySettings(operation).getRequiredFields());
  }

  @Test
  void getDwsRequiredFields_returnsFields_whenPresent() {
    Operation operation = TestResources.openApi()
        .getPaths()
        .get("/query9")
        .getGet();

    assertEquals(List.of("field1"), getDwsQuerySettings(operation).getRequiredFields());
  }

  @Test
  void getDwsRequiredFields_returnsEmptyList_withoutDwsQuery() {
    Operation operation = TestResources.openApi()
        .getPaths()
        .get("/query10")
        .getGet();

    assertEquals(List.of(), getDwsQuerySettings(operation).getRequiredFields());
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
  void isDwsOperation_returnsFalse_withoutDwsOperation() {
    Operation operation = TestResources.openApi()
        .getPaths()
        .get("/query12")
        .getGet();

    assertTrue(isDwsOperation(operation));
  }

  @Test
  void isDwsOperation_returnsTrue_withDwsOperationFalse() {
    Operation operation = TestResources.openApi()
        .getPaths()
        .get("/query13")
        .getGet();

    assertFalse(isDwsOperation(operation));
  }
}
