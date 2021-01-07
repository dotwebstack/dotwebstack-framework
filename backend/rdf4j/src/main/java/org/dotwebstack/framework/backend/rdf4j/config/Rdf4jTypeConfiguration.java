package org.dotwebstack.framework.backend.rdf4j.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.TypeConfiguration;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("rdf4j")
public class Rdf4jTypeConfiguration extends TypeConfiguration<Rdf4jFieldConfiguration> {

  private String nodeShape;
}
