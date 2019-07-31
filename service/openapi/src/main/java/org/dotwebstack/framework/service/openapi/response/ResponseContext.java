package org.dotwebstack.framework.service.openapi.response;

import java.util.List;
import lombok.Getter;
import org.dotwebstack.framework.core.query.GraphQlField;

@Getter
public class ResponseContext {

  private GraphQlField graphQlField;

  private List<ResponseTemplate> responses;

  public ResponseContext(GraphQlField graphQlField, List<ResponseTemplate> responses) {
    this.graphQlField = graphQlField;
    this.responses = responses;
  }
}
