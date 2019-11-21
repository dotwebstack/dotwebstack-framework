package org.dotwebstack.framework.service.openapi.helper;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_PARAMETERS;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_PARAMETER_NAME;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY_PARAMETER_VALUEEXPR;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.swagger.v3.oas.models.Operation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.junit.jupiter.api.Test;


public class DwsExtensionHelperTest {

  @Test
  public void getDwsQueryName_returnsQueryName_whenShortForm() {
    Operation getShortForm = TestResources.openApi()
        .getPaths()
        .get("/query1")
        .getGet();
    assertEquals(DwsExtensionHelper.getDwsQueryName(getShortForm), "query1");
  }

  @Test
  public void getDwsQueryName_returnsField_whenAdvancedForm() {
    Operation getAdvancedForm = TestResources.openApi()
        .getPaths()
        .get("/query2")
        .getGet();
    assertEquals(DwsExtensionHelper.getDwsQueryName(getAdvancedForm), "query2");
  }

  @Test
  public void getDwsQueryParameters_retursParameters_whenSpecified() {
    Object extension = TestResources.openApi()
        .getPaths()
        .get("/query5")
        .getGet()
        .getExtensions()
        .get(X_DWS_QUERY);
    assertThat(extension, instanceOf(Map.class));
    List<?> myParameters = (List<?>) ((Map) extension).get(X_DWS_QUERY_PARAMETERS);
    assertEquals(myParameters.size(), 1);
    assertEquals(((Map) myParameters.get(0)).get(X_DWS_QUERY_PARAMETER_NAME), "someJexlParameter");
    assertEquals(((Map) myParameters.get(0)).get(X_DWS_QUERY_PARAMETER_VALUEEXPR), "someJexlExpression");
  }

  @Test
  public void getDwsQueryParameters_empty_whenNotSpecified() {
    Operation getShortForm = TestResources.openApi()
        .getPaths()
        .get("/query2")
        .getGet();
    assertEquals(DwsExtensionHelper.getDwsQueryParameters(getShortForm), Collections.emptyMap());
  }

  @Test
  public void getDwsQueryParameters_empty_whenShortForm() {
    Operation getShortForm = TestResources.openApi()
        .getPaths()
        .get("/query1")
        .getGet();
    assertEquals(DwsExtensionHelper.getDwsQueryParameters(getShortForm), Collections.emptyMap());
  }
}
