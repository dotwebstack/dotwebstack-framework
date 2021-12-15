package org.dotwebstack.framework.service.openapi.jexl;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JexlExpression {

  @NonNull
  @Getter
  private String value;

  private String fallback;

  public Optional<String> getFallback() {
    return Optional.ofNullable(fallback);
  }
}
