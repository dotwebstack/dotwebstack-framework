package org.dotwebstack.framework.backend.json.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.TypeConfiguration;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("json")
public class JsonTypeConfiguration extends TypeConfiguration<JsonFieldConfiguration> {

}
