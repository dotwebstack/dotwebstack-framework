package org.dotwebstack.framework.frontend.ld;

import com.google.common.collect.ImmutableList;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.SupportedMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.mappers.LdRedirectionRequestMapper;
import org.dotwebstack.framework.frontend.ld.mappers.LdRepresentationRequestMapper;
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
  private LdRepresentationRequestMapper ldRepresentationRequestMapper;

  @Mock
  private LdRedirectionRequestMapper ldRedirectionRequestMapper;

  private LdModule ldModule;

  @Before
  public void setUp() {
    ldModule = new LdModule(ldRepresentationRequestMapper, ldRedirectionRequestMapper);
    ldModule.initialize(httpConfiguration);
  }

  @Test
  public void constructor_ThrowsException_WithMissingRepresentationRequestMapper() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new LdModule(null, ldRedirectionRequestMapper);
  }

  @Test
  public void constructor_ThrowsException_WithMissingRedirectionRequestMapper() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new LdModule(ldRepresentationRequestMapper, null);
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
