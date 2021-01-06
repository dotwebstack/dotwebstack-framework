package org.dotwebstack.framework.backend.rdf4j.config;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class JoinColumn {

  private final String name;

  private final String referencedField;
}
