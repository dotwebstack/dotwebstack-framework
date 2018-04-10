package org.dotwebstack.framework.transaction.flow.step.validation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.config.FileConfigurationBackend;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.validation.RdfModelTransformer;
import org.dotwebstack.framework.validation.ShaclValidator;
import org.dotwebstack.framework.validation.ValidationReport;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
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
  private Environment environment;

  @Mock
  private Resource elmoShapesResource;

  @Mock
  private ShaclValidator shaclValidator;

  @Mock
  private BackendResourceProvider backendResourceProvider;

  @Mock
  private Resource validDataResource;

  @Mock
  private Resource invalidDataResource;

  @Mock
  private Resource shapesResource;

  @Mock
  private ValidationReport report;

  @Mock
  private ResourceLoader resourceLoader;

  private Model transactionModel;

  private ValidationStep validationStep;

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
    // elmoShapesResource = new ClassPathResource("/model/elmo-shapes.trig");
    shaclValidator = mock(ShaclValidator.class);
    // when(shapesResource.getFile()).thenReturn(
    // new ClassPathResource("/shaclvalidation/model/shapes.trig").getFile());
    when(shapesResource.getInputStream()).thenReturn(new InputStreamResource(
        new ClassPathResource("/shaclvalidation/shapes.trig").getInputStream()).getInputStream());
    when(shapesResource.getFilename()).thenReturn("shapes.trig");

    // when(validDataResource.getFile()).thenReturn(
    // new ClassPathResource("/shaclvalidation/model/validData.trig").getFile());
    when(validDataResource.getInputStream()).thenReturn(
        new InputStreamResource(new ClassPathResource(
            "/shaclvalidation/validData.trig").getInputStream()).getInputStream());
    when(validDataResource.getFilename()).thenReturn("validData.trig");

    // when(invalidDataResource.getFile()).thenReturn(
    // new ClassPathResource("/shaclvalidation/model/invalidData.trig").getFile());
    when(invalidDataResource.getInputStream()).thenReturn(
        new InputStreamResource(new ClassPathResource(
            "/shaclvalidation/invalidData.trig").getInputStream()).getInputStream());
    when(invalidDataResource.getFilename()).thenReturn("invalidData.trig");

    // when(elmoShapesResource.getFile()).thenReturn(
    // new ClassPathResource("/shaclvalidation/model/elmo-shapes.trig").getFile());
    when(elmoShapesResource.getInputStream()).thenReturn(new InputStreamResource(
        new ClassPathResource("/model/elmo-shapes.trig").getInputStream()).getInputStream());
    when(elmoShapesResource.getFilename()).thenReturn("elmo-shapes.trig");

    // when(elmoConfigurationResource.getFile()).thenReturn(
    // new ClassPathResource("/shaclvalidation/model/elmo.trig").getFile());
    when(elmoConfigurationResource.getInputStream()).thenReturn(new InputStreamResource(
        new ClassPathResource("/model/elmo.trig").getInputStream()).getInputStream());
    when(elmoConfigurationResource.getFilename()).thenReturn("elmo.trig");

    report = mock(ValidationReport.class);
    when(report.isValid()).thenReturn(true);
    when(shaclValidator.validate(any(), (Model) any())).thenReturn(report);

    Resource[] projectResources = new Resource[] {elmoShapesResource, shapesResource,
        validDataResource, invalidDataResource, elmoConfigurationResource};
    when(((ResourcePatternResolver) resourceLoader).getResources(any())).thenReturn(
        projectResources);

  }

  @Test
  public void constructor_ThrowsNoErrors_WithValidObjects() {
    // Arrange
    final Model validationModel = new LinkedHashModel();
    validationStep = new ValidationStep.Builder(ELMO.VALIDATION_STEP, validationModel).conformsTo(
        ELMO.SHACL_CONCEPT_GRAPHNAME).build();
    validationStepExecutor = new ValidationStepExecutor(validationStep, transactionModel);

    // Act
    validationStepExecutor.execute(parameters, parameterValues);
  }

  @Test
  public void constructor_ThrowsNoErrors_WithValidData() throws Exception {
    // Arrange
    transactionModel = RdfModelTransformer.getModel(validDataResource.getInputStream());
    final Model validationModel = RdfModelTransformer.getModel(shapesResource.getInputStream());
    System.out.println("validation model output --> " + validationModel.toString());

    backend = new FileConfigurationBackend(elmoConfigurationResource, repository,
        "/shaclvalidation", elmoShapesResource, shaclValidator);
    backend.setResourceLoader(resourceLoader);
    backend.setEnvironment(environment);

    validationStep = new ValidationStep.Builder(ELMO.VALIDATION_STEP, validationModel).conformsTo(
        ELMO.SHACL_CONCEPT_GRAPHNAME).build();
    validationStepExecutor = new ValidationStepExecutor(validationStep, transactionModel);
    backend.loadResources();

    // Act
    validationStepExecutor.execute(parameters, parameterValues);
  }

}
