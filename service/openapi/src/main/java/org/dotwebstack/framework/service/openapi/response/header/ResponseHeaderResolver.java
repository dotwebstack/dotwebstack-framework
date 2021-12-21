package org.dotwebstack.framework.service.openapi.response.header;

import java.util.function.Consumer;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.springframework.http.HttpHeaders;

public interface ResponseHeaderResolver {

  Consumer<HttpHeaders> resolve(OperationRequest operationRequest, Object data);
}
