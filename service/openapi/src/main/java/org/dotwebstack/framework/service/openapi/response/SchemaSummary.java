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

  private final String type;

  private final boolean required;

  private final boolean nillable;

  private final boolean isTransient;

  private final String dwsType;

  private final Schema<?> schema;

  private final String ref;

  @Builder.Default
  @Setter
  private List<ResponseObject> children = new ArrayList<>();

  @Builder.Default
  @Setter
  private List<ResponseObject> composedOf = new ArrayList<>();

  @Builder.Default
  @Setter
  private List<ResponseObject> items = new ArrayList<>();

  private final Map<String, String> dwsExpr;

  public boolean hasExtension(String name){
    return getSchema()!=null&& getSchema().getExtensions() != null && getSchema().getExtensions()
        .containsKey(name);
  }

}
