package org.dotwebstack.framework.core.datafetchers.paging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "dotwebstack.paging")
@Configuration
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PagingConfiguration {

  @Value("${firstMaxValue:#{100}}")
  private int firstMaxValue;


  @Value("${firstDefaultValue:#{10}}")
  private int firstDefaultValue;

  @Value("${offsetMaxValue:#{10000}}")
  private int offsetMaxValue;

  @Value("${offsetDefaultValue:#{0}}")
  private int offsetDefaultValue;
}
