package org.dotwebstack.framework.service.openapi.response;

import io.swagger.v3.oas.models.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;

@Builder
@Getter
public class SchemaSummary {

  private String type;

  private boolean required;

  private boolean nillable;

  private boolean isTransient;

  private String dwsType;

  private Schema<?> schema;

  private String ref;

  @Builder.Default
  @Setter
  private List<ResponseObject> children = new ArrayList<>();

  @Builder.Default
  @Setter
  private List<ResponseObject> composedOf = new ArrayList<>();

  @Builder.Default
  @Setter
  private List<ResponseObject> items = new ArrayList<>();

  private Map<String, String> dwsExpr;

  public boolean hasIncludeCondition() {
    return getSchema() != null && getSchema().getExtensions() != null && getSchema().getExtensions()
        .containsKey(OasConstants.X_DWS_INCLUDE);
  }

}
