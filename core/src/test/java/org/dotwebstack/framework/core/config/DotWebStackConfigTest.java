package org.dotwebstack.framework.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class DotWebStackConfigTest {

  @Test
  public void test() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    DotWebStackConfig config = objectMapper.readValue(new File("C:\\Projects\\kdp-new\\dws-dev\\dotwebstack-framework\\example\\example-postgres\\src\\main\\resources\\dotwebstack.yml"),DotWebStackConfig.class);
    assertThat(config, notNullValue());

  }
}