package org.dotwebstack.framework.backend.postgres.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.dotwebstack.framework.core.config.TypeConfiguration;

@Getter
@SuperBuilder
@Jacksonized
@JsonTypeName("postgres")
public class PostgresTypeConfiguration extends TypeConfiguration<PostgresFieldConfiguration> {

  private final String table;
}
