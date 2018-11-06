package org.dotwebstack.framework.frontend.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

@Slf4j
@RequiredArgsConstructor
public class SpecEnvironmentResolver {

  private final Environment environment;
  private static final String REGEX_ENV_VAR = "\\%\\{?([A-Za-z0-9_. -]+)\\}?";
  private static final Pattern PATTERN = Pattern.compile(REGEX_ENV_VAR);

  public OpenAPI resolve(OpenAPI openApi) {
    openApi.getServers().forEach(this::replace);
    return openApi;
  }

  private void replace(Server server) {
    server.setUrl(replaceWithEnvVar(server.getUrl()));
  }

  public String replaceWithEnvVar(String url) {
    String replaced = url;

    Matcher matcher = PATTERN.matcher(url);

    while (matcher.find()) {
      String foundEnvVariables = matcher.group(1);
      String envValue = environment.getProperty(foundEnvVariables);
      if (envValue != null) {
        envValue = envValue.replace("\\", "\\\\");
        Pattern subexpr = Pattern.compile(Pattern.quote(matcher.group(0)));
        replaced = subexpr.matcher(replaced).replaceAll(envValue);
      }
    }
    return replaced;
  }
}
