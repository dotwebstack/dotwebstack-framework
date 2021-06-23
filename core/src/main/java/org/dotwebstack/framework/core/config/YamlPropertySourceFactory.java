package org.dotwebstack.framework.core.config;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import java.util.Properties;
import lombok.NonNull;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

public class YamlPropertySourceFactory implements PropertySourceFactory {

  @Override
  public @NonNull PropertySource<?> createPropertySource(String s, EncodedResource encodedResource) {
    YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
    factory.setResources(encodedResource.getResource());

    Properties properties = factory.getObject();

    if (properties == null) {
      throw illegalStateException("Unable to get properties from factory!");
    }

    if (encodedResource.getResource()
        .getFilename() == null) {
      throw illegalArgumentException("Resource filename is null!");
    }

    return new PropertiesPropertySource(encodedResource.getResource()
        .getFilename(), properties);
  }
}
