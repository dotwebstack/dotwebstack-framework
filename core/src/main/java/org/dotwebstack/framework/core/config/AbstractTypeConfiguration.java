package org.dotwebstack.framework.core.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "backend")
public abstract class AbstractTypeConfiguration<T extends AbstractFieldConfiguration> implements TypeConfiguration<T> {

  @NotNull
  @Valid
  @Size(min = 1, max = 1)
  private List<KeyConfiguration> keys;

  @NotNull
  @Valid
  @Size(min = 1)
  private Map<String, T> fields;

  public void init(TypeDefinitionRegistry typeDefinitionRegistry) {}
}
