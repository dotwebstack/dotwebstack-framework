package org.dotwebstack.framework.service.openapi.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseFieldTemplate {

  private String identifier;

  private String type;

  private boolean required;

  private boolean nillable;

  private List<ResponseFieldTemplate> children;

  private List<ResponseFieldTemplate> items;

}
