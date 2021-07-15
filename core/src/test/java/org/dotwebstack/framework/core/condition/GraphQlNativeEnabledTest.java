package org.dotwebstack.framework.core.condition;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.GraphQlSettingsConfiguration;
import org.dotwebstack.framework.core.config.SettingsConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@ExtendWith(MockitoExtension.class)
class GraphQlNativeEnabledTest {

  @Spy
  private GraphQlNativeEnabled condition;

  @Test
  void matches_returnsTrue_whenProxyIsNotSet() {
    DotWebStackConfiguration config = getConfig(null);
    doReturn(config).when(condition)
        .readConfig(any(String.class));

    ConditionContext context = mock(ConditionContext.class, Answers.RETURNS_DEEP_STUBS);
    when(context.getEnvironment()
        .getProperty(any(String.class))).thenReturn("config.yaml");

    boolean matches = condition.matches(context, mock(AnnotatedTypeMetadata.class));
    assertThat(matches, is(true));
  }

  @Test
  void matches_returnsFalse_whenProxyIsSet() {
    DotWebStackConfiguration config = getConfig("theproxy");
    doReturn(config).when(condition)
        .readConfig(any(String.class));

    ConditionContext context = mock(ConditionContext.class, Answers.RETURNS_DEEP_STUBS);
    when(context.getEnvironment()
        .getProperty(any(String.class))).thenReturn("config.yaml");

    boolean matches = condition.matches(context, mock(AnnotatedTypeMetadata.class));
    assertThat(matches, is(false));
  }

  private DotWebStackConfiguration getConfig(String proxy) {
    DotWebStackConfiguration config = new DotWebStackConfiguration();
    SettingsConfiguration settings = new SettingsConfiguration();
    GraphQlSettingsConfiguration graphql = new GraphQlSettingsConfiguration();
    graphql.setProxy(proxy);

    settings.setGraphql(graphql);
    config.setSettings(settings);

    return config;
  }
}
