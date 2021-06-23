package org.dotwebstack.framework.core.config;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import org.hamcrest.CoreMatchers;
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
  void whenFactoryProvidedThenYamlPropertiesInjected() {
    assertThat(yamlTestConfigurationProperties.getName(), CoreMatchers.equalTo("foo"));
    assertThat(yamlTestConfigurationProperties.getAliases(), CoreMatchers.equalTo(List.of("abc", "xyz")));
  }

}
