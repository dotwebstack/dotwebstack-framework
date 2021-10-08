// package org.dotwebstack.framework.core.config.validators;
//
// import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.Mockito.mock;
//
// import java.util.Map;
// import org.dotwebstack.framework.core.InvalidConfigurationException;
// import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
// import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
// import org.dotwebstack.framework.core.config.GraphQlSettingsConfiguration;
// import org.dotwebstack.framework.core.query.model.QueryConfiguration;
// import org.dotwebstack.framework.core.config.SettingsConfiguration;
// import org.dotwebstack.framework.core.model.SubscriptionConfiguration;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.junit.jupiter.MockitoExtension;
//
// @ExtendWith(MockitoExtension.class)
// class SettingsValidatorTest {
//
// @Test
// void validate_succeeds_withProxySettings() {
// DotWebStackConfiguration config = getBaseConfig();
// SettingsValidator validator = new SettingsValidator();
//
// assertDoesNotThrow(() -> validator.validate(config));
// }
//
// @Test
// void validate_fails_withQueriesAndProxySettings() {
// DotWebStackConfiguration config = getBaseConfig();
// config.setQueries(Map.of("q1", new QueryConfiguration()));
// SettingsValidator validator = new SettingsValidator();
//
// assertThrows(InvalidConfigurationException.class, () -> validator.validate(config));
// }
//
// @Test
// void validate_fails_withSubscriptionsAndProxySettings() {
// DotWebStackConfiguration config = getBaseConfig();
// config.setSubscriptions(Map.of("q1", new SubscriptionConfiguration()));
// SettingsValidator validator = new SettingsValidator();
//
// assertThrows(InvalidConfigurationException.class, () -> validator.validate(config));
// }
//
// @SuppressWarnings("unchecked")
// @Test
// void validate_fails_withObjectTypesAndProxySettings() {
// DotWebStackConfiguration config = getBaseConfig();
// config.setObjectTypes(Map.of("q1", mock(AbstractTypeConfiguration.class)));
// SettingsValidator validator = new SettingsValidator();
//
// assertThrows(InvalidConfigurationException.class, () -> validator.validate(config));
// }
//
// private DotWebStackConfiguration getBaseConfig() {
// DotWebStackConfiguration config = new DotWebStackConfiguration();
// SettingsConfiguration settings = new SettingsConfiguration();
// GraphQlSettingsConfiguration graphql = new GraphQlSettingsConfiguration();
// graphql.setProxy("theproxy");
// settings.setGraphql(graphql);
// config.setSettings(settings);
// return config;
// }
// }
