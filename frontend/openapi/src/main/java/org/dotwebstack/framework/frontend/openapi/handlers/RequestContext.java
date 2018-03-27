package org.dotwebstack.framework.frontend.openapi.handlers;

import com.atlassian.oai.validator.model.ApiOperation;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.informationproduct.InformationProduct;

@Getter
@RequiredArgsConstructor
public final class RequestContext {

  private final ApiOperation apiOperation;

  private final InformationProduct informationProduct;

  private final Map<String, String> parameters;

  private final String baseUri;

  public Map<String, String> getParameters() {
    return Collections.unmodifiableMap(parameters);
  }

  public void addParameter(@NonNull String key, String value) {
    parameters.put(key, value);
  }

}
