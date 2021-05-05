package org.dotwebstack.framework.core.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;

@Data
@JsonTypeName("postgres")
public class TestTypeConfiguration extends AbstractTypeConfiguration<TestFieldConfiguration> {

  @NotBlank
  private String table;

  @Override
  public KeyCondition getKeyCondition(DataFetchingEnvironment environment) {
    return null;
  }

  @Override
  public KeyCondition getKeyCondition(String fieldName, Map<String, Object> source) {
    return null;
  }

  @Override
  public KeyCondition invertKeyCondition(MappedByKeyCondition mappedByKeyCondition, Map<String, Object> source) {
    return null;
  }
}
