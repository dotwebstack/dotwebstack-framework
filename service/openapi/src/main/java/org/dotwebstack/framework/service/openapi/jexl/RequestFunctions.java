package org.dotwebstack.framework.service.openapi.jexl;

import lombok.NonNull;
import org.dotwebstack.framework.core.jexl.JexlFunction;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

@Component
public class RequestFunctions implements JexlFunction {

  private static final String NAMESPACE = "req";

  @Override
  public String getNamespace() {
    return NAMESPACE;
  }

  public boolean accepts(@NonNull String mediaType, @NonNull ServerRequest serverRequest) {
    var acceptableMediaTypes = serverRequest.headers()
        .accept();

    return acceptableMediaTypes.contains(MediaType.valueOf(mediaType));
  }
}
