package org.dotwebstack.framework.service.openapi.response.header;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResponseHeaderResolverFactoryTest {

  @Mock
  private EnvironmentProperties environmentProperties;

  @Mock
  private JexlEngine jexlEngine;

  @Mock
  private OperationRequest operationRequest;

  @Test
  void create_givenAllParams_createsResponseHeaderResolver() {
    ResponseHeaderResolverFactory responseHeaderResolverFactory =
        new ResponseHeaderResolverFactory(environmentProperties, jexlEngine);

    assertDoesNotThrow(() -> responseHeaderResolverFactory.create(operationRequest));
  }
}
