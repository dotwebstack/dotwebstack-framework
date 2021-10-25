package org.dotwebstack.framework.core.config.validators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Map;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.model.GraphQlSettings;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.model.Settings;
import org.dotwebstack.framework.core.model.Subscription;
import org.dotwebstack.framework.core.query.model.Query;
import org.dotwebstack.framework.core.testhelpers.TestObjectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SettingsValidatorTest {

  @Test
  void validate_succeeds_withProxySettings() {
    Schema config = getBaseConfig();
    SettingsValidator validator = new SettingsValidator();

    assertDoesNotThrow(() -> validator.validate(config));
  }

  @Test
  void validate_fails_withQueriesAndProxySettings() {
    Schema config = getBaseConfig();
    config.setQueries(Map.of("q1", new Query()));
    SettingsValidator validator = new SettingsValidator();

    assertThrows(InvalidConfigurationException.class, () -> validator.validate(config));
  }

  @Test
  void validate_fails_withSubscriptionsAndProxySettings() {
    Schema config = getBaseConfig();
    config.setSubscriptions(Map.of("q1", new Subscription()));
    SettingsValidator validator = new SettingsValidator();

    assertThrows(InvalidConfigurationException.class, () -> validator.validate(config));
  }

  @Test
  void validate_fails_withObjectTypesAndProxySettings() {
    Schema config = getBaseConfig();
    config.setObjectTypes(Map.of("q1", mock(TestObjectType.class)));
    SettingsValidator validator = new SettingsValidator();

    assertThrows(InvalidConfigurationException.class, () -> validator.validate(config));
  }

  private Schema getBaseConfig() {
    Schema config = new Schema();
    Settings settings = new Settings();
    GraphQlSettings graphql = new GraphQlSettings();
    graphql.setProxy("theproxy");
    settings.setGraphql(graphql);
    config.setSettings(settings);
    return config;
  }
}
