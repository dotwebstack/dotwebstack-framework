package org.dotwebstack.framework.ext.spatial;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import java.util.regex.Pattern;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.config.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "spatial")
@PropertySource(value = "classpath:config/${dotwebstack.config:dotwebstack.yaml}",
    factory = YamlPropertySourceFactory.class)
@Getter
public class SpatialConfigurationProperties {

  private final Pattern crsPattern = Pattern.compile("EPSG:\\d{4,6}");

  public static final String EPSG_PREFIX = "EPSG:";

  private String sourceCrs;

  public void setSourceCrs(String sourceCrs) {
    if (StringUtils.isEmpty(sourceCrs)) {
      return;
    }

    if (!crsPattern.matcher(sourceCrs)
        .matches()) {
      throw invalidConfigurationException("Spatial source crs '{}' is not a valid ESPG code.", sourceCrs);
    }
    this.sourceCrs = sourceCrs;
  }
}
