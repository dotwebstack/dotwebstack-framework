package org.dotwebstack.framework.service.openapi.response;

import java.util.ArrayList;
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

  private String dwsType;

  @Builder.Default
  private List<ResponseObject> children = new ArrayList<>();

  @Builder.Default
  private List<ResponseObject> items = new ArrayList<>();

  private String dwsTemplate;

}
