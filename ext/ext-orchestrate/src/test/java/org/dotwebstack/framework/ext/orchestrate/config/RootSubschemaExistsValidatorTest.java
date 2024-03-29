package org.dotwebstack.framework.ext.orchestrate.config;

import static org.dotwebstack.framework.ext.orchestrate.config.OrchestrateConfigurationTest.ROOT_KEY;
import static org.hamcrest.Matchers.is;

import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import org.dotwebstack.framework.ext.orchestrate.config.OrchestrateConfigurationProperties.SubschemaProperties;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RootSubschemaExistsValidatorTest {

  @Mock
  private static ConstraintValidatorContext context;

  private static final RootSubschemaExistsValidator VALIDATOR = new RootSubschemaExistsValidator();

  @Test
  void isValid_returnsTrue_whenRootKeyFound() {
    var configurationProperties = new OrchestrateConfigurationProperties();
    configurationProperties.setRoot(ROOT_KEY);
    configurationProperties.setSubschemas(Map.of(ROOT_KEY, new SubschemaProperties()));

    var isValid = VALIDATOR.isValid(configurationProperties, context);

    MatcherAssert.assertThat(isValid, is(true));
  }

  @Test
  void isValid_returnsFalse_whenRootKeyNotFound() {
    var configurationProperties = new OrchestrateConfigurationProperties();
    configurationProperties.setRoot("foo");
    configurationProperties.setSubschemas(Map.of(ROOT_KEY, new SubschemaProperties()));

    var isValid = VALIDATOR.isValid(configurationProperties, context);

    MatcherAssert.assertThat(isValid, is(false));
  }
}
