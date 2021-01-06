package org.dotwebstack.framework.backend.rdf4j.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.dotwebstack.framework.core.config.TypeConfiguration;

@Getter
@SuperBuilder
@Jacksonized
@JsonTypeName("rdf4j")
public class Rdf4jTypeConfiguration extends TypeConfiguration<Rdf4jFieldConfiguration> {

  private final String nodeShape;
}
