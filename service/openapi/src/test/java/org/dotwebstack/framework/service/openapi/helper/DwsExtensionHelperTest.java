package org.dotwebstack.framework.service.openapi.helper;

import static java.util.Collections.emptyMap;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsQueryName;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getDwsQueryParameters;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.swagger.v3.oas.models.Operation;
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
    assertEquals(getDwsQueryName(getShortForm), Optional.of("query1"));
  }

  @Test
  void getDwsQueryName_returnsField_whenAdvancedForm() {
    // Arrange
    Operation getAdvancedForm = TestResources.openApi()
        .getPaths()
        .get("/query2")
        .getGet();

    // Act / Assert
    assertEquals(getDwsQueryName(getAdvancedForm), Optional.of("query2"));
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
  public void getDwsQueryParameters_empty_whenNotSpecified() {
    // Arrange
    Operation getWithoutDwsParameters = TestResources.openApi()
        .getPaths()
        .get("/query2")
        .getGet();

    // Act / Assert
    assertEquals(emptyMap(), getDwsQueryParameters(getWithoutDwsParameters));
  }

  @Test
  public void getDwsQueryParameters_empty_whenShortForm() {
    // Arrange
    Operation getShortForm = TestResources.openApi()
        .getPaths()
        .get("/query1")
        .getGet();

    // Act / Assert
    assertEquals(emptyMap(), getDwsQueryParameters(getShortForm));
  }
}
