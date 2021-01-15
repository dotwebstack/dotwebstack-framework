package org.dotwebstack.framework.backend.json;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Deprecated
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "dotwebstack.json")
public class JsonProperties {

}
