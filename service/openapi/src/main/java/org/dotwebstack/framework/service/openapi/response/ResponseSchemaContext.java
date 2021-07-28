package org.dotwebstack.framework.service.openapi.response;

import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.query.GraphQlField;

@Getter
@Builder
public class ResponseSchemaContext {

  @Builder.Default
  private final List<String> requiredFields = new ArrayList<>();

  @Builder.Default
  private final List<ResponseTemplate> responses = new ArrayList<>();

  @Builder.Default
  private final List<Parameter> parameters = new ArrayList<>();

  private final String dwsQuery;

  @Builder.Default
  private final Map<String, String> dwsParameters = new HashMap<>();

  private final RequestBodyContext requestBodyContext;

}
