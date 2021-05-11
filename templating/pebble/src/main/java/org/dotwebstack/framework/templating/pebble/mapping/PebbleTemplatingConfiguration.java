package org.dotwebstack.framework.templating.pebble.mapping;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.extension.Extension;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.loader.DelegatingLoader;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

@Configuration

@Slf4j
public class PebbleTemplatingConfiguration {
  private static final String TEMPLATES_LOCATION = "templates/";

  private static final String CLASSPATH_TEMPLATES_LOCATION = "config/" + TEMPLATES_LOCATION;

  private static final String EXTERNAL_TEMPLATES_LOCATION = "/config/" + TEMPLATES_LOCATION;

  private PebbleEngine pebbleEngine;

  private Optional<Resource> templatesResource;

  private final ResourceLoader resourceLoader;

  public PebbleTemplatingConfiguration(@NonNull ResourceLoader resourceLoader, List<Extension> extensions) {
    this.resourceLoader = resourceLoader;

    this.templatesResource = ResourceLoaderUtils.getResource(TEMPLATES_LOCATION);

    this.pebbleEngine = new PebbleEngine.Builder().extension(extensions.toArray(new Extension[extensions.size()]))
        .loader(getTemplateLoader())
        .build();
  }

  private Loader<?> getTemplateLoader() {
    var classpathLoader = new ClasspathLoader();
    classpathLoader.setPrefix(CLASSPATH_TEMPLATES_LOCATION);

    var fileLoader = new FileLoader();
    fileLoader.setPrefix(EXTERNAL_TEMPLATES_LOCATION);

    return new DelegatingLoader(List.of(classpathLoader, fileLoader));
  }

  @Bean
  public Map<String, PebbleTemplate> htmlTemplates() throws IOException {
    Map<String, PebbleTemplate> htmlTemplates = new HashMap<>();
    if (templatesResource.isPresent()) {
      Resource[] resourceList = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
          .getResources(templatesResource.get()
              .getURI() + "**.html");

      htmlTemplates = Stream.of(resourceList)
          .filter(Resource::exists)
          .map(resource -> {
            LOG.debug("Looking for HTML templates in {}", resource);
            return resource.getFilename();
          })
          .peek(name -> LOG.debug("Adding '{}' as pre-compiled template", name)) // NOSONAR
          .collect(Collectors.toMap(Function.identity(), pebbleEngine::getTemplate));
    }
    return htmlTemplates;
  }
}
