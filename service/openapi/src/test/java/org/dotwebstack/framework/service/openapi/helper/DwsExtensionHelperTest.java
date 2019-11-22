package org.dotwebstack.framework.service.openapi.helper;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_PARAMETER_NAME;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_PARAMETER_VALUEEXPR;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.swagger.v3.oas.models.Operation;
import java.util.Collections;
import java.util.Map;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.junit.jupiter.api.Test;


public class DwsExtensionHelperTest {

  @Test
  public void getDwsQueryName_returnsQueryName_whenShortForm() {
    //Arrange
    Operation getShortForm = TestResources.openApi()
        .getPaths()
        .get("/query1")
        .getGet();

    //Act / Assert
    assertEquals(DwsExtensionHelper.getDwsQueryName(getShortForm), "query1");
  }

  @Test
  public void getDwsQueryName_returnsField_whenAdvancedForm() {
    //Arrange
    Operation getAdvancedForm = TestResources.openApi()
        .getPaths()
        .get("/query2")
        .getGet();

    //Act / Assert
    assertEquals(DwsExtensionHelper.getDwsQueryName(getAdvancedForm), "query2");
  }

  @Test
  public void getDwsQueryParameters_returnsParameters_whenSpecified() {
    //Arrange
    Operation getWithDwsParameters= TestResources.openApi()
        .getPaths()
        .get("/query5")
        .getGet();

    //Act
    Map<String, String> dwsParameters = DwsExtensionHelper.getDwsQueryParameters(getWithDwsParameters);

    //Assert
    assertEquals(dwsParameters.size(), 1);
    assertEquals(dwsParameters.get("someJexlParameter"), "someJexlExpression");
  }

  @Test
  public void getDwsQueryParameters_empty_whenNotSpecified() {
    //Arrange
    Operation getWithoutDwsParameters = TestResources.openApi()
        .getPaths()
        .get("/query2")
        .getGet();

    //Act / Assert
    assertEquals(DwsExtensionHelper.getDwsQueryParameters(getWithoutDwsParameters), Collections.emptyMap());
  }

  @Test
  public void getDwsQueryParameters_empty_whenShortForm() {
    //Arrange
    Operation getShortForm = TestResources.openApi()
        .getPaths()
        .get("/query1")
        .getGet();

    //Act / Assert
    assertEquals(DwsExtensionHelper.getDwsQueryParameters(getShortForm), Collections.emptyMap());
  }
}
