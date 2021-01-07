package org.dotwebstack.framework.backend.postgres.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.TypeConfiguration;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("postgres")
public class PostgresTypeConfiguration extends TypeConfiguration<PostgresFieldConfiguration> {

  @NotBlank
  private String table;
}
