package org.dotwebstack.framework.core.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "backend")
public abstract class TypeConfiguration<T extends FieldConfiguration> {

  private final List<String> keyFields;

  private final Map<String, T> fields;
}
