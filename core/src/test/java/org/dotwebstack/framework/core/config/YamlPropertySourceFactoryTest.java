package org.dotwebstack.framework.core.config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = YamlTestConfigurationProperties.class)
@EnableConfigurationProperties(YamlTestConfigurationProperties.class)
class YamlPropertySourceFactoryTest {

  @Autowired
  private YamlTestConfigurationProperties yamlTestConfigurationProperties;

  @Test
  void onBootstrap_fieldsAreInjected_forTestProperties() {
    assertThat(yamlTestConfigurationProperties.getName(), equalTo("foo"));
    assertThat(yamlTestConfigurationProperties.getAliases(), equalTo(List.of("abc", "xyz")));
  }
}
