package org.dotwebstack.framework.service.openapi.response;

import io.swagger.v3.oas.models.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ResponseSchema {

  private String type;

  private boolean required;

  private boolean nillable;

  private boolean isEnvelope;

  private String dwsType;

  private Schema<?> schema;

  @Builder.Default
  private List<ResponseObject> children = new ArrayList<>();

  @Builder.Default
  private List<ResponseObject> items = new ArrayList<>();

  private String dwsExpr;

}
