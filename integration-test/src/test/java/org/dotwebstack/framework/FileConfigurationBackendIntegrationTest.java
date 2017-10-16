package org.dotwebstack.framework;

import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.config.FileConfigurationBackend;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)

@ContextConfiguration(classes = IntegrationTestApplication.class)
public class FileConfigurationBackendIntegrationTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private FileConfigurationBackend fileConfigurationBackend;

  private Resource elmoConfiguration;

  private SailRepository sailRepository;

  @Autowired
  private Environment environment;

  @Before
  public void initVars() {
    elmoConfiguration = new ClassPathResource("/invalidConfig/model/elmo.trig");
    sailRepository = new SailRepository(new MemoryStore());
  }

  @Test
  public void configrateBackend_WithoutPrefixesInBackendfile_throwConfigurationException()
      throws Exception {
    fileConfigurationBackend = new FileConfigurationBackend(elmoConfiguration, sailRepository,
        "invalidConfig");

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Error while loading RDF data.");

    fileConfigurationBackend.setEnvironment(environment);
    fileConfigurationBackend.loadResources();
  }
}
