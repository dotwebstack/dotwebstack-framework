package org.dotwebstack.framework.backend.postgres;

import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
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

  @NotNull
  private String keyColumn;

  @NotNull
  private Map<String, TypeConfiguration> typeMapping = Map.of();

  @Getter
  @Setter
  public static class TypeConfiguration {

    @NotNull
    private String table;

    private Map<String, FieldConfiguration> fields;
  }

  @Getter
  @Setter
  public static class FieldConfiguration {

    private AssociationMapping oneToMany;

    private AssociationMapping manyToOne;
  }

  @Getter
  @Setter
  public static class AssociationMapping {

    private List<JoinColumn> joinColumns;

    private String mappedBy;
  }

  @Getter
  @Setter
  public static class JoinColumn {

    @NotNull
    private String name;

    @NotNull
    private String referencedColumnName;
  }
}
