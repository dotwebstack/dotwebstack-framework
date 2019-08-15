package org.dotwebstack.framework.service.openapi.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseObject {

  private String identifier;

  private String type;

  private boolean required;

  private boolean nillable;

  private boolean isEnvelope;

  private List<ResponseObject> children;

  private List<ResponseObject> items;

  private String dwsTemplate;

}
