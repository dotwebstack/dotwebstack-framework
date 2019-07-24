package org.dotwebstack.framework.service.openapi.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseTemplate {

  private int responseCode;

  private String mediaType;

  private ResponseFieldTemplate responseObject;

  public boolean isApplicable(int bottom, int top, String mediaType) {
    return this.responseCode >= bottom && this.responseCode <= top && this.mediaType.equals(mediaType);
  }
}
