package org.dotwebstack.framework.backend.json.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.dotwebstack.framework.core.config.TypeConfiguration;

@SuperBuilder
@Jacksonized
@JsonTypeName("json")
public class JsonTypeConfiguration extends TypeConfiguration<JsonFieldConfiguration> {

}
