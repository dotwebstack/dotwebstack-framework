package org.dotwebstack.framework.core.datafetchers.paging;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "dotwebstack.paging")
@Configuration
@Getter
public class PagingConfiguration {

  @Value("${firstMaxValue:#{100}}")
  private int firstMaxValue;

  @Value("${offsetMaxValue:#{10000}}")
  private int offsetMaxValue;
}
