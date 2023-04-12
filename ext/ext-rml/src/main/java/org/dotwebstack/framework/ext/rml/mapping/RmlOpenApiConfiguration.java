package org.dotwebstack.framework.ext.rml.mapping;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.invalidOpenApiConfigurationException;

import io.carml.model.TriplesMap;
import io.carml.util.Models;
import io.carml.util.RmlMappingLoader;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.core.ResourceProperties;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelCollector;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RmlOpenApiConfiguration {

  private static final String RML_MAPPING_PATH = "rml/";

  private static final String X_DWS_RML_MAPPING = "x-dws-rml-mapping";

  private static final RmlMappingLoader RML_MAPPING_LOADER = RmlMappingLoader.build();

  @Bean
  public Map<Operation, Set<TriplesMap>> mappingsPerOperation(OpenAPI openApi) {
    return openApi.getPaths()
        .entrySet()
        .stream()
        .flatMap(this::unpackOperations)
        .map(pathEntry -> mappingPerOperation(pathEntry.getKey(), pathEntry.getValue()))
        .flatMap(map -> map.entrySet()
            .stream())
        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private Stream<Map.Entry<String, Operation>> unpackOperations(Map.Entry<String, PathItem> pathEntry) {
    var name = pathEntry.getKey();
    var pathItem = pathEntry.getValue();

    return Stream.of(pathItem.getGet(), pathItem.getPost())
        .filter(Objects::nonNull)
        .map(operation -> Map.entry(name, operation));
  }

  @SuppressWarnings({"unchecked"})
  private Map<Operation, Set<TriplesMap>> mappingPerOperation(String name, Operation operation) {
    Map<String, Object> extensions = operation.getExtensions();

    if (extensions == null || !extensions.containsKey(X_DWS_RML_MAPPING)) {
      return Map.of();
    }

    Object mappingConfig = extensions.get(X_DWS_RML_MAPPING);

    if (mappingConfig instanceof List) {
      return Map.of(operation, resolveMappings((List<String>) mappingConfig));
    } else if (mappingConfig instanceof String stringConfig) {
      return Map.of(operation, resolveMappings(List.of(stringConfig)));
    } else {
      throw invalidOpenApiConfigurationException("{} on {} is not a list of RML mapping paths", X_DWS_RML_MAPPING,
          name);
    }
  }

  private Set<TriplesMap> resolveMappings(List<String> mappingFiles) {
    Model mapping = mappingFiles.stream()
        .map(this::resolveMappingPerFile)
        .flatMap(Model::stream)
        .collect(ModelCollector.toModel());

    return RML_MAPPING_LOADER.load(mapping);
  }

  private Model resolveMappingPerFile(String mappingFile) {
    URI location = ResourceProperties.getFileConfigPath()
        .resolve(RML_MAPPING_PATH + mappingFile);
    var path = Paths.get(location);

    InputStream mappingInputStream;
    if (Files.exists(path)) {
      try {
        mappingInputStream = new FileInputStream(path.toFile());
      } catch (FileNotFoundException fileNotFoundException) {
        throw invalidConfigurationException("Could not resolve mapping file {}", path, fileNotFoundException);
      }
    } else {
      mappingInputStream = getClass().getResourceAsStream(ResourceProperties.getResourcePath()
          .resolve(RML_MAPPING_PATH + mappingFile)
          .getPath());
    }

    if (mappingInputStream == null) {
      throw invalidConfigurationException("Could not resolve mapping file {}", path);
    }

    RDFFormat rdfFormat = Rio.getParserFormatForFileName(mappingFile)
        .orElseThrow(() -> invalidConfigurationException(
            "Could not determine rdf format for mapping filename: {}. Supported file extensions are: {}", mappingFile,
            RDFParserRegistry.getInstance()
                .getKeys()
                .stream()
                .map(RDFFormat::getFileExtensions)
                .flatMap(List::stream)
                .toList()));

    return Models.parse(mappingInputStream, rdfFormat);
  }
}
