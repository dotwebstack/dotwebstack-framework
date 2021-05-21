package org.dotwebstack.framework.backend.postgres;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "dotwebstack.postgres")
public class PostgresProperties {

  private String host = "localhost";

  private int port = 5432;

  private String username = "postgres";

  private String password = "postgres";

  private String database = "postgres";
}
