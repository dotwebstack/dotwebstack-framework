package org.dotwebstack.framework.core.condition;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.core.model.GraphQlSettings;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.model.Settings;
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
    Schema schema = getSchema(null);
    doReturn(schema).when(condition)
        .readSchema(any(String.class));

    ConditionContext context = mock(ConditionContext.class, Answers.RETURNS_DEEP_STUBS);
    when(context.getEnvironment()
        .getProperty(any(String.class))).thenReturn("schema.yaml");

    boolean matches = condition.matches(context, mock(AnnotatedTypeMetadata.class));
    assertThat(matches, is(true));
  }

  @Test
  void matches_returnsFalse_whenProxyIsSet() {
    Schema schema = getSchema("theproxy");
    doReturn(schema).when(condition)
        .readSchema(any(String.class));

    ConditionContext context = mock(ConditionContext.class, Answers.RETURNS_DEEP_STUBS);
    when(context.getEnvironment()
        .getProperty(any(String.class))).thenReturn("schema.yaml");

    boolean matches = condition.matches(context, mock(AnnotatedTypeMetadata.class));
    assertThat(matches, is(false));
  }

  private Schema getSchema(String proxy) {
    var schema = new Schema();
    var settings = new Settings();
    var graphql = new GraphQlSettings();
    graphql.setProxy(proxy);

    settings.setGraphql(graphql);
    schema.setSettings(settings);

    return schema;
  }
}
