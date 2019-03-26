package org.dotwebstack.framework.backend.rdf4j;

import static org.dotwebstack.framework.backend.rdf4j.LocalBackend.LOCAL_BACKEND_NAME;

import java.io.IOException;
import java.util.Arrays;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.BackendConfigurer;
import org.dotwebstack.framework.core.BackendRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
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
public class LocalBackendConfigurer implements BackendConfigurer {

  private static final String MODEL_PATH_PATTERN = "classpath:config/model/**";

  private final ResourceLoader resourceLoader;

  @Override
  public void registerBackends(BackendRegistry registry) {
    try {
      LocalBackend localBackend = new LocalBackend(createRepository());
      registry.register(LOCAL_BACKEND_NAME, localBackend);
    } catch (IOException e) {
      throw new InvalidConfigurationException("Failed creating local repository.", e);
    }
  }

  private SailRepository createRepository() throws IOException {
    SailRepository repository = new SailRepository(new MemoryStore());
    repository.init();

    Resource[] resourceList = ResourcePatternUtils
        .getResourcePatternResolver(resourceLoader)
        .getResources(MODEL_PATH_PATTERN);

    @Cleanup RepositoryConnection con = repository.getConnection();

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
              con.add(modelResource.getInputStream(), "", format);
            } catch (IOException e) {
              throw new InvalidConfigurationException("Error while loading data.", e);
            }
          }
        });

    return repository;
  }

}
