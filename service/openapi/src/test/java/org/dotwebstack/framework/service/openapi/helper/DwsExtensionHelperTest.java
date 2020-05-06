package org.dotwebstack.framework.service.openapi.helper;

import static graphql.Assert.assertTrue;
import static java.util.Collections.emptyMap;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsQueryName;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsQueryParameters;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsRequiredFields;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.isEnvelope;
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
    // Arrange
    Operation getShortForm = TestResources.openApi()
        .getPaths()
        .get("/query1")
        .getGet();

    // Act / Assert
    assertEquals(Optional.of("query1"), getDwsQueryName(getShortForm));
  }

  @Test
  void getDwsQueryName_returnsField_whenAdvancedForm() {
    // Arrange
    Operation getAdvancedForm = TestResources.openApi()
        .getPaths()
        .get("/query2")
        .getGet();

    // Act / Assert
    assertEquals(Optional.of("query2"), getDwsQueryName(getAdvancedForm));
  }

  @Test
  void getDwsQueryName_returnsEmpty_withoutDwsQuery() {
    // Arrange
    Operation getAdvancedForm = TestResources.openApi()
        .getPaths()
        .get("/query10")
        .getGet();

    // Act / Assert
    assertEquals(Optional.empty(), getDwsQueryName(getAdvancedForm));
  }

  @Test
  void getDwsQueryParameters_returnsParameters_whenSpecified() {
    // Arrange
    Operation getWithDwsParameters = TestResources.openApi()
        .getPaths()
        .get("/query5")
        .getGet();

    // Act
    Map<String, String> dwsParameters = getDwsQueryParameters(getWithDwsParameters);

    // Assert
    assertEquals(dwsParameters.size(), 1);
    assertEquals(dwsParameters.get("someJexlParameter"), "someJexlExpression");
  }

  @Test
  void getDwsQueryParameters_empty_whenNotSpecified() {
    // Arrange
    Operation getWithoutDwsParameters = TestResources.openApi()
        .getPaths()
        .get("/query2")
        .getGet();

    // Act / Assert
    assertEquals(emptyMap(), getDwsQueryParameters(getWithoutDwsParameters));
  }

  @Test
  void getDwsQueryParameters_empty_whenShortForm() {
    // Arrange
    Operation getShortForm = TestResources.openApi()
        .getPaths()
        .get("/query1")
        .getGet();

    // Act / Assert
    assertEquals(emptyMap(), getDwsQueryParameters(getShortForm));
  }

  @Test
  void getDwsQueryParameters_empty_withoutDwsQuery() {
    // Arrange
    Operation getShortForm = TestResources.openApi()
        .getPaths()
        .get("/query10")
        .getGet();

    // Act / Assert
    assertEquals(emptyMap(), getDwsQueryParameters(getShortForm));
  }

  @Test
  void getDwsRequiredFields_returnsEmptyList_withoutRequiredFields() {
    // Arrange
    Operation operation = TestResources.openApi()
        .getPaths()
        .get("/query8")
        .getGet();

    // Act / Assert
    assertEquals(List.of(), getDwsRequiredFields(operation));
  }

  @Test
  void getDwsRequiredFields_returnsFields_whenPresent() {
    // Arrange
    Operation operation = TestResources.openApi()
        .getPaths()
        .get("/query9")
        .getGet();

    // Act / Assert
    assertEquals(List.of("field1"), getDwsRequiredFields(operation));
  }

  @Test
  void getDwsRequiredFields_returnsEmptyList_withoutDwsQuery() {
    // Arrange
    Operation operation = TestResources.openApi()
        .getPaths()
        .get("/query10")
        .getGet();

    // Act / Assert
    assertEquals(List.of(), getDwsRequiredFields(operation));
  }

  @Test
  void isEnvelope_returnsFalse_withObject1() {
    // Arrange
    Schema<?> schema = TestResources.openApi()
        .getComponents()
        .getSchemas()
        .get("Object1");

    // Act / Assert
    assertFalse(isEnvelope(schema));
  }

  @Test
  void supportsDwsType_returnsFalse_forRequestBodyWithoutDwsType() {
    // Arrange
    RequestBody requestBody = TestResources.openApi()
        .getPaths()
        .get("/query1")
        .getPost()
        .getRequestBody();

    // Act / Assert
    assertFalse(supportsDwsType(requestBody, "specialtype"));
  }

  @Test
  void supportsDwsType_returnsTrue_forRequestBodyWithDwsType() {
    // Arrange
    RequestBody requestBody = TestResources.openApi()
        .getPaths()
        .get("/query11")
        .getPost()
        .getRequestBody();

    // Act / Assert
    assertTrue(supportsDwsType(requestBody, "specialtype"));
  }

  @Test
  void supportsDwsType_returnsTrue_forParameterWithoutDwsType() {
    // Arrange
    Parameter parameter = TestResources.openApi()
        .getPaths()
        .get("/query3/{query3_param1}")
        .getGet()
        .getParameters()
        .get(0);

    // Act / Assert
    assertFalse(supportsDwsType(parameter, "specialtype"));
  }

  @Test
  void supportsDwsType_returnsTrue_forParameterWithDwsType() {
    // Arrange
    Parameter parameter = TestResources.openApi()
        .getPaths()
        .get("/query6")
        .getGet()
        .getParameters()
        .get(0);

    // Act / Assert
    assertTrue(supportsDwsType(parameter, "specialtype"));
  }
}
