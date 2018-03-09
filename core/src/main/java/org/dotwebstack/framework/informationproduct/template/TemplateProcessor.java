package org.dotwebstack.framework.informationproduct.template;

import com.google.common.collect.ImmutableMap;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateMethodModelEx;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class TemplateProcessor implements InitializingBean {

  private static final Logger LOG = LoggerFactory.getLogger(TemplateProcessor.class);

  private static final TemplateMethodModelEx ESCAPE = new EscapeStringLiteralMethod();

  private Configuration config;

  private static Configuration prepareConfiguration() {
    Configuration result = new Configuration(Configuration.VERSION_2_3_26);

    result.setNumberFormat("computer");
    result.setDefaultEncoding(StandardCharsets.UTF_8.name());
    result.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    result.setLogTemplateExceptions(false);

    return result;
  }

  public String processString(@NonNull String templateString,
      @NonNull Map<String, Object> parameters) {
    LOG.debug("Processing template string with parameters: {}", parameters);

    try {
      Template endpointTemplate = new Template(null, templateString, config);
      StringWriter processedStringWriter = new StringWriter();

      ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
      builder.put(EscapeStringLiteralMethod.CTX_NAME, ESCAPE);
      builder.putAll(parameters);

      endpointTemplate.process(builder.build(), processedStringWriter);

      return processedStringWriter.toString();
    } catch (IOException | freemarker.template.TemplateException ex) {
      throw new TemplateException(ex);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    config = prepareConfiguration();
  }

}
