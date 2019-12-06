package org.dotwebstack.framework.service.openapi;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("dotwebstack.openapi")
public class OpenApiProperties {

  private List<String> xdwsStringTypes = Collections.emptyList();
}
