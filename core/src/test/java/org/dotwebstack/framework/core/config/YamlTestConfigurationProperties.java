package org.dotwebstack.framework.core.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "yaml")
@PropertySource(value = "classpath:config/yamlpropertysourcefactory.yml", factory = YamlPropertySourceFactory.class)
@Getter
@Setter
public class YamlTestConfigurationProperties {
  private String name;

  private List<String> aliases;
}
