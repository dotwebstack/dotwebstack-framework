package org.dotwebstack.framework.param.template;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

// TODO Move the informationproduct package
@Component
public class TemplateProcessor implements InitializingBean {

  private static final Logger LOG = LoggerFactory.getLogger(TemplateProcessor.class);

  private Configuration config;

  public String processString(@NonNull String templateString, Map<String, Object> parameters)
      throws TemplateException {
    LOG.debug("Processing template string with parameters: {}", parameters);

    try {
      Template endpointTemplate = new Template(null, templateString, config);
      StringWriter processedStringWriter = new StringWriter();

      endpointTemplate.process(parameters, processedStringWriter);

      return processedStringWriter.toString();
    } catch (IOException | freemarker.template.TemplateException ex) {
      throw new TemplateException(ex);
    }
  }

  private static Configuration prepareConfiguration() throws IOException {
    Configuration result = new Configuration(Configuration.VERSION_2_3_26);

    result.setNumberFormat("computer");
    result.setDefaultEncoding(StandardCharsets.UTF_8.name());
    result.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    result.setLogTemplateExceptions(false);

    return result;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    config = prepareConfiguration();
  }

}
