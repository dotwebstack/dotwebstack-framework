package org.dotwebstack.framework.service.openapi.response;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.List;
import lombok.Getter;
import org.dotwebstack.framework.core.query.GraphQlField;

@Getter
public class ResponseContext {

  private GraphQlField graphQlField;

  private List<ResponseTemplate> responses;

  private List<Parameter> parameters;

  private RequestBody requestBody;

  public ResponseContext(GraphQlField graphQlField, List<ResponseTemplate> responses, List<Parameter> parameters,
      RequestBody requestBody) {
    this.graphQlField = graphQlField;
    this.responses = responses;
    this.parameters = parameters;
    this.requestBody = requestBody;
  }
}
