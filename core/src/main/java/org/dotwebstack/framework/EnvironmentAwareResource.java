package org.dotwebstack.framework;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvironmentAwareResource {

  private static final Logger LOG = LoggerFactory.getLogger(EnvironmentAwareResource.class);

  private static final String REGEX_ENV_VAR = "\\$\\{?([A-Za-z0-9_]+)\\}?";

  private InputStream inputStream;

  public EnvironmentAwareResource(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public InputStream getInputStream() throws IOException {
    String result = IOUtils.toString(inputStream, "UTF-8");

    return new ByteArrayInputStream(parsePattern(result, REGEX_ENV_VAR).getBytes());
  }

  private String parsePattern(String text, String regex) {
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(text);

    while (matcher.find()) {
      String foundEnvVariables = matcher.group(1);
      String envValue = System.getenv().get(foundEnvVariables);
      if (envValue == null) {
        LOG.error(String.format("Env variable {} found but not defined", foundEnvVariables));
        continue;
      }

      envValue = envValue.replace("\\", "\\\\");
      Pattern subexpr = Pattern.compile(Pattern.quote(matcher.group(0)));
      text = subexpr.matcher(text).replaceAll(envValue);
    }

    return text;
  }

}
