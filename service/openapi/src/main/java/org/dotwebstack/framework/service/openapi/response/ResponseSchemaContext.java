package org.dotwebstack.framework.service.openapi.response;

import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.dotwebstack.framework.core.query.GraphQlField;

@Getter
public class ResponseSchemaContext {

  private GraphQlField graphQlField;

  private List<ResponseTemplate> responses;

  private List<Parameter> parameters;

  private Map<String, String> dwsParameters;

  private RequestBodyContext requestBodyContext;

  public ResponseSchemaContext(GraphQlField graphQlField, List<ResponseTemplate> responses, List<Parameter> parameters,
      Map<String, String> dwsParameters, RequestBodyContext requestBodyContext) {
    this.graphQlField = graphQlField;
    this.responses = responses;
    this.parameters = parameters;
    this.dwsParameters = dwsParameters;
    this.requestBodyContext = requestBodyContext;
  }
}
