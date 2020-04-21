package org.dotwebstack.framework.templating.pebble.mapping;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.loader.DelegatingLoader;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.CoreProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableConfigurationProperties(CoreProperties.class)
@Slf4j
public class PebbleTemplatingConfiguration {
  private static final String TEMPLATES_LOCATION = "templates";

  private PebbleEngine pebbleEngine;

  private final CoreProperties coreProperties;

  private final URI externalTemplatesLocation;

  private final URI classpathTemplatesLocation;

  public PebbleTemplatingConfiguration(CoreProperties coreProperties) throws URISyntaxException {
    this.externalTemplatesLocation = coreProperties.getFileConfigPath()
        .resolve(TEMPLATES_LOCATION);

    this.classpathTemplatesLocation = getClass().getResource(coreProperties.getResourcePath()
        .resolve(TEMPLATES_LOCATION)
        .getPath())
        .toURI();

    this.coreProperties = coreProperties;

    buildEngine();
  }

  @Bean
  public Map<String, PebbleTemplate> htmlTemplates() {
    return Stream.of(externalTemplatesLocation, classpathTemplatesLocation)
        .map(Paths::get)
        .filter(Files::exists)
        .peek(location -> LOG.debug("Looking for HTML templates in {}", location))
        .map(Path::toFile)
        .map(File::listFiles)
        .filter(Objects::nonNull)
        .flatMap(Arrays::stream)
        .map(File::toPath)
        .peek(location -> LOG.debug("Adding '{}' as pre-compiled template", location.getFileName()))
        .collect(Collectors.toMap(getTemplateName(), getCompiledTemplate()));
  }

  private void buildEngine() {
    List<Loader<?>> defaultLoadingStrategies = new ArrayList<>();
    ClasspathLoader classpathLoader = new ClasspathLoader();
    classpathLoader.setPrefix("config/templates/");

    defaultLoadingStrategies.add(classpathLoader);

    FileLoader fileLoader = new FileLoader();
    fileLoader.setPrefix("/config/templates/");

    defaultLoadingStrategies.add(fileLoader);

    this.pebbleEngine = new PebbleEngine.Builder()
        .loader(new DelegatingLoader(defaultLoadingStrategies))
        .build();
  }

  private Function<Path, PebbleTemplate> getCompiledTemplate() {
    return location -> pebbleEngine.getTemplate(location.getFileName().toString());
  }

  private Function<Path, String> getTemplateName() {
    return location -> location.getFileName()
        .toString();
  }

}
