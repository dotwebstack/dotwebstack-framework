package org.dotwebstack.framework.backend.postgres;

import io.r2dbc.postgresql.client.SSLMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
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

  @NotNull
  private Pool pool = new Pool();

  @Getter
  @Setter
  static class Pool {

    @NotNull
    private Integer initialSize = 10;

    @NotNull
    private Integer maxSize = 100;

    @NotNull
    private Integer maxIdleTime = 30;

    @NotNull
    private Integer maxLifeTime = 120;
  }
}
