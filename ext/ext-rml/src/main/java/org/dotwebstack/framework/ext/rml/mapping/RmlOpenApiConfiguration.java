package org.dotwebstack.framework.ext.rml.mapping;

import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.util.Models;
import com.taxonic.carml.util.RmlMappingLoader;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.ResourceProperties;
import org.dotwebstack.framework.service.openapi.HttpMethodOperation;
import org.dotwebstack.framework.service.openapi.OpenApiConfiguration;
import org.dotwebstack.framework.service.openapi.exception.InvalidOpenApiConfigurationException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelCollector;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RmlOpenApiConfiguration {

  private static final String RML_MAPPING_PATH = "rml/";

  private static final String X_DWS_RML_MAPPING = "x-dws-rml-mapping";

  private static final RmlMappingLoader RML_MAPPING_LOADER = RmlMappingLoader.build();

  @Bean
  public Map<HttpMethodOperation, Set<TriplesMap>> mappingsPerOperation(OpenAPI openApi) {
    return openApi.getPaths()
        .entrySet()
        .stream()
        .map(entry -> OpenApiConfiguration.getHttpMethodOperations(entry.getValue(), entry.getKey()))
        .flatMap(List::stream)
        .map(this::mappingPerOperation)
        .flatMap(map -> map.entrySet()
            .stream())
        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @SuppressWarnings({"unchecked"})
  private Map<HttpMethodOperation, Set<TriplesMap>> mappingPerOperation(HttpMethodOperation httpMethodOperation) {
    Operation operation = httpMethodOperation.getOperation();

    Map<String, Object> extensions = operation.getExtensions();

    if (extensions == null || !extensions.containsKey(X_DWS_RML_MAPPING)) {
      return Map.of();
    }

    Object mappingConfig = extensions.get(X_DWS_RML_MAPPING);

    if (mappingConfig instanceof List) {
      return Map.of(httpMethodOperation, resolveMappings((List<String>) mappingConfig));
    } else if (mappingConfig instanceof String) {
      return Map.of(httpMethodOperation, resolveMappings(List.of((String) mappingConfig)));
    } else {
      throw new InvalidOpenApiConfigurationException(String.format("%s on %s is not a list of RML mapping paths",
          X_DWS_RML_MAPPING, httpMethodOperation.getName()));
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
        throw new InvalidConfigurationException(String.format("Could not resolve mapping file %s", path),
            fileNotFoundException);
      }
    } else {
      mappingInputStream = getClass().getResourceAsStream(ResourceProperties.getResourcePath()
          .resolve(RML_MAPPING_PATH + mappingFile)
          .getPath());
    }

    if (mappingInputStream == null) {
      throw new InvalidConfigurationException(String.format("Could not resolve mapping file %s", path));
    }

    return Models.parse(mappingInputStream, RDFFormat.TURTLE);
  }
}
