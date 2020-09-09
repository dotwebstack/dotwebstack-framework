package org.dotwebstack.framework.backend.rdf4j;

import static org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeFactory.createShapeFromModel;
import static org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeFactory.processInheritance;

import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetchingEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties.RepositoryProperties;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfig;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.config.MemoryStoreConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

@Slf4j
@Configuration
@EnableConfigurationProperties(Rdf4jProperties.class)
class Rdf4jConfiguration {

  public static final String LOCAL_REPOSITORY_ID = "local";

  private static final String BASE_DIR_PREFIX = "rdf4j";

  private static final String MODEL_PATH = "model";

  private static final String MODEL_PATTERN = "/**.trig";

  private static final String SPARQL_PATH = "sparql";

  private static final String SPARQL_PATTERN = "/**.rq";

  @Bean
  public ConfigFactory configFactory() {
    return new ConfigFactoryImpl();
  }

  @Bean
  RepositoryAdapter localRepositoryAdapter(LocalRepositoryManager localRepositoryManager) {
    return new RepositoryAdapter() {
      @Override
      public TupleQuery prepareTupleQuery(String repositoryId, DataFetchingEnvironment environment, String query) {
        return localRepositoryManager.getRepository(repositoryId)
            .getConnection()
            .prepareTupleQuery(query);
      }

      @Override
      public GraphQuery prepareGraphQuery(String repositoryId, DataFetchingEnvironment environment, String query,
          List<String> subjectIris) {
        return localRepositoryManager.getRepository(repositoryId)
            .getConnection()
            .prepareGraphQuery(query);
      }

      @Override
      public BooleanQuery prepareBooleanQuery(String repositoryId, DataFetchingEnvironment environment, String query) {
        return localRepositoryManager.getRepository(repositoryId)
            .getConnection()
            .prepareBooleanQuery(query);
      }

      @Override
      public boolean supports(String repositoryId) {
        return localRepositoryManager.hasRepositoryConfig(repositoryId);
      }
    };
  }

  @Bean
  LocalRepositoryManager localRepositoryManager(@NonNull Rdf4jProperties rdf4jProperties,
      @NonNull ConfigFactory configFactory, @NonNull ResourceLoader resourceLoader) throws IOException {
    LOG.debug("Initializing repository manager");

    File baseDir = Files.createTempDirectory(BASE_DIR_PREFIX)
        .toFile();
    LocalRepositoryManager repositoryManager = new LocalRepositoryManager(baseDir);
    repositoryManager.init();

    // Add & populate local repository
    repositoryManager.addRepositoryConfig(createLocalRepositoryConfig());

    populateLocalRepository(repositoryManager.getRepository(LOCAL_REPOSITORY_ID), resourceLoader);

    // Add repositories from external config
    if (rdf4jProperties.getRepositories() != null) {
      rdf4jProperties.getRepositories()
          .entrySet()
          .stream()
          .map(repositoryProperty -> createRepositoryConfig(repositoryProperty, configFactory))
          .forEach(repositoryManager::addRepositoryConfig);
    }

    return repositoryManager;
  }

  @Bean
  NodeShapeRegistry nodeShapeRegistry(@NonNull LocalRepositoryManager localRepositoryManager,
      @NonNull Rdf4jProperties rdf4jProperties) {
    Repository repository = localRepositoryManager.getRepository(LOCAL_REPOSITORY_ID);

    Model shapeModel = QueryResults.asModel(repository.getConnection()
        .getStatements(null, null, null, rdf4jProperties.getShape()
            .getGraph()));
    NodeShapeRegistry registry = new NodeShapeRegistry(rdf4jProperties.getShape()
        .getPrefix());

    Map<org.eclipse.rdf4j.model.Resource, NodeShape> nodeShapeMap = new HashMap<>();
    Models.subjectIRIs(shapeModel.filter(null, RDF.TYPE, SHACL.NODE_SHAPE))
        .forEach(subject -> createShapeFromModel(shapeModel, subject, nodeShapeMap));

    nodeShapeMap.values()
        .forEach(shape -> {
          processInheritance(shape, nodeShapeMap);
          registry.register(shape.getIdentifier(), shape);
        });

    return registry;
  }

  @Bean
  public Map<String, String> queryReferenceRegistry(@NonNull ResourceLoader resourceLoader) throws IOException {
    Map<String, String> result = new HashMap<>();

    Optional<Resource> sparqlLocationResource = ResourceLoaderUtils.getResource(SPARQL_PATH);

    if (sparqlLocationResource.isPresent()) {
      Resource[] resourceList = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
          .getResources(sparqlLocationResource.get()
              .getURI() + SPARQL_PATTERN);

      for (Resource resource : resourceList) {
        String content = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        String fileName = resource.getFilename();
        result.put(fileName.substring(0, fileName.lastIndexOf('.')), content);
      }
    }

    return result;
  }

  private static RepositoryConfig createRepositoryConfig(Entry<String, RepositoryProperties> repositoryEntry,
      ConfigFactory configFactory) {
    String repositoryId = repositoryEntry.getKey();
    RepositoryProperties repository = repositoryEntry.getValue();

    RepositoryImplConfig repositoryImplConfig = configFactory.create(repository.getType(),
        repository.getArgs() != null ? repository.getArgs() : ImmutableMap.of());
    repositoryImplConfig.validate();

    return new RepositoryConfig(repositoryId, repositoryImplConfig);
  }

  private static RepositoryConfig createLocalRepositoryConfig() {
    SailRepositoryConfig repositoryConfig = new SailRepositoryConfig(new MemoryStoreConfig());
    return new RepositoryConfig(LOCAL_REPOSITORY_ID, repositoryConfig);
  }

  private static void populateLocalRepository(Repository repository, ResourceLoader resourceLoader) {
    ResourceLoaderUtils.getResource(MODEL_PATH)
        .ifPresent(resource -> {
          Resource[] resourceList;

          try {
            resourceList = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                .getResources(resource.getURI()
                    .toString() + MODEL_PATTERN);
          } catch (IOException e) {
            throw new UncheckedIOException("Error while loading local model.", e);
          }

          @Cleanup
          RepositoryConnection con = repository.getConnection();

          Arrays.stream(resourceList)
              .filter(Resource::isReadable)
              .filter(fileResource -> fileResource.getFilename() != null)
              .forEach(modelResource -> {
                String fileExtension = Arrays.stream(modelResource.getFilename()
                    .split("\\."))
                    .reduce("", (s1, s2) -> s2);

                RDFFormat format = FileFormats.getFormat(fileExtension);

                if (format != null) {
                  LOG.debug("Adding '{}' into '{}' repository", modelResource.getFilename(), LOCAL_REPOSITORY_ID);

                  try {
                    con.add(modelResource.getInputStream(), "", format);
                  } catch (IOException e) {
                    throw new UncheckedIOException("Error while loading data.", e);
                  }
                }
              });
        });

  }

}
