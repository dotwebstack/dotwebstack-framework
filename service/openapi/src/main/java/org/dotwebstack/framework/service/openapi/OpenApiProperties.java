package org.dotwebstack.framework.service.openapi;

import java.util.Collections;
import java.util.List;
import javax.validation.constraints.Pattern;
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
  private static final String SUB_DELIMS = "(?:[!$&'()*+,;=])";

  private static final String UNRESERVED = "(?:[A-Za-z0-9-._~])";

  private static final String PCT_ENCODED = "(?:%(?:[0-9ABCDEF]){2})";

  private static final String PCHAR = "(?:" + UNRESERVED + "|" + PCT_ENCODED + "|" + SUB_DELIMS + "|[:@])";

  private static final String SEGMENT = "(?:" + PCHAR + "*)";

  private static final String PATH_ABEMPTY = "(?:(\\/" + SEGMENT + ")*)";

  private static final String VALID_PATH_REGEX = "^" + PATH_ABEMPTY + "$";

  @Pattern(regexp = VALID_PATH_REGEX)
  private String apiDocPublicationPath = "";

  private List<String> xdwsStringTypes = Collections.emptyList();

  private DateFormatProperties dateproperties;

  @Getter
  @Setter
  public static class DateFormatProperties {

    private String dateformat;

    private String datetimeformat;

    private String timezone;

  }

}
