package org.dotwebstack.framework.backend.rdf4j.config;

import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Getter
@SuperBuilder
@Jacksonized
public class Rdf4jFieldConfiguration extends FieldConfiguration {

  private final List<JoinColumn> joinProperties;
}
