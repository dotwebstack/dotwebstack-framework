package org.dotwebstack.framework.service.openapi.response;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;

@Builder
@Getter
@Setter
public class ResponseTemplate {

  private int responseCode;

  private MediaType mediaType;

  private ResponseObject responseObject;

  private Map<String, ResponseHeader> responseHeaders;

  private boolean isDefault;

  public boolean isApplicable(int bottom, int top) {
    return this.responseCode >= bottom && this.responseCode <= top;
  }

}
