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

  private GraphQlField graphQlField;

  @Builder.Default
  private List<String> requiredFields = new ArrayList<>();

  @Builder.Default
  private List<ResponseTemplate> responses = new ArrayList<>();

  @Builder.Default
  private List<Parameter> parameters = new ArrayList<>();

  @Builder.Default
  private Map<String, String> dwsParameters = new HashMap<>();

  private RequestBodyContext requestBodyContext;

}
