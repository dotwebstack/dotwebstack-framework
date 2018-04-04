package org.dotwebstack.framework.backend.sparql.validationstep;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.config.FileConfigurationBackend;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.transaction.flow.step.validation.ValidationStep;
import org.dotwebstack.framework.validation.ShaclValidator;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

@RunWith(MockitoJUnitRunner.class)
public class ValidationStepExecutorTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private SailRepository repository;

  @Mock
  private Resource elmoConfigurationResource;

  @Mock
  private Resource elmoShapesResource;

  @Mock
  private ShaclValidator shaclValidator;

  @Mock
  private QueryEvaluator queryEvaluator;

  @Mock
  private BackendResourceProvider backendResourceProvider;

  private Model transactionModel;

  private ValidationStep validationStep;

  private ResourceLoader resourceLoader;

  private FileConfigurationBackend backend;

  private ValidationStepExecutor validationStepExecutor;

  private Collection<Parameter> parameters = new ArrayList<>();

  private Map<String, String> parameterValues = new HashMap<>();

  @Before
  public void setUp() throws IOException {
    repository = new SailRepository(new MemoryStore());
    transactionModel = new LinkedHashModel();
    resourceLoader =
        mock(ResourceLoader.class, withSettings().extraInterfaces(ResourcePatternResolver.class));
    elmoConfigurationResource = mock(Resource.class);
    elmoShapesResource = new ClassPathResource("/model/elmo-shapes.trig");
    shaclValidator = mock(ShaclValidator.class);
  }

  @Test
  public void constructor_ThrowsException_WithMissingElmoConfiguration() {
    // Arrange
    backend = new FileConfigurationBackend(elmoConfigurationResource, repository, "file:config",
        elmoShapesResource, shaclValidator);
    validationStep =
        new ValidationStep.Builder(ELMO.VALIDATION_STEP, backendResourceProvider).conformsTo(
            ELMO.SHACL_CONCEPT_GRAPHNAME).fileConfigurationBackend(backend).build();
    validationStepExecutor =
        new ValidationStepExecutor(validationStep, transactionModel, backend, queryEvaluator);
    RepositoryResult<Statement> statements = mock(RepositoryResult.class);

    // Act
    validationStepExecutor.execute(parameters, parameterValues);
  }

}
