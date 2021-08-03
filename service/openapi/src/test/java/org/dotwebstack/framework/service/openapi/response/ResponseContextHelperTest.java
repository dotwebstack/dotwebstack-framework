package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPANDED_PARAMS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResponseContextHelperTest {

  @Test
  void validate_isExpanded_withExpandedParameter() {
    assertTrue(ResponseContextHelper.isExpanded(ImmutableMap.of(X_DWS_EXPANDED_PARAMS, List.of("test")), "test"));
  }

  @Test
  void validate_isExpanded_withUnExpandedParameter() {
    assertFalse(ResponseContextHelper.isExpanded(Collections.emptyMap(), ""));
  }

}
