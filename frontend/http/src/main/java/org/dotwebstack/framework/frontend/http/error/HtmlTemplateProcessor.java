package org.dotwebstack.framework.frontend.http.error;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import lombok.NonNull;
import org.dotwebstack.framework.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class HtmlTemplateProcessor implements ResourceLoaderAware, InitializingBean {

  public static final String DEFAULT_ERROR_PAGE_TEMPLATE = "<HTML>" //
      + "<TITLE>%s</TITLE>" //
      + "<BODY><H1>%s %s</H1><P>%s</P>" //
      + "</BODY></HTML>";

  private static final Logger LOG = LoggerFactory.getLogger(HtmlTemplateProcessor.class);

  private ApplicationProperties applicationProperties;

  private ResourceLoader resourceLoader;

  private Configuration config;

  @Autowired
  HtmlTemplateProcessor(@NonNull ApplicationProperties applicationProperties) {
    this.applicationProperties = applicationProperties;
  }

  @Override
  public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  private static Configuration prepareConfiguration() {
    Configuration result = new Configuration(Configuration.VERSION_2_3_26);

    result.setNumberFormat("computer");
    result.setDefaultEncoding(StandardCharsets.UTF_8.name());
    result.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    result.setLogTemplateExceptions(false);

    return result;
  }

  public void process(ProblemDetails problemDetails, OutputStream outputStream) {
    try {
      Template template = config.getTemplate(String.valueOf(problemDetails.getStatus()));
      template.process(problemDetails, new OutputStreamWriter(outputStream));
    } catch (Exception e) {
      LOG.error(e.getMessage());
      showFallbackPage(problemDetails,outputStream);
    }
  }

  private void showFallbackPage(ProblemDetails problemDetails, OutputStream outputStream) {
    try {
      LOG.debug("Fall back to default error page");
      Writer w = new OutputStreamWriter(outputStream);
      w.write(String.format(DEFAULT_ERROR_PAGE_TEMPLATE, //
          problemDetails.getTitle(), //
          problemDetails.getStatus(), //
          problemDetails.getTitle(), //
          problemDetails.getDetail()));
      w.close();
    } catch (IOException e) {
      LOG.error(e.getMessage());
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    config = prepareConfiguration();
    config.setTemplateLoader(new HtmlTemplateLoader(applicationProperties, resourceLoader));
  }

}
