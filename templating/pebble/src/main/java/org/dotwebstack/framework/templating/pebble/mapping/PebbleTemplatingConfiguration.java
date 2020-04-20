package org.dotwebstack.framework.templating.pebble.mapping;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.dotwebstack.framework.core.CoreProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableConfigurationProperties(CoreProperties.class)
public class PebbleTemplatingConfiguration {

  private static final String TEMPLATES_LOCATION = "templates/";

  private List<String> templateLocations = new ArrayList<>();

  private final CoreProperties coreProperties;

  public PebbleTemplatingConfiguration(CoreProperties coreProperties) {
    this.coreProperties = coreProperties;
  }

  @Bean
  public void getTemplates() throws IOException {
    this.templateLocations = getTemplateFileNames();

    PebbleEngine engine = new PebbleEngine.Builder().build();
    Map<String, PebbleTemplate> templates = new HashMap<>();

    templateLocations.forEach(templateLocation -> {
      PebbleTemplate compiledTemplate = engine.getLiteralTemplate(templateLocation);
      templates.put(templateLocation, compiledTemplate);
    });


  }

  private List<String> getTemplateFileNames() throws IOException {
    List<String> result = new ArrayList<>();

    Resource templates = new ClassPathResource("config/templates");
    String path = templates.getURL().getPath();

    return Arrays.stream(new File(path).listFiles()).map(File::toString).collect(Collectors.toList());

//    try (Stream<Path> paths = Files.walk(Paths.get(coreProperties.getResourcePath().resolve(TEMPLATES_LOCATION)))) {
//      paths
//          .filter(Files::isRegularFile)
//          .forEach(path -> result.add(path.toString()));
//    } catch (IOException e) {
//      e.printStackTrace();
//    }

//    return result;

  }

}
