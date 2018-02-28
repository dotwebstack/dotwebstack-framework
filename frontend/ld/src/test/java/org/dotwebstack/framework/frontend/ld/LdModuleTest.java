package org.dotwebstack.framework.frontend.ld;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.ld.SupportedWriterMediaTypesScannerTest.StubGraphEntityWriter;
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

  private HttpConfiguration httpConfiguration = new HttpConfiguration(ImmutableList.of());

  @Mock
  private LdRepresentationRequestMapper ldRepresentationRequestMapper;

  @Mock
  private LdRedirectionRequestMapper ldRedirectionRequestMapper;

  @Mock
  private SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner;

  @Mock
  private SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  private LdModule ldModule;

  @Before
  public void setUp() {
    ldModule = new LdModule(ldRepresentationRequestMapper, ldRedirectionRequestMapper,
        supportedWriterMediaTypesScanner, supportedReaderMediaTypesScanner);
    ldModule.initialize(httpConfiguration);
  }

  @Test
  public void constructor_ThrowsException_WithMissingRepresentationRequestMapper() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new LdModule(null, ldRedirectionRequestMapper,
        supportedWriterMediaTypesScanner, supportedReaderMediaTypesScanner);
  }

  @Test
  public void constructor_ThrowsException_WithMissingRedirectionRequestMapper() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new LdModule(ldRepresentationRequestMapper, null,
        supportedWriterMediaTypesScanner, supportedReaderMediaTypesScanner);
  }

  @Test
  public void constructor_ThrowsException_WithMissingMediaTypesScanner() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new LdModule(ldRepresentationRequestMapper, ldRedirectionRequestMapper,
        null, supportedReaderMediaTypesScanner);
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

  @Test
  public void constructor_RegistersSparqlProviders_WhenProvidedByScanner() {
    // Arrange
    when(supportedWriterMediaTypesScanner.getGraphEntityWriters()).thenReturn(
        Collections.singletonList(new StubGraphEntityWriter()));

    // Act
    ldModule.initialize(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getInstances(), hasSize(1));
  }

}
