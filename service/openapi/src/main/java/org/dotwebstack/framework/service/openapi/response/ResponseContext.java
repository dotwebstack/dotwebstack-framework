package org.dotwebstack.framework.service.openapi.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.query.GraphQlField;

@Builder
@Getter
public class ResponseContext {

  private GraphQlField graphQlField;

  private List<ResponseTemplate> responses;

}
