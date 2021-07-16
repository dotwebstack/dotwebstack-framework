package org.dotwebstack.framework.core.config.validators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Map;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.GraphQlSettingsConfiguration;
import org.dotwebstack.framework.core.config.QueryConfiguration;
import org.dotwebstack.framework.core.config.SettingsConfiguration;
import org.dotwebstack.framework.core.config.SubscriptionConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SettingsValidatorTest {

  @Test
  void validate_succeeds_withProxySettings() {
    // Arrange
    DotWebStackConfiguration config = getBaseConfig();
    SettingsValidator validator = new SettingsValidator();

    // Act / Assert
    assertDoesNotThrow(() -> validator.validate(config));
  }

  @Test
  void validate_fails_withQueriesAndProxySettings() {
    // Arrange
    DotWebStackConfiguration config = getBaseConfig();
    config.setQueries(Map.of("q1", new QueryConfiguration()));
    SettingsValidator validator = new SettingsValidator();

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> new SettingsValidator().validate(config));
  }

  @Test
  void validate_fails_withSubscriptionsAndProxySettings() {
    // Arrange
    DotWebStackConfiguration config = getBaseConfig();
    config.setSubscriptions(Map.of("q1", new SubscriptionConfiguration()));
    SettingsValidator validator = new SettingsValidator();

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> validator.validate(config));
  }

  @SuppressWarnings("unchecked")
  @Test
  void validate_fails_withObjectTypesAndProxySettings() {
    // Arrange
    DotWebStackConfiguration config = getBaseConfig();
    config.setObjectTypes(Map.of("q1", mock(AbstractTypeConfiguration.class)));
    SettingsValidator validator = new SettingsValidator();

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> validator.validate(config));
  }

  private DotWebStackConfiguration getBaseConfig() {
    DotWebStackConfiguration config = new DotWebStackConfiguration();
    SettingsConfiguration settings = new SettingsConfiguration();
    GraphQlSettingsConfiguration graphql = new GraphQlSettingsConfiguration();
    graphql.setProxy("theproxy");
    settings.setGraphql(graphql);
    config.setSettings(settings);
    return config;
  }
}
