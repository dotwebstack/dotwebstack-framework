package org.dotwebstack.framework.backend.rdf4j;

import static com.pivovarit.function.ThrowingFunction.unchecked;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

@Slf4j
@Configuration
class Rdf4jConfiguration {

  private static final String MODEL_PATH_PATTERN = "classpath:config/model/**";

  @Bean
  public RepositoryConnection repositoryConnection(@NonNull ResourceLoader resourceLoader)
      throws IOException {
    SailRepository repository = new SailRepository(new MemoryStore());
    repository.init();

    Resource[] resourceList = ResourcePatternUtils
        .getResourcePatternResolver(resourceLoader)
        .getResources(MODEL_PATH_PATTERN);

    RepositoryConnection repositoryConnection = repository.getConnection();

    Arrays.stream(resourceList)
        .map(unchecked(Resource::getFile))
        .filter(File::isFile)
        .forEach(modelFile -> {
          String fileExtension = Arrays.stream(modelFile.getName().split("\\."))
              .reduce((s1, s2) -> s2)
              .orElseThrow(() -> new InvalidConfigurationException(String
                  .format("Could not determine file extension for '%s'.", modelFile.getName())));

          RDFFormat format = FileFormats.getFormat(fileExtension.toLowerCase());

          if (format != null) {
            LOG.debug("Adding '{}' into local RDF4J repository", modelFile.getName());

            try {
              repositoryConnection.add(modelFile, "", format);
            } catch (IOException e) {
              throw new InvalidConfigurationException("Error while loading data.", e);
            }
          }
        });

    return repositoryConnection;
  }
}
