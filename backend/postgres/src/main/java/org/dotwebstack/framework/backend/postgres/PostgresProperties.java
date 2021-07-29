package org.dotwebstack.framework.backend.postgres;

import io.r2dbc.postgresql.client.SSLMode;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "dotwebstack.postgres")
public class PostgresProperties {

  @NotBlank
  private String host = "localhost";

  @NotNull
  private int port = 5432;

  @NotBlank
  private String username = "postgres";

  @NotBlank
  private String password = "postgres";

  @NotBlank
  private String database = "postgres";

  @NotNull
  private SSLMode sslMode = SSLMode.DISABLE;

  @NotNull
  private Map<String, String> options;
}
