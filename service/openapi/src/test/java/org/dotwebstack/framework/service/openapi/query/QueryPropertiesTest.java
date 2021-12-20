package org.dotwebstack.framework.service.openapi.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.swagger.v3.oas.models.Operation;
import java.util.Map;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
import org.junit.jupiter.api.Test;

class QueryPropertiesTest {

  @Test
  void fromOperation_createsPropertiesWithField_forStringExtension() {
    var operation = new Operation();
    operation.addExtension(OasConstants.X_DWS_QUERY, "brewery");

    var queryProperties = QueryProperties.fromOperation(operation);

    assertThat(queryProperties.getField(), is(equalTo("brewery")));
  }

  @Test
  void fromOperation_createsPropertiesWithField_forObjectExtension() {
    var operation = new Operation();
    operation.addExtension(OasConstants.X_DWS_QUERY, Map.of("field", "brewery"));

    var queryProperties = QueryProperties.fromOperation(operation);

    assertThat(queryProperties.getField(), is(equalTo("brewery")));
  }
}
