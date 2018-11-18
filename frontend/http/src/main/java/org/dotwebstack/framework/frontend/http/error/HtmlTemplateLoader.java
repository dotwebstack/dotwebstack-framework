package org.dotwebstack.framework.frontend.http.error;

import freemarker.cache.TemplateLoader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import lombok.NonNull;
import org.dotwebstack.framework.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

public class HtmlTemplateLoader implements TemplateLoader {

  private static final Logger LOG = LoggerFactory.getLogger(HtmlTemplateLoader.class);

  private ApplicationProperties applicationProperties;

  private ResourceLoader resourceLoader;

  HtmlTemplateLoader(@NonNull ApplicationProperties applicationProperties,
      @NonNull ResourceLoader resourceLoader) {
    this.applicationProperties = applicationProperties;
    this.resourceLoader = resourceLoader;
  }

  @Override
  public Object findTemplateSource(String templateId) {
    String filepath = String.format("%s/errorpages/%s.html",
        applicationProperties.getResourcePath(),templateId);
    LOG.debug("Looking for template: {}", filepath);
    Resource resource = resourceLoader.getResource(filepath);
    if (resource.exists()) {
      return resource;
    } else {
      return null;
    }
  }

  @Override
  public long getLastModified(Object templateSource) {
    return -1; //Don't know
  }

  @Override
  public Reader getReader(Object templateSource, String encoding) throws IOException {
    return new InputStreamReader(((Resource) templateSource).getInputStream());
  }

  @Override
  public void closeTemplateSource(Object templateSource)  {
    // Nothing to do here...
  }

}
