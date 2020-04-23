package org.dotwebstack.framework.templating.pebble.templating;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.function.Executable;

@Slf4j
@TestInstance(Lifecycle.PER_CLASS)
class PebbleTemplateResponseMapperTest {

  private static final String TEMPLATES_LOCATION = "templates/";

  private static final String EXTERNAL_TEMPLATES_LOCATION = "/config/" + TEMPLATES_LOCATION;

  private static final String TEST_RESOURCES = "src/test/resources";

  private PebbleEngine pebbleEngine;

  private PebbleTemplateResponseMapper pebbleTemplateResponseMapper;

  @BeforeAll
  void setUp() {
    // Arrange
    FileLoader fileLoader = new FileLoader();
    fileLoader.setPrefix(TEST_RESOURCES + EXTERNAL_TEMPLATES_LOCATION);

    this.pebbleEngine = new PebbleEngine.Builder().loader(fileLoader)
        .build();

    URI templatesFolder = Paths.get(TEST_RESOURCES, "config")
        .resolve(TEMPLATES_LOCATION)
        .toUri();

    pebbleTemplateResponseMapper = new PebbleTemplateResponseMapper(getTemplates(templatesFolder));
  }

  private Map<String, PebbleTemplate> getTemplates(URI templatesFolder) {
    return Stream.of(templatesFolder)
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


  @Test
  void testValid() {
    // Act
    String response =
        pebbleTemplateResponseMapper.toResponse("correct.html", Map.of(), Map.of("name", "alfa"), Map.of());
    // Assert
    assertThat(response,
        is("<html>\n" + "<head>\n" + "    <title>alfa</title>\n" + "</head>\n" + "<body>\n" + "<div id=\"content\">\n"
            + "    <h1>alfa</h1>\n" + "<h3>since </h3>\n"
            + "<p> Welcome to our beer page. Currently we have <b></b> beers in stock.</p>\n" + "</div>\n" + "\n"
            + "</body>\n" + "</html>"));
  }

  @Test
  void invalidTemplateName_toResponse_throwsInvalidConfigurationException() {
    // Arrange
    Executable action =
        () -> pebbleTemplateResponseMapper.toResponse("non-existent.html", Map.of(), Map.of(), Map.of());

    // Act
    assertThrows(InvalidConfigurationException.class, action);
  }
}
