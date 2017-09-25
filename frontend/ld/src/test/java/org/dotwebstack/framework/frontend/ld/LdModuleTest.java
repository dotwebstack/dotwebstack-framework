package org.dotwebstack.framework.frontend.ld;

import com.google.common.collect.ImmutableList;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.SupportedMediaTypesScanner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LdModuleTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private SupportedMediaTypesScanner supportedMediaTypesScanner =
      new SupportedMediaTypesScanner(ImmutableList.of(), ImmutableList.of());

  private HttpConfiguration httpConfiguration =
      new HttpConfiguration(ImmutableList.of(), supportedMediaTypesScanner);

  @Mock
  private LdRequestMapper requestMapper;

  private LdModule ldModule;

  @Before
  public void setUp() {
    ldModule = new LdModule(requestMapper);
    ldModule.initialize(httpConfiguration);
  }

  @Test
  public void constructor_ThrowsException_WithMissingRequestMapper() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new LdModule(null);
  }

  @Test
  public void initialize_ThrowsException_WithMissingHttpConfiguration() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    ldModule.initialize(null);
  }

  @Test
  public void initialize_DoesNotThrowException_WithHttpConfiguration() {
    // Act
    ldModule.initialize(httpConfiguration);
  }

}
