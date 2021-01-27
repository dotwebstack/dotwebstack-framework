package org.dotwebstack.framework.backend.rdf4j.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;


@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("rdf4j")
public class Rdf4jTypeConfiguration extends AbstractTypeConfiguration<Rdf4jFieldConfiguration> {

  private String nodeShape;

}
