package org.dotwebstack.framework.core.config;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class DotWebStackConfiguration {

  private final Map<String, TypeConfiguration<? extends FieldConfiguration>> typeMapping;
}
