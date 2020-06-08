package org.dotwebstack.framework.templating.pebble.mapping;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.extension.Extension;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.loader.DelegatingLoader;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

@Slf4j
public class PebbleTemplatingConfiguration {
  private static final String TEMPLATES_LOCATION = "templates/";

  private static final String CLASSPATH_TEMPLATES_LOCATION = "config/" + TEMPLATES_LOCATION;

  private static final String EXTERNAL_TEMPLATES_LOCATION = "/config/" + TEMPLATES_LOCATION;

  private PebbleEngine pebbleEngine;

  private URI templatesLocation;

  public PebbleTemplatingConfiguration(List<Extension> extensions) {

    templatesLocation = ResourceLoaderUtils.getResourceLocation(TEMPLATES_LOCATION)
        .orElse(null);

    this.pebbleEngine = new PebbleEngine.Builder().extension(extensions.toArray(new Extension[extensions.size()]))
        .loader(getTemplateLoader())
        .build();
  }

  private Loader<?> getTemplateLoader() {
    ClasspathLoader classpathLoader = new ClasspathLoader();
    classpathLoader.setPrefix(CLASSPATH_TEMPLATES_LOCATION);

    FileLoader fileLoader = new FileLoader();
    fileLoader.setPrefix(EXTERNAL_TEMPLATES_LOCATION);

    return new DelegatingLoader(List.of(classpathLoader, fileLoader));
  }

  @Bean
  public Map<String, PebbleTemplate> htmlTemplates() {
    return Stream.ofNullable(templatesLocation)
        .map(Paths::get)
        .filter(Files::exists)
        .peek(location -> LOG.debug("Looking for HTML templates in {}", location))
        .map(Path::toFile)
        .map(File::listFiles)
        .filter(Objects::nonNull)
        .flatMap(Arrays::stream)
        .map(File::toPath)
        .map(Path::getFileName)
        .map(Path::toString)
        .peek(name -> LOG.debug("Adding '{}' as pre-compiled template", name))
        .collect(Collectors.toMap(Function.identity(), pebbleEngine::getTemplate));
  }

}
