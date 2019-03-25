package org.dotwebstack.framework.backend.rdf4j;

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
public class Rdf4jConfiguration {

  private static final String MODEL_PATH_PATTERN = "classpath:config/model/**";

  @Bean
  RepositoryConnection repositoryConnection(@NonNull ResourceLoader resourceLoader)
      throws IOException {
    SailRepository repository = new SailRepository(new MemoryStore());
    repository.init();

    RepositoryConnection repositoryConnection = repository.getConnection();

    Resource[] resourceList = ResourcePatternUtils
        .getResourcePatternResolver(resourceLoader)
        .getResources(MODEL_PATH_PATTERN);

    Arrays.stream(resourceList)
        .filter(Resource::isReadable)
        .filter(resource -> resource.getFilename() != null)
        .forEach(modelResource -> {
          String fileExtension = Arrays
              .stream(modelResource.getFilename().split("\\."))
              .reduce("", (s1, s2) -> s2);

          RDFFormat format = FileFormats.getFormat(fileExtension);

          if (format != null) {
            LOG.debug("Adding '{}' into local repository", modelResource.getFilename());

            try {
              repositoryConnection.add(modelResource.getInputStream(), "", format);
            } catch (IOException e) {
              throw new InvalidConfigurationException("Error while loading data.", e);
            }
          }
        });

    return repositoryConnection;
  }
}
