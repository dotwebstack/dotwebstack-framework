package org.dotwebstack.framework;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

public class EnvironmentAwareResource {

  private static final Logger LOG = LoggerFactory.getLogger(EnvironmentAwareResource.class);

  private static final String REGEX_ENV_VAR = "\\%\\{?([A-Za-z0-9_. -]+)\\}?";

  private final InputStream inputStream;

  private final Environment environment;

  public EnvironmentAwareResource(@NonNull InputStream inputStream,
      @NonNull Environment environment) {
    this.inputStream = inputStream;
    this.environment = environment;
  }

  public InputStream getInputStream() throws IOException {
    String result = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));

    return new ByteArrayInputStream(parsePattern(result, REGEX_ENV_VAR).getBytes());
  }

  private String parsePattern(String text, String regex) {
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(text);
    String replacedText = text;

    while (matcher.find()) {
      String foundEnvVariables = matcher.group(1);
      String envValue = environment.getProperty(foundEnvVariables);
      if (envValue == null) {
        LOG.error("Env variable {} found but not defined", foundEnvVariables);
        continue;
      }

      envValue = envValue.replace("\\", "\\\\");
      Pattern subexpr = Pattern.compile(Pattern.quote(matcher.group(0)));
      replacedText = subexpr.matcher(replacedText).replaceAll(envValue);
    }

    return replacedText;
  }

}
