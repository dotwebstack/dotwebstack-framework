package org.dotwebstack.framework.backend.rdf4j;

import static com.pivovarit.function.ThrowingFunction.unchecked;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.backend.BackendLoader;
import org.dotwebstack.framework.core.backend.BackendRegistry;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
final class LocalBackendLoader implements BackendLoader {

  private static final String MODEL_PATH_PATTERN = "classpath:config/model/**";

  private final ResourceLoader resourceLoader;

  @Override
  public void load(@NonNull BackendRegistry backendRegistry) {
    SailRepository repository = new SailRepository(new MemoryStore());
    repository.init();

    Resource[] resourceList;

    try {
      resourceList = ResourcePatternUtils
          .getResourcePatternResolver(resourceLoader)
          .getResources(MODEL_PATH_PATTERN);
    } catch (IOException e) {
      throw new InvalidConfigurationException("Could not read model-configuration folder.", e);
    }

    try (RepositoryConnection con = repository.getConnection()) {
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
                con.add(modelFile, "", format);
              } catch (IOException e) {
                throw new InvalidConfigurationException("Error while loading data.", e);
              }
            }
          });
    }

    backendRegistry
        .register(Rdf4jBackend.LOCAL_BACKEND_NAME, new Rdf4jBackend(repository));
    LOG.debug("Registered '{}' RDF4J backend", Rdf4jBackend.LOCAL_BACKEND_NAME);
  }
}
