package org.dotwebstack.framework.service.openapi;

import jakarta.validation.constraints.Pattern;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties("dotwebstack.openapi")
public class OpenApiProperties {

  // source: https://tools.ietf.org/html/rfc3986#appendix-A
  @SuppressWarnings("java:S5998")
  private static final String PATH_ABEMPTY =
      "(?:(\\/(?:(?:(?:[A-Za-z0-9-._~])|(?:%(?:[0-9ABCDEF]){2})|(?:[!$&'()*+,;=])|[:@])*))*)";

  private static final String VALID_PATH_REGEX = "^" + PATH_ABEMPTY + "$";

  @Pattern(regexp = VALID_PATH_REGEX)
  private String apiDocPublicationPath = "";

  private List<String> xdwsStringTypes = Collections.emptyList();

  private boolean serializeNull = true;

  private CorsProperties cors = new CorsProperties();

  private DateFormatProperties dateproperties;

  private SpatialProperties spatial;

  @Getter
  @Setter
  public static class CorsProperties {

    private Boolean enabled = false;

    private Boolean allowCredentials = false;
  }

  @Getter
  @Setter
  public static class DateFormatProperties {

    private String dateformat;

    private String datetimeformat;

    private String timezone;
  }

  @Getter
  @Setter
  public static class SpatialProperties {

    private SridParameterProperties sridParameter;
  }

  @Getter
  @Setter
  public static class SridParameterProperties {

    private String name;

    private Map<String, Integer> valueMap;
  }
}
